package com.daram.nutcracker.prediction

import com.daram.nutcracker.SyllableState

/**
 * 단어 예측 요청. 매 키 입력 후 생성하여 WordPredictor.predict()에 전달한다.
 *
 * [committedText]   - 현재 단어 내 확정된 텍스트 (마지막 공백 이후의 문자열)
 * [composingState]  - 현재 FSM 상태 (InputResult.newState)
 * [composingText]   - 조합 중인 음절 표시 문자열 (InputResult.composing)
 * [language]        - 현재 입력 언어
 * [pendingAmbiguous]- 레이아웃 특성상 현재 자모/문자가 아직 미확정 상태인 경우 true
 *                     (SKY-II cycleKey, 천지인 vowelBuffer, 단모음 cycleKey, 모토로라 cycleKey 등)
 * [candidateJamos]  - [pendingAmbiguous]가 true일 때 현재 사이클에서 나올 수 있는 모든 자모 목록
 *                     AmbiguityResolver.pendingJamos()로 구성
 * [maxResults]      - 반환할 최대 후보 수
 */
data class PredictionQuery(
    val committedText: String,
    val composingState: SyllableState,
    val composingText: String,
    val language: InputLanguage,
    val pendingAmbiguous: Boolean = false,
    val candidateJamos: List<Char> = emptyList(),
    val maxResults: Int = 5,
)
