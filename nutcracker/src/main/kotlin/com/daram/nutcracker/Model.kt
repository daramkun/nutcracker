package com.daram.nutcracker

/** 키 입력 추상화 */
sealed class KeyInput {
    data class Char(val key: kotlin.Char) : KeyInput()
    data class Special(val type: SpecialKey) : KeyInput()
}

enum class SpecialKey {
    BACKSPACE, SPACE, ENTER, CONFIRM,
    SHIFT, SHIFT_LOCK,
    DIRECTION_LEFT, DIRECTION_RIGHT, DIRECTION_UP, DIRECTION_DOWN,
    STROKE_ADD,   // * 키 (획추가/쌍자음)
    MODE_SWITCH   // 한영 전환
}

/** FSM 상태 */
enum class FSMState { S0, S1, S2, S3, S3D, S4 }

/** 현재 조합 중인 음절 상태 (immutable) */
data class SyllableState(
    val fsm: FSMState = FSMState.S0,
    val cho: kotlin.Char? = null,       // 초성 자모
    val jung: kotlin.Char? = null,      // 중성 자모 (복합 중성 포함)
    val jong: kotlin.Char? = null,      // 종성 첫 번째 자모
    val jong2: kotlin.Char? = null,     // 종성 두 번째 자모 (겹받침)
    // 레이아웃별 추가 컨텍스트
    val cycleKey: kotlin.Char? = null,  // 현재 순환 중인 키
    val cycleCount: Int = 0,            // 순환 횟수
    val vowelBuffer: List<kotlin.Char> = emptyList() // 모음 조합 버퍼 (천지인/SKY)
)

/** 키 입력 처리 결과 */
data class InputResult(
    val committed: String,       // 이번 입력으로 확정된 문자열
    val composing: String,       // 현재 조합 중인 문자 (화면 표시용)
    val newState: SyllableState
)
