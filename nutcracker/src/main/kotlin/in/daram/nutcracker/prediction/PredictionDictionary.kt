package `in`.daram.nutcracker.prediction

/**
 * 단어 예측 사전 추상화.
 * 구현체: TrieDictionary (in-memory), 외부 커스텀 구현체
 */
interface PredictionDictionary {
    /** 이 사전이 지원하는 언어 */
    val language: InputLanguage

    /**
     * 단어 목록으로 사전 초기화. 기존 항목을 완전히 대체한다.
     * [words]의 frequency는 내부적으로 정규화된 score (0.0~1.0)로 변환된다.
     */
    fun initialize(words: List<WordEntry>)

    /**
     * 단어 삽입/업데이트.
     * 이미 존재하면 frequency를 갱신하며, [frequency]가 0이면 삭제한다.
     */
    fun add(word: String, frequency: Int)

    /**
     * [prefix]로 시작하는 단어를 최대 [limit]개 검색한다.
     * 반환값은 (word, score) 쌍의 리스트로, score 내림차순 정렬.
     * [prefix]는 완성된 유니코드 문자열이어야 한다 (부분 음절 처리는 호출 전 수행).
     */
    fun search(prefix: String, limit: Int): List<Pair<String, Float>>

    /**
     * 여러 프리픽스를 한번에 검색 (미확정 자모 처리 등에 사용).
     * 기본 구현은 각 prefix별 search 결과를 병합 후 score 내림차순 정렬하여 반환.
     */
    fun searchMulti(prefixes: List<String>, limit: Int): List<Pair<String, Float>> =
        prefixes.flatMap { search(it, limit) }
            .sortedByDescending { it.second }
            .distinctBy { it.first }
            .take(limit)

    /** 현재 사전에 등록된 단어 수 */
    val size: Int
}
