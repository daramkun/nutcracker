package com.daram.nutcracker.prediction.resolver

import com.daram.nutcracker.SyllableState
import com.daram.nutcracker.prediction.AmbiguityResolver

/**
 * 모토로라 미확정 자모 추출.
 *
 * cycleKey가 있으면 '#'(변환키)를 눌러 다른 자모로 전환 가능.
 * cycleCount=0 이면 tentative(FSM 미진입) 상태.
 * cycleCount=1 이면 1번째 자모 확정, '#'으로 2번째 전환 가능.
 * cycleCount=2 이면 이미 2번째 자모로 전환된 상태 → 다음 사이클 없음.
 *
 * 예: cycleKey='6', cycleCount=1 → [ㅗ, ㅜ]  (현재 ㅗ, '#'으로 ㅜ 전환 가능)
 *     cycleKey='6', cycleCount=0 → [ㅗ, ㅜ]  (tentative, 미확정)
 *     cycleKey='6', cycleCount=2 → [ㅜ]      (이미 ㅜ, 더 이상 전환 없음)
 */
class MotorolaAmbiguityResolver : AmbiguityResolver {
    override val layoutName = "모토로라"

    companion object {
        private val KEY_CYCLE = mapOf(
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
    }

    override fun pendingJamos(state: SyllableState): List<Char> {
        val key = state.cycleKey ?: return emptyList()
        val cycle = KEY_CYCLE[key] ?: return emptyList()
        return when {
            state.cycleCount == 0 -> cycle              // tentative: 모든 후보
            state.cycleCount >= cycle.size -> emptyList() // 최대 탭 도달, 미확정 없음
            else -> cycle.subList(state.cycleCount - 1, cycle.size)
        }
    }
}
