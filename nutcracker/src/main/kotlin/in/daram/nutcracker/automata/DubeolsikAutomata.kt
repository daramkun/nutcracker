package `in`.daram.nutcracker.automata

import `in`.daram.nutcracker.*

/** §2. 두벌식 오토마타 */
class DubeolsikAutomata : HangulAutomata {
    override val layoutName = "두벌식"

    companion object {
        // §2.2 기본 키 매핑 (소문자 QWERTY → 자모)
        private val KEY_MAP = mapOf(
            'q' to 'ㅂ', 'w' to 'ㅈ', 'e' to 'ㄷ', 'r' to 'ㄱ', 't' to 'ㅅ',
            'y' to 'ㅛ', 'u' to 'ㅕ', 'i' to 'ㅑ', 'o' to 'ㅐ', 'p' to 'ㅔ',
            'a' to 'ㅁ', 's' to 'ㄴ', 'd' to 'ㅇ', 'f' to 'ㄹ', 'g' to 'ㅎ',
            'h' to 'ㅗ', 'j' to 'ㅓ', 'k' to 'ㅏ', 'l' to 'ㅣ',
            'z' to 'ㅋ', 'x' to 'ㅌ', 'c' to 'ㅊ', 'v' to 'ㅍ',
            'b' to 'ㅠ', 'n' to 'ㅜ', 'm' to 'ㅡ',
        )

        // §2.2 Shift 키 매핑
        private val SHIFT_MAP = mapOf(
            'Q' to 'ㅃ', 'W' to 'ㅉ', 'E' to 'ㄸ', 'R' to 'ㄲ', 'T' to 'ㅆ',
            'O' to 'ㅒ', 'P' to 'ㅖ',
        )
    }

    override fun process(state: SyllableState, input: KeyInput): InputResult {
        return when (input) {
            is KeyInput.Special -> processSpecialCommon(state, input.type)
            is KeyInput.Char -> {
                val key = input.key
                val jamo = KEY_MAP[key] ?: SHIFT_MAP[key]
                    ?: return InputResult(
                        state.toComposingString() + key,  // 한글 아닌 문자는 확정 후 그대로
                        "",
                        state.clearFSM()
                    )
                val isConsonant = jamo in CONSONANTS
                val isVowel = jamo in VOWELS
                processCommonFSM(state, jamo, isConsonant, isVowel)
            }
        }
    }

    override fun flush(state: SyllableState): InputResult = flushCommonFSM(state)
}
