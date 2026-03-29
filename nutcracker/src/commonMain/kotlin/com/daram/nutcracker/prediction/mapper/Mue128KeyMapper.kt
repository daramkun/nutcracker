package com.daram.nutcracker.prediction.mapper

import com.daram.nutcracker.prediction.KeyMapper

/**
 * 무이128 역매핑.
 *
 * Mue128Automata.KEY_CYCLE 역전:
 *   자모가 사이클의 N번째(1-based)이면 → 해당 키를 N번 반복한 시퀀스
 *
 *   ㄱ(×1), ㄲ(×2)
 *   ㄷ(×1), ㄸ(×2)
 *   ㅂ(×1), ㅃ(×2)
 *   ㅅ(×1), ㅆ(×2)
 *   ㅈ(×1), ㅉ(×2)
 *   ㅌ(×1), ㅊ(×2)
 *   ㅎ(×1), ㅍ(×2)
 *   ㄴ,ㄹ,ㅁ,ㅇ,ㅋ: 각 1회
 *   ㅔ(×1), ㅖ(×2)  / ㅐ(×1), ㅒ(×2)  / ㅓ(×1), ㅕ(×2)
 *   ㅏ(×1), ㅑ(×2)  / ㅜ(×1), ㅠ(×2)  / ㅗ(×1), ㅛ(×2)
 *   ㅣ,ㅡ: 각 1회
 */
class Mue128KeyMapper : KeyMapper {
    override val layoutName = "무이128"

    companion object {
        private val JAMO_TO_KEY_COUNT: Map<Char, Pair<Char, Int>> = buildMap {
            val keyCycle = mapOf(
                'ㄱ' to listOf('ㄱ', 'ㄲ'),
                'ㄴ' to listOf('ㄴ'),
                'ㄷ' to listOf('ㄷ', 'ㄸ'),
                'ㄹ' to listOf('ㄹ'),
                'ㅁ' to listOf('ㅁ'),
                'ㅂ' to listOf('ㅂ', 'ㅃ'),
                'ㅅ' to listOf('ㅅ', 'ㅆ'),
                'ㅇ' to listOf('ㅇ'),
                'ㅈ' to listOf('ㅈ', 'ㅉ'),
                'ㅌ' to listOf('ㅌ', 'ㅊ'),
                'ㅋ' to listOf('ㅋ'),
                'ㅎ' to listOf('ㅎ', 'ㅍ'),
                'ㅔ' to listOf('ㅔ', 'ㅖ'),
                'ㅐ' to listOf('ㅐ', 'ㅒ'),
                'ㅓ' to listOf('ㅓ', 'ㅕ'),
                'ㅏ' to listOf('ㅏ', 'ㅑ'),
                'ㅣ' to listOf('ㅣ'),
                'ㅜ' to listOf('ㅜ', 'ㅠ'),
                'ㅗ' to listOf('ㅗ', 'ㅛ'),
                'ㅡ' to listOf('ㅡ'),
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
