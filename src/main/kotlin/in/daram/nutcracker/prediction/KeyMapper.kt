package `in`.daram.nutcracker.prediction

/**
 * 자모(또는 문자)를 물리키 시퀀스로 역매핑하는 인터페이스.
 * 레이아웃별로 구현하며 HangulAutomata.layoutName과 동일한 이름을 사용한다.
 *
 * 반환값은 물리키 시퀀스의 목록이다.
 * 동일한 자모를 생성하는 시퀀스가 여러 개일 경우 모두 반환한다.
 *
 * 예시:
 *   두벌식 ㄱ  → [['r']]
 *   두벌식 ㄲ  → [['R']]  (Shift+r)
 *   SKY-II ㄲ  → [['1','1','1']]  (3탭)
 *   나랏글 ㅜ  → [['6','6']]  (ㅗ → 2회째 → ㅜ)
 *   천지인 ㅏ  → [['1','2']]  (ㅣ + ㆍ)
 *   모토로라 ㅋ → [['1','#']]  (ㄱ + 변환키)
 *   영어 'a'  → [['a']]
 */
interface KeyMapper {
    /** HangulAutomata.layoutName과 일치하는 레이아웃 이름 */
    val layoutName: String

    /**
     * [char]를 입력하기 위한 물리키 시퀀스 목록을 반환한다.
     * 해당 자모를 지원하지 않으면 빈 리스트를 반환한다.
     */
    fun charToKeySequences(char: Char): List<List<Char>>

    /**
     * [char]를 입력하기 위해 가장 먼저 눌러야 할 물리키 집합.
     * 다음 키 하이라이트에 주로 사용된다.
     */
    fun firstKeysFor(char: Char): Set<Char> =
        charToKeySequences(char).mapNotNull { it.firstOrNull() }.toSet()
}
