package `in`.daram.nutcracker.prediction.resolver

import `in`.daram.nutcracker.SyllableState
import `in`.daram.nutcracker.prediction.AmbiguityResolver

/**
 * SKY-II 미확정 자모 추출.
 *
 * cycleKey가 있으면 현재 탭 횟수(cycleCount) 이후로 추가 탭이 가능하므로
 * cycleCount번째부터 마지막까지의 모든 자모를 반환한다.
 *
 * 예: cycleKey='1', cycleCount=1 → [ㄱ, ㅋ, ㄲ]  (현재 ㄱ, 더 탭하면 ㅋ/ㄲ)
 *     cycleKey='1', cycleCount=2 → [ㅋ, ㄲ]
 */
class SkyIIAmbiguityResolver : AmbiguityResolver {
    override val layoutName = "SKY-II"

    companion object {
        private val KEY_CYCLE = mapOf(
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
    }

    override fun pendingJamos(state: SyllableState): List<Char> {
        val key = state.cycleKey ?: return emptyList()
        val cycle = KEY_CYCLE[key] ?: return emptyList()
        // cycleCount는 1-based. 현재 선택된 인덱스 = (cycleCount-1) % size
        val currentIdx = (state.cycleCount - 1).coerceAtLeast(0) % cycle.size
        // 현재 자모부터 마지막까지 반환 (추가 탭으로 도달 가능한 모든 후보)
        return cycle.subList(currentIdx, cycle.size)
    }
}
