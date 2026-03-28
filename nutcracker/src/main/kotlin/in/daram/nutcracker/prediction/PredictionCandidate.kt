package `in`.daram.nutcracker.prediction

/**
 * 단어 예측 후보 하나.
 *
 * [word]       - 완성된 단어 제안 (전체 문자열)
 * [score]      - 관련도 점수 (높을수록 우선)
 * [language]   - 단어의 언어
 * [isUserWord] - 사용자 학습 사전에서 온 단어이면 true
 * [nextJamos]  - 현재 입력 이후 사용자가 추가로 입력해야 할 자모/문자 목록.
 *               빈 리스트이면 현재 입력이 이 단어와 이미 일치함.
 *               KeyMapper.charToKeySequences()와 함께 다음 키 하이라이트에 활용한다.
 *
 * 예시:
 *   "닭" 예측, 현재 S1(ㄷ 입력됨) → nextJamos = [ㅏ, ㄹ, ㄱ]
 *   두벌식에서 nextJamos[0]=ㅏ → KeyMapper → 'k' 키 하이라이트
 */
data class PredictionCandidate(
    val word: String,
    val score: Float,
    val language: InputLanguage,
    val isUserWord: Boolean,
    val nextJamos: List<Char>,
)
