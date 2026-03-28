package `in`.daram.nutcracker

/** 레이아웃별 키 입력 처리 인터페이스 (순수 함수) */
interface HangulAutomata {
    val layoutName: String

    /** 키 입력 처리. 부수효과 없이 새 상태를 반환한다. */
    fun process(state: SyllableState, input: KeyInput): InputResult

    /** 강제 확정 (커서 이동, 포커스 아웃, 한영 전환 등) */
    fun flush(state: SyllableState): InputResult

    fun initialState(): SyllableState = SyllableState()
}
