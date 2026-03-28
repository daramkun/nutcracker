package `in`.daram.nutcracker.prediction.mapper

import `in`.daram.nutcracker.prediction.KeyMapper

/**
 * 모토로라 역매핑.
 *
 * MotorolaAutomata.KEY_CYCLE 역전:
 *   1회 자모 → [key]
 *   2회 자모 → [key, '#']  ('#'는 변환키 MODE_SWITCH)
 *
 *   1: ㄱ→['1'],     ㅋ→['1','#']
 *   2: ㄴ→['2'],     ㅁ→['2','#']
 *   3: ㅏ→['3'],     ㅓ→['3','#']
 *   4: ㄷ→['4'],     ㅌ→['4','#']
 *   5: ㄹ→['5']
 *   6: ㅗ→['6'],     ㅜ→['6','#']
 *   7: ㅂ→['7'],     ㅍ→['7','#']
 *   8: ㅅ→['8']
 *   9: ㅣ→['9'],     ㅡ→['9','#']
 *   *: ㅈ→['*'],     ㅊ→['*','#']
 *   0: ㅇ→['0'],     ㅎ→['0','#']
 *
 * 복합 중성(ㅗ+ㅏ=ㅘ 등)은 FSM 레벨에서 처리되므로
 * nextJamos 계산 시 이미 분해된 자모로 제공됨 → 개별 시퀀스 조합으로 처리.
 */
class MotorolaKeyMapper : KeyMapper {
    override val layoutName = "모토로라"

    companion object {
        private val JAMO_TO_SEQUENCES: Map<Char, List<List<Char>>> = buildMap {
            val keyCycle = mapOf(
                '1' to listOf('ㄱ', 'ㅋ'),
                '2' to listOf('ㄴ', 'ㅁ'),
                '3' to listOf('ㅏ', 'ㅓ'),
                '4' to listOf('ㄷ', 'ㅌ'),
                '5' to listOf('ㄹ'),
                '6' to listOf('ㅗ', 'ㅜ'),
                '7' to listOf('ㅂ', 'ㅍ'),
                '8' to listOf('ㅅ'),
                '9' to listOf('ㅣ', 'ㅡ'),
                '*' to listOf('ㅈ', 'ㅊ'),
                '0' to listOf('ㅇ', 'ㅎ'),
            )
            for ((key, jamos) in keyCycle) {
                for ((idx, jamo) in jamos.withIndex()) {
                    val seq = if (idx == 0) listOf(key) else listOf(key, '#')
                    put(jamo, listOf(seq))
                }
            }
        }
    }

    override fun charToKeySequences(char: Char): List<List<Char>> =
        JAMO_TO_SEQUENCES[char] ?: emptyList()
}
