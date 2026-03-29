package com.daram.nutcracker.prediction.mapper

import com.daram.nutcracker.prediction.KeyMapper

/**
 * SKY-II 역매핑.
 *
 * SkyIIAutomata.KEY_CYCLE 역전:
 *   자모가 사이클의 N번째(1-based)이면 → 해당 키를 N번 반복한 시퀀스
 *
 *   1: ㄱ(×1), ㅋ(×2), ㄲ(×3)
 *   2: ㅣ(×1), ㅡ(×2)
 *   3: ㅏ(×1), ㅑ(×2)
 *   4: ㄷ(×1), ㅌ(×2), ㄸ(×3)
 *   5: ㄴ(×1), ㄹ(×2)
 *   6: ㅓ(×1), ㅕ(×2)
 *   7: ㅁ(×1), ㅅ(×2)
 *   8: ㅂ(×1), ㅍ(×2), ㅃ(×3)
 *   9: ㅗ(×1), ㅛ(×2)
 *   *: ㅈ(×1), ㅊ(×2), ㅉ(×3)
 *   0: ㅇ(×1), ㅎ(×2)
 *   #: ㅜ(×1), ㅠ(×2)
 */
class SkyIIKeyMapper : KeyMapper {
    override val layoutName = "SKY-II"

    companion object {
        // SkyIIAutomata.KEY_CYCLE 역전: 자모 → (물리키, 탭 횟수)
        private val JAMO_TO_KEY_COUNT: Map<Char, Pair<Char, Int>> = buildMap {
            val keyCycle = mapOf(
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
            for ((key, jamos) in keyCycle) {
                for ((idx, jamo) in jamos.withIndex()) {
                    put(jamo, key to (idx + 1))
                }
            }
        }
    }

    override fun charToKeySequences(char: Char): List<List<Char>> {
        val (key, count) = JAMO_TO_KEY_COUNT[char] ?: return emptyList()
        return listOf(List(count) { key })
    }
}
