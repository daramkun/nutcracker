package `in`.daram.nutcracker.prediction

/**
 * 사용자 학습 데이터의 저장/불러오기를 외부에서 구현.
 * 라이브러리는 인터페이스만 제공하며 SQLite 등에 의존하지 않는다.
 */
interface UserLearningDelegate {
    /** 앱 시작 시 저장된 사용자 단어 목록 반환. WordPredictor 초기화 시 호출. */
    fun loadUserWords(): List<UserWordEntry>

    /** 단어 선택/커밋 이벤트를 받아 저장소에 반영. */
    fun saveUserWord(entry: UserWordEntry)

    /**
     * 주기적 score 감쇠 및 정리 처리.
     * [minScore] 미만으로 감쇠된 단어는 삭제.
     * 앱 단에서 적절한 시점에 호출한다.
     */
    fun decayAndCleanup(minScore: Float)
}
