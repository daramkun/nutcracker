package com.daram.nutcracker.automata

import com.daram.nutcracker.*

/**
 * §8. 무이128 오토마타
 *
 * 두벌식 기반 12자음+8모음 멀티탭 레이아웃.
 * 물리 키 = 각 사이클의 첫 번째 자모.
 *
 * 자음 멀티탭:
 *   ㄱ→[ㄱ,ㄲ], ㄷ→[ㄷ,ㄸ], ㅂ→[ㅂ,ㅃ], ㅅ→[ㅅ,ㅆ], ㅈ→[ㅈ,ㅉ]
 *   ㅌ→[ㅌ,ㅊ] (2015-05-25 수정: ㅌ 우선), ㅎ→[ㅎ,ㅍ]
 *   ㄴ,ㄹ,ㅁ,ㅇ,ㅋ: 단독 키
 *
 * 모음 멀티탭:
 *   ㅔ→[ㅔ,ㅖ], ㅐ→[ㅐ,ㅒ], ㅓ→[ㅓ,ㅕ], ㅏ→[ㅏ,ㅑ], ㅜ→[ㅜ,ㅠ], ㅗ→[ㅗ,ㅛ]
 *   ㅣ,ㅡ: 단독 키
 *
 * 복합 모음 이어치기(ㅗ+ㅏ=ㅘ 등)는 공통 FSM §1-5 테이블로 처리.
 */
class Mue128Automata : HangulAutomata {
    override val layoutName = "무이128"

    companion object {
        // §8-3 키 → 자모 순환 리스트 (물리 키 = 첫 번째 자모)
        private val KEY_CYCLE = mapOf(
            // 자음
            'ㄱ' to listOf('ㄱ', 'ㄲ'),
            'ㄴ' to listOf('ㄴ'),
            'ㄷ' to listOf('ㄷ', 'ㄸ'),
            'ㄹ' to listOf('ㄹ'),
            'ㅁ' to listOf('ㅁ'),
            'ㅂ' to listOf('ㅂ', 'ㅃ'),
            'ㅅ' to listOf('ㅅ', 'ㅆ'),
            'ㅇ' to listOf('ㅇ'),
            'ㅈ' to listOf('ㅈ', 'ㅉ'),
            'ㅌ' to listOf('ㅌ', 'ㅊ'),  // 2015-05-25 수정: ㅌ 우선
            'ㅋ' to listOf('ㅋ'),
            'ㅎ' to listOf('ㅎ', 'ㅍ'),
            // 모음
            'ㅔ' to listOf('ㅔ', 'ㅖ'),
            'ㅐ' to listOf('ㅐ', 'ㅒ'),
            'ㅓ' to listOf('ㅓ', 'ㅕ'),
            'ㅏ' to listOf('ㅏ', 'ㅑ'),
            'ㅣ' to listOf('ㅣ'),
            'ㅜ' to listOf('ㅜ', 'ㅠ'),
            'ㅗ' to listOf('ㅗ', 'ㅛ'),
            'ㅡ' to listOf('ㅡ'),
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
        val s = if (state.cycleKey != null) state.copy(cycleKey = null, cycleCount = 0) else state
        return processSpecialCommon(s, key)
    }

    private fun handleBackspace(state: SyllableState): InputResult {
        // §1.10-2: 멀티탭 진행 중이더라도 FSM BS 기준으로만 처리
        val s = state.copy(cycleKey = null, cycleCount = 0)
        return processBackspace(s)
    }

    private fun handleChar(state: SyllableState, key: Char): InputResult {
        val cycle = KEY_CYCLE[key]
            ?: run {
                val flushed = flushAll(state)
                return InputResult(flushed.committed + key, "", flushed.newState)
            }

        return if (state.cycleKey == key) {
            // 동일 키 반복: 사이클 순환
            val nextCount = state.cycleCount + 1
            val jamo = cycle[(nextCount - 1) % cycle.size]
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
