package `in`.daram.nutcracker.automata

import `in`.daram.nutcracker.*

/**
 * §7. 모토로라 오토마타
 *
 * 각 키에 자모 2개 배치. `#`(한/영 전환 키)가 변환키 역할을 하여
 * 조합 중인 자모를 같은 키의 두 번째 자모로 교체한다.
 *
 * SyllableState.cycleKey: 현재 조합 중인 키 (확정 전)
 * SyllableState.cycleCount: 1=1번째 자모, 2=2번째 자모
 */
class MotorolaAutomata : HangulAutomata {
    override val layoutName = "모토로라"

    companion object {
        // §7.2 키 → [1회, 2회] 자모 리스트
        private val KEY_CYCLE = mapOf(
            '1' to listOf('ㄱ', 'ㅋ'),
            '2' to listOf('ㄴ', 'ㅁ'),
            '3' to listOf('ㅏ', 'ㅓ'),
            '4' to listOf('ㄷ', 'ㅌ'),
            '5' to listOf('ㄹ'),         // 단독 자음 (2회 시 독립 처리)
            '6' to listOf('ㅗ', 'ㅜ'),
            '7' to listOf('ㅂ', 'ㅍ'),
            '8' to listOf('ㅅ'),         // 단독 자음
            '9' to listOf('ㅣ', 'ㅡ'),
            '*' to listOf('ㅈ', 'ㅊ'),
            '0' to listOf('ㅇ', 'ㅎ'),
            // '#' 은 한/영 변환키 (MODE_SWITCH) 역할
        )
    }

    override fun process(state: SyllableState, input: KeyInput): InputResult {
        return when (input) {
            is KeyInput.Special -> handleSpecial(state, input.type)
            is KeyInput.Char -> handleChar(state, input.key)
        }
    }

    private fun handleSpecial(state: SyllableState, key: SpecialKey): InputResult {
        return when (key) {
            SpecialKey.BACKSPACE -> {
                // cycleCount=0: tentative vowel (FSM에 미진입) — tentative만 취소
                if (state.cycleKey != null && state.cycleCount == 0) {
                    return InputResult("", state.toComposingString(), state.copy(cycleKey = null, cycleCount = 0))
                }
                // §7.7: ㅋ→BS→ㄱ: cycleCount가 2라면 1로 되돌림 (변환 전 상태)
                if (state.cycleKey != null && state.cycleCount >= 2) {
                    // 변환된 자모를 1번째 자모로 되돌림
                    val cycle = KEY_CYCLE[state.cycleKey!!]!!
                    val prevJamo = cycle[0]
                    val backResult = processBackspace(state.copy(cycleKey = null, cycleCount = 0))
                    val r = processCommonFSM(backResult.newState, prevJamo, prevJamo in CONSONANTS, prevJamo in VOWELS)
                    return InputResult(r.committed, r.composing, r.newState.copy(cycleKey = state.cycleKey, cycleCount = 1))
                }
                processBackspace(state.copy(cycleKey = null, cycleCount = 0))
            }
            SpecialKey.MODE_SWITCH -> {
                // '#' 키 = 변환키: 직전 미확정 자모를 2번째 자모로 교체
                handleModeSwitch(state)
            }
            else -> processSpecialCommon(state.copy(cycleKey = null, cycleCount = 0), key)
        }
    }

    /**
     * §7.3 [한] 키 처리:
     * - 조합 중(cycleKey != null): 현재 자모를 같은 키의 다음 자모로 교체 (순환)
     * - cycleCount=0 (tentative): FSM에 미진입 상태, compound 시도 후 적용
     * - 확정 후: 무효
     */
    private fun handleModeSwitch(state: SyllableState): InputResult {
        val cycleKey = state.cycleKey ?: return InputResult("", state.toComposingString(), state)
        val cycle = KEY_CYCLE[cycleKey] ?: return InputResult("", state.toComposingString(), state)
        if (cycle.size <= 1) return InputResult("", state.toComposingString(), state)

        // cycleCount=0: tentative vowel (FSM 미진입). 다음 자모를 FSM에 직접 적용 시도.
        val nextCount = if (state.cycleCount == 0) 2 else state.cycleCount % cycle.size + 1
        val nextJamo = cycle[(nextCount - 1) % cycle.size]

        if (state.cycleCount == 0 && state.fsm == FSMState.S2 && nextJamo in VOWELS) {
            val compound = compoundJungseong(state.jung!!, nextJamo)
            if (compound != null) {
                val newState = state.copy(jung = compound, cycleKey = null, cycleCount = 0)
                return InputResult("", compose(state.cho!!, compound).toString(), newState)
            }
            // compound 안됨 → 계속 tentative cycling
            return InputResult("", state.toComposingString(), state.copy(cycleKey = cycleKey, cycleCount = nextCount))
        }

        // 현재 자모 되돌리고 다음 자모로 교체
        val backResult = processBackspace(state.copy(cycleKey = null, cycleCount = 0))
        val r = processCommonFSM(backResult.newState, nextJamo, nextJamo in CONSONANTS, nextJamo in VOWELS)
        return InputResult(r.committed, r.composing, r.newState.copy(cycleKey = cycleKey, cycleCount = nextCount))
    }

    private fun handleChar(state: SyllableState, key: Char): InputResult {
        val cycle = KEY_CYCLE[key]
            ?: run {
                val flushed = flushCommonFSM(state.copy(cycleKey = null, cycleCount = 0))
                return InputResult(flushed.committed + key, "", flushed.newState)
            }

        val jamo = cycle[0]
        val s = state.copy(cycleKey = null, cycleCount = 0)

        // S2에서 모음이 compound 안 되지만 cycle된 버전이 compound 가능하면 tentative hold
        if (s.fsm == FSMState.S2 && jamo in VOWELS) {
            val compound = compoundJungseong(s.jung!!, jamo)
            if (compound == null && cycle.size > 1) {
                val anyCompounds = cycle.any { v -> v in VOWELS && compoundJungseong(s.jung!!, v) != null }
                if (anyCompounds) {
                    // cycleCount=0 = tentative (FSM 미진입)
                    return InputResult("", s.toComposingString(), s.copy(cycleKey = key, cycleCount = 0))
                }
            }
        }

        val r = processCommonFSM(s, jamo, jamo in CONSONANTS, jamo in VOWELS)
        return InputResult(r.committed, r.composing, r.newState.copy(cycleKey = key, cycleCount = 1))
    }

    override fun flush(state: SyllableState): InputResult =
        flushCommonFSM(state.copy(cycleKey = null, cycleCount = 0))
}
