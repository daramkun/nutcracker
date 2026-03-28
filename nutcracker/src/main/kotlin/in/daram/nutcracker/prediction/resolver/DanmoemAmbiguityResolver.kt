package `in`.daram.nutcracker.prediction.resolver

import `in`.daram.nutcracker.SyllableState
import `in`.daram.nutcracker.prediction.AmbiguityResolver

/**
 * 단모음 미확정 자모 추출.
 *
 * cycleKey가 있으면 아직 다음 키 입력 여부에 따라 단일 자모 또는 쌍자음/이중모음으로
 * 확정될 수 있다.
 *
 * 예: cycleKey='r'(ㄱ) → [ㄱ, ㄲ]  (한 번 더 누르면 ㄲ)
 *     cycleKey='j'(ㅏ) → [ㅏ, ㅑ]  (한 번 더 누르면 ㅑ)
 */
class DanmoemAmbiguityResolver : AmbiguityResolver {
    override val layoutName = "단모음"

    companion object {
        private val CONSONANT_MAP = mapOf(
            'q' to 'ㅂ', 'w' to 'ㅈ', 'e' to 'ㄷ', 'r' to 'ㄱ', 't' to 'ㅅ',
            'a' to 'ㅁ', 's' to 'ㄴ', 'd' to 'ㅇ', 'f' to 'ㄹ', 'g' to 'ㅎ',
            'z' to 'ㅋ', 'x' to 'ㅌ', 'c' to 'ㅊ', 'v' to 'ㅍ',
        )
        private val DOUBLE_CONSONANT_MAP = mapOf(
            'q' to 'ㅃ', 'w' to 'ㅉ', 'e' to 'ㄸ', 'r' to 'ㄲ', 't' to 'ㅆ',
        )
        private val VOWEL_MAP = mapOf(
            'y' to 'ㅗ', 'u' to 'ㅐ', 'i' to 'ㅔ',
            'h' to 'ㅓ', 'j' to 'ㅏ', 'k' to 'ㅣ',
            'b' to 'ㅜ', 'n' to 'ㅡ',
        )
        private val DOUBLE_VOWEL_MAP = mapOf(
            'j' to 'ㅑ', 'h' to 'ㅕ', 'y' to 'ㅛ', 'b' to 'ㅠ', 'u' to 'ㅒ', 'i' to 'ㅖ',
        )
    }

    override fun pendingJamos(state: SyllableState): List<Char> {
        val key = state.cycleKey ?: return emptyList()
        val base = CONSONANT_MAP[key] ?: VOWEL_MAP[key] ?: return emptyList()
        val doubled = DOUBLE_CONSONANT_MAP[key] ?: DOUBLE_VOWEL_MAP[key]
        return if (doubled != null) listOf(base, doubled) else listOf(base)
    }
}
