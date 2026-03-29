package com.daram.nutcracker.prediction.resolver

import com.daram.nutcracker.SyllableState
import com.daram.nutcracker.prediction.AmbiguityResolver

/**
 * 무이128 미확정 자모 추출.
 *
 * cycleKey가 있으면 현재 탭 횟수(cycleCount) 이후로 추가 탭이 가능하므로
 * cycleCount번째부터 마지막까지의 모든 자모를 반환한다.
 *
 * 예: cycleKey='ㄱ', cycleCount=1 → [ㄱ, ㄲ]  (현재 ㄱ, 더 탭하면 ㄲ)
 *     cycleKey='ㅌ', cycleCount=1 → [ㅌ, ㅊ]
 *     cycleKey='ㅏ', cycleCount=2 → [ㅑ]
 */
class Mue128AmbiguityResolver : AmbiguityResolver {
    override val layoutName = "무이128"

    companion object {
        private val KEY_CYCLE = mapOf(
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
    }

    override fun pendingJamos(state: SyllableState): List<Char> {
        val key = state.cycleKey ?: return emptyList()
        val cycle = KEY_CYCLE[key] ?: return emptyList()
        val currentIdx = (state.cycleCount - 1).coerceAtLeast(0) % cycle.size
        return cycle.subList(currentIdx, cycle.size)
    }
}
