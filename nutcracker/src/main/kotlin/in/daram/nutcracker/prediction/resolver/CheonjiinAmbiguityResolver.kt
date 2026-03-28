package `in`.daram.nutcracker.prediction.resolver

import `in`.daram.nutcracker.SyllableState
import `in`.daram.nutcracker.prediction.AmbiguityResolver

/**
 * 천지인 미확정 자모 추출.
 *
 * vowelBuffer가 비어 있지 않으면 현재 버퍼가 완성되거나 확장되어 여러 모음 중
 * 하나가 확정될 수 있다.
 * CheonjiinAutomata.VOWEL_PATTERNS에서 현재 버퍼를 접두사로 갖는 패턴들의
 * 최종 모음 목록을 반환한다.
 *
 * cycleKey가 있으면 자음 사이클이 미확정 상태.
 * 예: cycleKey='4', cycleCount=1 → [ㄱ, ㅋ, ㄲ]
 */
class CheonjiinAmbiguityResolver : AmbiguityResolver {
    override val layoutName = "천지인"

    companion object {
        private const val MIDDLE_DOT = 'ㆍ'

        private val VOWEL_PATTERNS: List<Pair<List<Char>, Char>> = listOf(
            listOf('ㅣ', MIDDLE_DOT, MIDDLE_DOT, 'ㅣ') to 'ㅒ',
            listOf(MIDDLE_DOT, MIDDLE_DOT, 'ㅣ', 'ㅣ') to 'ㅖ',
            listOf('ㅣ', MIDDLE_DOT, MIDDLE_DOT) to 'ㅑ',
            listOf(MIDDLE_DOT, MIDDLE_DOT, 'ㅣ') to 'ㅕ',
            listOf(MIDDLE_DOT, MIDDLE_DOT, 'ㅡ') to 'ㅛ',
            listOf('ㅡ', MIDDLE_DOT, MIDDLE_DOT) to 'ㅠ',
            listOf('ㅣ', MIDDLE_DOT, 'ㅣ') to 'ㅐ',
            listOf(MIDDLE_DOT, 'ㅣ', 'ㅣ') to 'ㅔ',
            listOf('ㅣ', MIDDLE_DOT) to 'ㅏ',
            listOf(MIDDLE_DOT, 'ㅣ') to 'ㅓ',
            listOf(MIDDLE_DOT, 'ㅡ') to 'ㅗ',
            listOf('ㅡ', MIDDLE_DOT) to 'ㅜ',
            listOf('ㅡ', 'ㅣ') to 'ㅢ',
            listOf('ㅣ') to 'ㅣ',
            listOf('ㅡ') to 'ㅡ',
        )

        private val CONSONANT_CYCLE = mapOf(
            '4' to listOf('ㄱ', 'ㅋ', 'ㄲ'),
            '5' to listOf('ㄴ', 'ㄹ'),
            '6' to listOf('ㄷ', 'ㅌ', 'ㄸ'),
            '7' to listOf('ㅂ', 'ㅍ', 'ㅃ'),
            '8' to listOf('ㅅ', 'ㅎ', 'ㅆ'),
            '9' to listOf('ㅈ', 'ㅊ', 'ㅉ'),
            '0' to listOf('ㅇ', 'ㅁ'),
        )
    }

    override fun pendingJamos(state: SyllableState): List<Char> {
        // 모음 버퍼 미확정
        if (state.vowelBuffer.isNotEmpty()) {
            val buf = state.vowelBuffer
            return VOWEL_PATTERNS
                .filter { (pattern, _) -> pattern.size >= buf.size && pattern.subList(0, buf.size) == buf }
                .map { it.second }
                .distinct()
        }
        // 자음 사이클 미확정
        val key = state.cycleKey ?: return emptyList()
        val cycle = CONSONANT_CYCLE[key] ?: return emptyList()
        return cycle
    }
}
