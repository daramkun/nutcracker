package com.daram.nutcracker.prediction

/**
 * 단어 입력 예측 메인 인터페이스.
 *
 * 구현체는 스레드 안전해야 한다. predict()는 동기 함수이지만
 * Dispatchers.IO 등 백그라운드에서 호출하는 것을 권장한다.
 */
interface WordPredictor {
    /**
     * [query]를 기반으로 예측 후보를 반환한다.
     * 결과는 score 내림차순으로 최대 [PredictionQuery.maxResults]개 반환된다.
     * 목표 응답 시간: 10ms 이내.
     */
    fun predict(query: PredictionQuery): List<PredictionCandidate>

    /**
     * 예측 후보로부터 키보드에서 하이라이트할 다음 키 힌트를 반환한다.
     * [mapper]는 현재 활성 레이아웃의 KeyMapper를 전달해야 한다.
     *
     * predict()와 분리된 이유: 키보드 UI가 없는 경우 힌트 계산을 생략할 수 있다.
     */
    fun nextKeyHints(
        candidates: List<PredictionCandidate>,
        mapper: KeyMapper,
    ): NextKeyHint

    /**
     * 사용자가 예측 후보 [word]를 선택했을 때 호출.
     * 내부적으로 사용자 학습 score를 갱신하고 UserLearningDelegate에 저장을 위임한다.
     * [context]는 선택 당시의 앞 단어(문맥)로, 향후 바이그램 학습에 활용된다.
     */
    fun onCandidateSelected(word: String, language: InputLanguage, context: String = "")

    /**
     * 사용자가 예측 후보 없이 단어를 직접 커밋했을 때 호출.
     * 번들 사전에 없는 단어라면 사용자 사전에 신규 등록한다.
     * [context]는 커밋 당시의 앞 단어.
     */
    fun onWordCommitted(word: String, language: InputLanguage, context: String = "")
}
