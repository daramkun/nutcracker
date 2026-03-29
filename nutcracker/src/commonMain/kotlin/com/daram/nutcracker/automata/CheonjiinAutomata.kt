package com.daram.nutcracker.automata

import com.daram.nutcracker.*

/**
 * §4. 천지인 오토마타
 *
 * ㅣ(1), ㆍ(2), ㅡ(3) 세 획 키의 순서 있는 조합으로 모음을 생성 (VowelContext).
 * 자음은 동일 키 반복 입력으로 파생 (cycleKey/cycleCount).
 *
 * vowelBuffer가 더 긴 패턴의 접두사이면 대기하며 tentative 표시.
 * 자음 키 입력 시 vowelBuffer를 강제 확정(force-finalize).
 *
 * ㆍ (가운뎃점, U+318D)는 모음 조합에만 사용되며 단독 출력되지 않음.
 */
class CheonjiinAutomata : HangulAutomata {
    override val layoutName = "천지인"

    companion object {
        private const val MIDDLE_DOT = 'ㆍ' // U+318D

        // §4.3 자음 순환 테이블 (키 → 순환 자모 리스트)
        private val CONSONANT_CYCLE = mapOf(
            '4' to listOf('ㄱ', 'ㅋ', 'ㄲ'),
            '5' to listOf('ㄴ', 'ㄹ'),
            '6' to listOf('ㄷ', 'ㅌ', 'ㄸ'),
            '7' to listOf('ㅂ', 'ㅍ', 'ㅃ'),
            '8' to listOf('ㅅ', 'ㅎ', 'ㅆ'),
            '9' to listOf('ㅈ', 'ㅊ', 'ㅉ'),
            '0' to listOf('ㅇ', 'ㅁ'),
        )

        // §4.4 완성 모음 패턴 테이블 (긴 패턴 우선)
        private val VOWEL_PATTERNS: List<Pair<List<Char>, Char>> = listOf(
            listOf('ㅣ', MIDDLE_DOT, MIDDLE_DOT, 'ㅣ') to 'ㅒ',
            listOf(MIDDLE_DOT, MIDDLE_DOT, 'ㅣ', 'ㅣ') to 'ㅖ',
            listOf('ㅣ', MIDDLE_DOT, MIDDLE_DOT) to 'ㅑ',
            listOf(MIDDLE_DOT, MIDDLE_DOT, 'ㅣ') to 'ㅕ',
            listOf(MIDDLE_DOT, MIDDLE_DOT, 'ㅡ') to 'ㅛ',
            listOf('ㅡ', MIDDLE_DOT, MIDDLE_DOT) to 'ㅠ',
            listOf('ㅣ', MIDDLE_DOT, 'ㅣ') to 'ㅐ',
            listOf(MIDDLE_DOT, 'ㅣ', 'ㅣ') to 'ㅔ',
            listOf('ㅣ', MIDDLE_DOT) to 'ㅏ',
            listOf(MIDDLE_DOT, 'ㅣ') to 'ㅓ',
            listOf(MIDDLE_DOT, 'ㅡ') to 'ㅗ',
            listOf('ㅡ', MIDDLE_DOT) to 'ㅜ',
            listOf('ㅡ', 'ㅣ') to 'ㅢ',
            listOf('ㅣ') to 'ㅣ',
            listOf('ㅡ') to 'ㅡ',
        )

        private val VOWEL_INPUT_KEYS = setOf('1', '2', '3')
        private val KEY_TO_VOWEL_BASE = mapOf('1' to 'ㅣ', '2' to MIDDLE_DOT, '3' to 'ㅡ')

        fun matchComplete(buf: List<Char>): Char? =
            VOWEL_PATTERNS.firstOrNull { it.first == buf }?.second

        fun isPrefix(buf: List<Char>): Boolean =
            VOWEL_PATTERNS.any { it.first.size > buf.size && it.first.subList(0, buf.size) == buf }

        /**
         * buf의 최장 일치 모음을 찾는다. 일치 없으면 첫 번째 항목 버리고 나머지 반환.
         */
        fun resolveLongestMatch(buf: List<Char>): Pair<Char?, List<Char>> {
            for (len in buf.size downTo 1) {
                val sub = buf.subList(0, len)
                val match = matchComplete(sub)
                if (match != null) return match to buf.subList(len, buf.size)
            }
            return null to buf.drop(1)
        }

        /**
         * vowelBuffer의 현재 최장 일치 모음을 FSM 상태에 적용한 tentative composing 문자열.
         * 아직 FSM에 전달되지 않은 모음을 화면에 미리 보여주기 위해 사용.
         */
        fun tentativeComposing(state: SyllableState): String {
            val buf = state.vowelBuffer
            if (buf.isEmpty()) return state.toComposingString()
            val bestVowel = (buf.size downTo 1)
                .firstNotNullOfOrNull { len -> matchComplete(buf.subList(0, len)) }
                ?: return state.toComposingString()
            return when (state.fsm) {
                FSMState.S0 -> bestVowel.toString()
                FSMState.S1 -> compose(state.cho!!, bestVowel).toString()
                FSMState.S2 -> {
                    val compound = compoundJungseong(state.jung!!, bestVowel)
                    if (compound != null) compose(state.cho!!, compound).toString()
                    else compose('ㅇ', bestVowel).toString()
                }
                FSMState.S3 -> state.toComposingString()
                FSMState.S3D -> state.toComposingString()
                FSMState.S4 -> {
                    val compound = compoundJungseong(state.jung!!, bestVowel)
                    compound?.toString() ?: bestVowel.toString()
                }
            }
        }
    }

    override fun process(state: SyllableState, input: KeyInput): InputResult {
        return when (input) {
            is KeyInput.Special -> handleSpecial(state, input.type)
            is KeyInput.Char -> handleChar(state, input.key)
        }
    }

    private fun handleSpecial(state: SyllableState, key: SpecialKey): InputResult {
        if (key == SpecialKey.BACKSPACE) return handleBackspace(state)
        // vowelBuffer 강제 확정 후 special 처리
        val s = if (state.vowelBuffer.isNotEmpty()) forceProcessVowelBuffer(state).newState
        else if (state.cycleKey != null) state.copy(cycleKey = null, cycleCount = 0)
        else state
        return processSpecialCommon(s, key)
    }

    private fun handleBackspace(state: SyllableState): InputResult {
        return when {
            state.vowelBuffer.isNotEmpty() -> {
                // vowelBuffer 전체 초기화 (아직 FSM에 전달되지 않은 미확정 입력 취소)
                val newState = state.copy(vowelBuffer = emptyList())
                InputResult("", newState.toComposingString(), newState)
            }
            state.cycleKey != null -> {
                val withoutCycle = state.copy(cycleKey = null, cycleCount = 0)
                processBackspace(withoutCycle)
            }
            else -> processBackspace(state)
        }
    }

    private fun handleChar(state: SyllableState, key: Char): InputResult {
        return when {
            key in VOWEL_INPUT_KEYS -> handleVowelKey(state, key)
            key in CONSONANT_CYCLE -> handleConsonantKey(state, key)
            else -> {
                val flushed = flushAll(state)
                InputResult(flushed.committed + key, "", flushed.newState)
            }
        }
    }

    private fun handleVowelKey(state: SyllableState, key: Char): InputResult {
        val baseVowel = KEY_TO_VOWEL_BASE[key]!!
        // 자음 사이클 중이라면 먼저 확정 (FSM 상태는 그대로 유지, cycle만 초기화)
        val s = if (state.cycleKey != null) state.copy(cycleKey = null, cycleCount = 0) else state
        val newBuf = s.vowelBuffer + baseVowel
        return processVowelBuffer(s.copy(vowelBuffer = newBuf))
    }

    /**
     * vowelBuffer 평가:
     * 1. 더 긴 패턴의 접두사이면 대기 (tentative composing 표시)
     * 2. 완성 패턴이면 FSM에 전달
     * 3. 아무것도 아니면 최장 일치 + 나머지 재처리
     */
    private fun processVowelBuffer(state: SyllableState): InputResult {
        val buf = state.vowelBuffer
        if (buf.isEmpty()) return InputResult("", state.toComposingString(), state)

        // S3/S3D에서 2자 이상 완성 패턴이면 즉시 처리 (단음자는 더 긴 패턴 대기)
        if ((state.fsm == FSMState.S3 || state.fsm == FSMState.S3D)
            && buf.size >= 2 && matchComplete(buf) != null) {
            val complete = matchComplete(buf)!!
            val clearedState = state.copy(vowelBuffer = emptyList())
            return processCommonFSM(clearedState, complete, false, true)
        }

        // 1. 접두사 확인: 더 긴 패턴 가능성 있으면 대기
        if (isPrefix(buf)) {
            return InputResult("", tentativeComposing(state), state)
        }

        // 2. 완성 패턴
        val complete = matchComplete(buf)
        if (complete != null) {
            val clearedState = state.copy(vowelBuffer = emptyList())
            val r = processCommonFSM(clearedState, complete, false, true)
            return r
        }

        // 3. 최장 일치 후 나머지 재처리
        return forceProcessVowelBuffer(state)
    }

    /**
     * vowelBuffer를 강제 확정한다 (접두사 체크 없음).
     * 자음 키 입력이나 flush 시 호출.
     */
    private fun forceProcessVowelBuffer(state: SyllableState): InputResult {
        val buf = state.vowelBuffer
        if (buf.isEmpty()) return InputResult("", state.toComposingString(), state)
        val complete = matchComplete(buf)
        if (complete != null) {
            val clearedState = state.copy(vowelBuffer = emptyList())
            val r = processCommonFSM(clearedState, complete, false, true)
            return r
        }
        val (resolved, rest) = resolveLongestMatch(buf)
        var current = state.copy(vowelBuffer = emptyList())
        var totalCommitted = ""
        if (resolved != null) {
            val r = processCommonFSM(current, resolved, false, true)
            totalCommitted += r.committed
            current = r.newState
        }
        if (rest.isNotEmpty()) {
            val r2 = forceProcessVowelBuffer(current.copy(vowelBuffer = rest))
            return InputResult(totalCommitted + r2.committed, r2.composing, r2.newState)
        }
        return InputResult(totalCommitted, current.toComposingString(), current)
    }

    private fun handleConsonantKey(state: SyllableState, key: Char): InputResult {
        // vowelBuffer가 있으면 강제 확정 후 자음 처리
        if (state.vowelBuffer.isNotEmpty()) {
            val r = forceProcessVowelBuffer(state)
            return continueConsonant(r.newState, key, r.committed)
        }
        return continueConsonant(state, key, "")
    }

    private fun continueConsonant(state: SyllableState, key: Char, extraCommitted: String): InputResult {
        val cycle = CONSONANT_CYCLE[key]!!
        return if (state.cycleKey == key) {
            // 동일 키 반복: 순환 (index = (nextCount-1) % size)
            val nextCount = state.cycleCount + 1
            val jamo = cycle[(nextCount - 1) % cycle.size]
            val withoutCycle = state.copy(cycleKey = null, cycleCount = 0)
            val backState = processBackspace(withoutCycle)
            val r = processCommonFSM(backState.newState, jamo, true, false)
            InputResult(extraCommitted + r.committed, r.composing, r.newState.copy(cycleKey = key, cycleCount = nextCount))
        } else {
            // 다른 키
            val flushedState = if (state.cycleKey != null) state.copy(cycleKey = null, cycleCount = 0) else state
            val jamo = cycle[0]
            val r = processCommonFSM(flushedState, jamo, true, false)
            InputResult(extraCommitted + r.committed, r.composing, r.newState.copy(cycleKey = key, cycleCount = 1))
        }
    }

    private fun flushAll(state: SyllableState): InputResult {
        val s = if (state.vowelBuffer.isNotEmpty()) forceProcessVowelBuffer(state).newState
        else state
        return flushCommonFSM(s.copy(cycleKey = null, cycleCount = 0))
    }

    override fun flush(state: SyllableState): InputResult = flushAll(state)
}
