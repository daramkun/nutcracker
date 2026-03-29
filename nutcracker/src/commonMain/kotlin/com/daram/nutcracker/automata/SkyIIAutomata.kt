package com.daram.nutcracker.automata

import com.daram.nutcracker.*

/**
 * §6. SKY-II 오토마타
 *
 * 각 키에 자모가 2~3개 배정되며 동일 키 반복 입력(멀티탭)으로 순환한다.
 * 3회 입력 가능한 키는 쌍자음(또는 순환)을 갖는다.
 *
 * SyllableState.cycleKey: 현재 순환 중인 키
 * SyllableState.cycleCount: 순환 횟수 (1-based)
 */
class SkyIIAutomata : HangulAutomata {
    override val layoutName = "SKY-II"

    companion object {
        // §6.2 키 → 자모 순환 리스트
        private val KEY_CYCLE = mapOf(
            '1' to listOf('ㄱ', 'ㅋ', 'ㄲ'),
            '2' to listOf('ㅣ', 'ㅡ'),
            '3' to listOf('ㅏ', 'ㅑ'),
            '4' to listOf('ㄷ', 'ㅌ', 'ㄸ'),
            '5' to listOf('ㄴ', 'ㄹ'),
            '6' to listOf('ㅓ', 'ㅕ'),
            '7' to listOf('ㅁ', 'ㅅ'),
            '8' to listOf('ㅂ', 'ㅍ', 'ㅃ'),
            '9' to listOf('ㅗ', 'ㅛ'),
            '*' to listOf('ㅈ', 'ㅊ', 'ㅉ'),
            '0' to listOf('ㅇ', 'ㅎ'),
            '#' to listOf('ㅜ', 'ㅠ'),
        )
    }

    override fun process(state: SyllableState, input: KeyInput): InputResult {
        return when (input) {
            is KeyInput.Special -> handleSpecial(state, input.type)
            is KeyInput.Char -> handleChar(state, input.key)
        }
    }

    private fun handleSpecial(state: SyllableState, key: SpecialKey): InputResult {
        if (key == SpecialKey.BACKSPACE) return handleBackspace(state)
        // 사이클 확정 후 special 처리
        val s = if (state.cycleKey != null) state.copy(cycleKey = null, cycleCount = 0) else state
        return processSpecialCommon(s, key)
    }

    private fun handleBackspace(state: SyllableState): InputResult {
        // 사이클 중이더라도 FSM BS 기준으로만 처리 (§1.10-2)
        val s = state.copy(cycleKey = null, cycleCount = 0)
        return processBackspace(s)
    }

    private fun handleChar(state: SyllableState, key: Char): InputResult {
        val cycle = KEY_CYCLE[key]
            ?: run {
                // 미지원 키
                val flushed = flushAll(state)
                return InputResult(flushed.committed + key, "", flushed.newState)
            }

        return if (state.cycleKey == key) {
            // 동일 키 반복: 순환 (cycleCount는 1-based이므로 index = (nextCount-1) % size)
            val nextCount = state.cycleCount + 1
            val jamo = cycle[(nextCount - 1) % cycle.size]
            // 이전 자모를 FSM에서 되돌리고 새 자모로 교체
            val withoutCycle = state.copy(cycleKey = null, cycleCount = 0)
            val backResult = processBackspace(withoutCycle)
            val r = processCommonFSM(
                backResult.newState, jamo,
                jamo in CONSONANTS, jamo in VOWELS
            )
            InputResult(r.committed, r.composing, r.newState.copy(cycleKey = key, cycleCount = nextCount))
        } else {
            // 다른 키: 이전 사이클 확정 후 새 자모
            val s = state.copy(cycleKey = null, cycleCount = 0)
            val jamo = cycle[0]
            val r = processCommonFSM(s, jamo, jamo in CONSONANTS, jamo in VOWELS)
            InputResult(r.committed, r.composing, r.newState.copy(cycleKey = key, cycleCount = 1))
        }
    }

    private fun flushAll(state: SyllableState): InputResult {
        val s = state.copy(cycleKey = null, cycleCount = 0)
        return flushCommonFSM(s)
    }

    override fun flush(state: SyllableState): InputResult = flushAll(state)
}
