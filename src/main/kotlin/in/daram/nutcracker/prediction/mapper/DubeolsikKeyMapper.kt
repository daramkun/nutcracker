package `in`.daram.nutcracker.prediction.mapper

import `in`.daram.nutcracker.prediction.KeyMapper

/** 두벌식 역매핑. 자모 → 물리키 1:1 직접 매핑. */
class DubeolsikKeyMapper : KeyMapper {
    override val layoutName = "두벌식"

    companion object {
        // DubeolsikAutomata의 KEY_MAP/SHIFT_MAP 역전
        private val JAMO_TO_KEY: Map<Char, List<Char>> = mapOf(
            'ㅂ' to listOf('q'), 'ㅈ' to listOf('w'), 'ㄷ' to listOf('e'),
            'ㄱ' to listOf('r'), 'ㅅ' to listOf('t'),
            'ㅛ' to listOf('y'), 'ㅕ' to listOf('u'), 'ㅑ' to listOf('i'),
            'ㅐ' to listOf('o'), 'ㅔ' to listOf('p'),
            'ㅁ' to listOf('a'), 'ㄴ' to listOf('s'), 'ㅇ' to listOf('d'),
            'ㄹ' to listOf('f'), 'ㅎ' to listOf('g'),
            'ㅗ' to listOf('h'), 'ㅓ' to listOf('j'), 'ㅏ' to listOf('k'), 'ㅣ' to listOf('l'),
            'ㅋ' to listOf('z'), 'ㅌ' to listOf('x'), 'ㅊ' to listOf('c'), 'ㅍ' to listOf('v'),
            'ㅠ' to listOf('b'), 'ㅜ' to listOf('n'), 'ㅡ' to listOf('m'),
            // Shift 매핑 (대문자)
            'ㅃ' to listOf('Q'), 'ㅉ' to listOf('W'), 'ㄸ' to listOf('E'),
            'ㄲ' to listOf('R'), 'ㅆ' to listOf('T'),
            'ㅒ' to listOf('O'), 'ㅖ' to listOf('P'),
        )
    }

    override fun charToKeySequences(char: Char): List<List<Char>> {
        val key = JAMO_TO_KEY[char] ?: return emptyList()
        return listOf(key)
    }
}
