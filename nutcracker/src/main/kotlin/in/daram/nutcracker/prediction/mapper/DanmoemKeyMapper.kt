package `in`.daram.nutcracker.prediction.mapper

import `in`.daram.nutcracker.prediction.KeyMapper

/**
 * 단모음 역매핑.
 * - 단자음/단모음: 키 1회
 * - 쌍자음/이중모음: 동일 키 2회
 */
class DanmoemKeyMapper : KeyMapper {
    override val layoutName = "단모음"

    companion object {
        // DanmoemAutomata의 CONSONANT_MAP 역전
        private val CONSONANT_TO_KEY = mapOf(
            'ㅂ' to 'q', 'ㅈ' to 'w', 'ㄷ' to 'e', 'ㄱ' to 'r', 'ㅅ' to 't',
            'ㅁ' to 'a', 'ㄴ' to 's', 'ㅇ' to 'd', 'ㄹ' to 'f', 'ㅎ' to 'g',
            'ㅋ' to 'z', 'ㅌ' to 'x', 'ㅊ' to 'c', 'ㅍ' to 'v',
        )
        // DOUBLE_CONSONANT_MAP 역전: 쌍자음 → 키 2회
        private val DOUBLE_CONSONANT_TO_KEY = mapOf(
            'ㅃ' to 'q', 'ㅉ' to 'w', 'ㄸ' to 'e', 'ㄲ' to 'r', 'ㅆ' to 't',
        )
        // VOWEL_MAP 역전
        private val VOWEL_TO_KEY = mapOf(
            'ㅗ' to 'y', 'ㅐ' to 'u', 'ㅔ' to 'i',
            'ㅓ' to 'h', 'ㅏ' to 'j', 'ㅣ' to 'k',
            'ㅜ' to 'b', 'ㅡ' to 'n',
        )
        // DOUBLE_VOWEL_MAP 역전: 이중모음 → 키 2회
        private val DOUBLE_VOWEL_TO_KEY = mapOf(
            'ㅑ' to 'j', 'ㅕ' to 'h', 'ㅛ' to 'y', 'ㅠ' to 'b', 'ㅒ' to 'u', 'ㅖ' to 'i',
        )
    }

    override fun charToKeySequences(char: Char): List<List<Char>> {
        CONSONANT_TO_KEY[char]?.let { return listOf(listOf(it)) }
        DOUBLE_CONSONANT_TO_KEY[char]?.let { return listOf(listOf(it, it)) }
        VOWEL_TO_KEY[char]?.let { return listOf(listOf(it)) }
        DOUBLE_VOWEL_TO_KEY[char]?.let { return listOf(listOf(it, it)) }
        return emptyList()
    }
}
