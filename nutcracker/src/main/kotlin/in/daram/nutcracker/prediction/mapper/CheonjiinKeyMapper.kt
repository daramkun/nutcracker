package `in`.daram.nutcracker.prediction.mapper

import `in`.daram.nutcracker.prediction.KeyMapper

/**
 * 천지인 역매핑.
 *
 * 키:
 *   1=ㅣ, 2=ㆍ(중간점), 3=ㅡ
 *   4=ㄱ계, 5=ㄴ계, 6=ㄷ계, 7=ㅂ계, 8=ㅅ계, 9=ㅈ계, 0=ㅇ계 (멀티탭)
 *
 * 모음은 CheonjiinAutomata.VOWEL_PATTERNS를 역전해 키 시퀀스로 변환:
 *   ㅣ → ['1'],  ㅡ → ['3'],  ㅏ → ['1','2'],  ㅓ → ['2','1'],
 *   ㅗ → ['2','3'],  ㅜ → ['3','2'],  ㅢ → ['3','1'], ...
 *
 * 자음은 CONSONANT_CYCLE 역전 (탭 횟수만큼 반복):
 *   ㄱ→['4'], ㅋ→['4','4'], ㄲ→['4','4','4']
 *   ㄴ→['5'], ㄹ→['5','5']
 *   ㄷ→['6'], ㅌ→['6','6'], ㄸ→['6','6','6']
 *   ㅂ→['7'], ㅍ→['7','7'], ㅃ→['7','7','7']
 *   ㅅ→['8'], ㅎ→['8','8'], ㅆ→['8','8','8']
 *   ㅈ→['9'], ㅊ→['9','9'], ㅉ→['9','9','9']
 *   ㅇ→['0'], ㅁ→['0','0']
 */
class CheonjiinKeyMapper : KeyMapper {
    override val layoutName = "천지인"

    companion object {
        private val JAMO_TO_SEQUENCES: Map<Char, List<List<Char>>> = mapOf(
            // 자음 (CONSONANT_CYCLE 역전)
            'ㄱ' to listOf(listOf('4')),
            'ㅋ' to listOf(listOf('4', '4')),
            'ㄲ' to listOf(listOf('4', '4', '4')),
            'ㄴ' to listOf(listOf('5')),
            'ㄹ' to listOf(listOf('5', '5')),
            'ㄷ' to listOf(listOf('6')),
            'ㅌ' to listOf(listOf('6', '6')),
            'ㄸ' to listOf(listOf('6', '6', '6')),
            'ㅂ' to listOf(listOf('7')),
            'ㅍ' to listOf(listOf('7', '7')),
            'ㅃ' to listOf(listOf('7', '7', '7')),
            'ㅅ' to listOf(listOf('8')),
            'ㅎ' to listOf(listOf('8', '8')),
            'ㅆ' to listOf(listOf('8', '8', '8')),
            'ㅈ' to listOf(listOf('9')),
            'ㅊ' to listOf(listOf('9', '9')),
            'ㅉ' to listOf(listOf('9', '9', '9')),
            'ㅇ' to listOf(listOf('0')),
            'ㅁ' to listOf(listOf('0', '0')),
            // 모음 (VOWEL_PATTERNS 역전: pattern의 1→'1', ㆍ→'2', ㅡ→'3')
            'ㅣ' to listOf(listOf('1')),
            'ㅡ' to listOf(listOf('3')),
            'ㅏ' to listOf(listOf('1', '2')),
            'ㅓ' to listOf(listOf('2', '1')),
            'ㅗ' to listOf(listOf('2', '3')),
            'ㅜ' to listOf(listOf('3', '2')),
            'ㅢ' to listOf(listOf('3', '1')),
            'ㅐ' to listOf(listOf('1', '2', '1')),
            'ㅔ' to listOf(listOf('2', '1', '1')),
            'ㅑ' to listOf(listOf('1', '2', '2')),
            'ㅕ' to listOf(listOf('2', '2', '1')),
            'ㅛ' to listOf(listOf('2', '2', '3')),
            'ㅠ' to listOf(listOf('3', '2', '2')),
            'ㅒ' to listOf(listOf('1', '2', '2', '1')),
            'ㅖ' to listOf(listOf('2', '2', '1', '1')),
        )
    }

    override fun charToKeySequences(char: Char): List<List<Char>> =
        JAMO_TO_SEQUENCES[char] ?: emptyList()
}
