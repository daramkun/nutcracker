package com.daram.nutcracker.prediction

/**
 * 키보드 다음 키 하이라이트 힌트.
 *
 * [layoutName] - HangulAutomata.layoutName과 일치해야 함
 * [keyHints]   - 물리키 문자 → 하이라이트 가중치 (0.0~1.0).
 *               가중치 = 해당 키로 도달 가능한 후보들의 score 합, 전체 대비 정규화.
 *               UI는 이 값으로 키의 강조 정도(색상/불투명도 등)를 결정할 수 있다.
 */
data class NextKeyHint(
    val layoutName: String,
    val keyHints: Map<Char, Float>,
)
