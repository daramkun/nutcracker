package com.daram.nutcracker.automata

import com.daram.nutcracker.*

/**
 * §3. 단모음 오토마타
 *
 * 두벌식 FSM을 그대로 사용하되, 동일 키 두 번 입력으로 쌍자음·이중모음을 생성하는
 * 전처리(preprocess)가 추가된다.
 *
 * SyllableState.cycleKey: 직전에 입력된 단모음/단자음 키
 * SyllableState.cycleCount: 해당 키 연속 입력 횟수 (1 이상)
 */
class DanmoemAutomata : HangulAutomata {
    override val layoutName = "단모음"

    companion object {
        // §3.3 자음 매핑 (소문자 QWERTY)
        private val CONSONANT_MAP = mapOf(
            'q' to 'ㅂ', 'w' to 'ㅈ', 'e' to 'ㄷ', 'r' to 'ㄱ', 't' to 'ㅅ',
            'a' to 'ㅁ', 's' to 'ㄴ', 'd' to 'ㅇ', 'f' to 'ㄹ', 'g' to 'ㅎ',
            'z' to 'ㅋ', 'x' to 'ㅌ', 'c' to 'ㅊ', 'v' to 'ㅍ',
        )

        // §3.3 모음 매핑
        private val VOWEL_MAP = mapOf(
            'y' to 'ㅗ', 'u' to 'ㅐ', 'i' to 'ㅔ',
            'h' to 'ㅓ', 'j' to 'ㅏ', 'k' to 'ㅣ',
            'b' to 'ㅜ', 'n' to 'ㅡ',
        )

        // §3.4 쌍자음 변환 (키 두 번 → 쌍자음)
        private val DOUBLE_CONSONANT_MAP = mapOf(
            'q' to 'ㅃ', 'w' to 'ㅉ', 'e' to 'ㄸ', 'r' to 'ㄲ', 't' to 'ㅆ',
        )

        // §3.4 이중모음 변환 (키 두 번 → 이중모음)
        private val DOUBLE_VOWEL_MAP = mapOf(
            'j' to 'ㅑ', 'h' to 'ㅕ', 'y' to 'ㅛ', 'b' to 'ㅠ', 'u' to 'ㅒ', 'i' to 'ㅖ',
        )
    }

    override fun process(state: SyllableState, input: KeyInput): InputResult {
        return when (input) {
            is KeyInput.Special -> {
                // Special 입력 시 cycle 컨텍스트 초기화
                val cleared = if (input.type == SpecialKey.BACKSPACE) state else state.copy(cycleKey = null, cycleCount = 0)
                processSpecialCommon(cleared, input.type)
            }
            is KeyInput.Char -> {
                val key = input.key
                val baseJamo = CONSONANT_MAP[key] ?: VOWEL_MAP[key]
                    ?: run {
                        // 한글 아닌 문자: cycle 초기화 후 확정
                        val flushed = flushCommonFSM(state)
                        return InputResult(flushed.committed + key, "", flushed.newState.copy(cycleKey = null, cycleCount = 0))
                    }

                // §3.5 전처리: 직전 키와 동일한가?
                val isSameKey = state.cycleKey == key
                if (isSameKey) {
                    // 동일 키 두 번째 입력
                    val doubleJamo = DOUBLE_CONSONANT_MAP[key] ?: DOUBLE_VOWEL_MAP[key]
                    if (doubleJamo != null) {
                        // 변환 성공: 직전 자모를 FSM에서 되돌린 뒤 변환된 자모로 재처리
                        val prevState = processBackspace(state.copy(cycleKey = null, cycleCount = 0))
                        val newState = prevState.newState.copy(cycleKey = null, cycleCount = 0)
                        val isConsonant = doubleJamo in CONSONANTS
                        val isVowel = doubleJamo in VOWELS
                        val result = processCommonFSM(newState, doubleJamo, isConsonant, isVowel)
                        // 쌍자음/이중모음 처리 후 cycle 초기화 (세 번째 입력을 새 사이클로)
                        result.copy(newState = result.newState.copy(cycleKey = null, cycleCount = 0))
                    } else {
                        // 변환 없음: 두 번째 입력을 독립 자모로 처리 (cycle 초기화)
                        val clearedState = state.copy(cycleKey = null, cycleCount = 0)
                        val isConsonant = baseJamo in CONSONANTS
                        val isVowel = baseJamo in VOWELS
                        val result = processCommonFSM(clearedState, baseJamo, isConsonant, isVowel)
                        result.copy(newState = result.newState.copy(cycleKey = key, cycleCount = 1))
                    }
                } else {
                    // 새로운 키: 일반 처리, cycle 갱신
                    val clearedState = state.copy(cycleKey = null, cycleCount = 0)
                    val isConsonant = baseJamo in CONSONANTS
                    val isVowel = baseJamo in VOWELS
                    val result = processCommonFSM(clearedState, baseJamo, isConsonant, isVowel)
                    result.copy(newState = result.newState.copy(cycleKey = key, cycleCount = 1))
                }
            }
        }
    }

    override fun flush(state: SyllableState): InputResult =
        flushCommonFSM(state).let { it.copy(newState = it.newState.copy(cycleKey = null, cycleCount = 0)) }
}
