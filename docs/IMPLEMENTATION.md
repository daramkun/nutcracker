# 공통 구현 지시

Claude Code는 아래 지시에 따라 구현하라.

## 8-1. 데이터 모델

```kotlin
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
    val cho: Char? = null,       // 초성 자모
    val jung: Char? = null,      // 중성 자모 (복합 중성 포함)
    val jong: Char? = null,      // 종성 첫 번째 자모
    val jong2: Char? = null,     // 종성 두 번째 자모 (겹받침)
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
```

## 8-2. 핵심 함수 시그니처

```kotlin
/** 유니코드 음절 조합 */
fun compose(cho: Char, jung: Char, jong: Char? = null): Char

/** 유니코드 음절 분해 */
fun decompose(syllable: Char): Triple<Char, Char, Char?>

/** 복합 중성 조합 시도 */
fun compoundJungseong(first: Char, second: Char): Char?

/** 겹받침 조합 시도 */
fun compoundJongseong(first: Char, second: Char): Char?

/** 레이아웃별 키 입력 처리 (순수 함수) */
interface HangulAutomata {
    val layoutName: String
    fun process(state: SyllableState, input: KeyInput): InputResult
    fun flush(state: SyllableState): InputResult  // 강제 확정
    fun initialState(): SyllableState
}
```

## 8-3. 불변 구현 규칙

1. `SyllableState`는 반드시 `immutable`(val only). `process()`는 새 상태를 반환하며 부수효과 없음.
2. 모든 FSM 전이는 [SPEC_COMMON.md의 1-8절](SPEC_COMMON.md#18-공통-fsm-전이-규칙) 규칙 표를 엄격하게 따른다.
3. `compose()` / `decompose()`는 [SPEC_COMMON.md의 1-1~1-4절](SPEC_COMMON.md#1-1-유니코드-음절-조합-공식) 공식만 사용하며 하드코딩 금지.
4. `flush()`는 커서 이동, 포커스 아웃, 한영 전환 등 외부 이벤트 시 반드시 호출된다.
5. 각 레이아웃은 `HangulAutomata` 인터페이스를 구현하고 레이아웃별 키 매핑/순환/모음 조합 로직만 캡슐화한다.

## 8-4. 레이아웃별 구현 클래스

```kotlin
class DubeolsikAutomata : HangulAutomata        // SPEC_LAYOUTS.md 섹션 2
class DanmoemAutomata : HangulAutomata          // SPEC_LAYOUTS.md 섹션 3
class CheonjiinAutomata : HangulAutomata        // SPEC_LAYOUTS.md 섹션 4
class NaratgeulAutomata : HangulAutomata        // SPEC_LAYOUTS.md 섹션 5
class SkyIIAutomata : HangulAutomata            // SPEC_LAYOUTS.md 섹션 6
class MotorolaAutomata : HangulAutomata         // SPEC_LAYOUTS.md 섹션 7
```

---

## 참고 문서

- **[SPEC_COMMON.md](SPEC_COMMON.md)**: 모든 레이아웃이 공유하는 유니코드 공식, FSM, Backspace 규칙
- **[SPEC_LAYOUTS.md](SPEC_LAYOUTS.md)**: 각 레이아웃의 키 매핑, 입력 규칙, 검증 케이스
- **[VALIDATION.md](VALIDATION.md)**: 크로스 레이아웃 검증 시나리오

*이 문서는 Kotlin 기반 구현을 가정합니다. 다른 언어의 경우 문법은 조정하되, 아키텍처와 데이터 모델은 동일하게 유지하세요.*