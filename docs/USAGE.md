# Nutcracker 사용 가이드

> **라이브러리 소비자(앱 개발자)용 API 참조 문서**
>
> 라이브러리 자체를 구현/수정하는 경우는 [SPEC_COMMON.md](SPEC_COMMON.md)와
> [SPEC_LAYOUTS.md](SPEC_LAYOUTS.md)를 참고하세요.

---

## 1. 핵심 개념

### 1.1 `committed` vs `composing`

`process()` / `flush()`는 항상 `InputResult`를 반환합니다.

```
InputResult(
    committed: String,  // 이번 입력으로 확정(commit)된 문자열
    composing: String,  // 현재 조합 중인 음절 (화면에 밑줄 표시)
    newState: SyllableState
)
```

- `committed`: 더 이상 변경되지 않는 확정 문자열. 입력 필드에 append 처리.
- `composing`: 아직 조합 중인 문자. 입력 필드의 composing 영역을 해당 문자로 교체.
- 두 값 모두 빈 문자열(`""`)일 수 있음.

### 1.2 `committed == "\b"` — Backspace sentinel

> **IMPORTANT**: `committed`가 `"\b"` (백스페이스 문자, 0x08)이면 **이전에 확정된 마지막 문자를 삭제**하라는 신호입니다. 이 값은 S0 상태(조합 중인 글자 없음)에서 백스페이스 키 입력 시 반환됩니다.

```kotlin
val result = automata.process(state, KeyInput.Special(SpecialKey.BACKSPACE))
state = result.newState

when {
    result.committed == "\b" -> deleteLastCharacter()   // 이전 확정 문자 삭제
    result.committed.isNotEmpty() -> appendText(result.committed)
}
updateComposingArea(result.composing)
```

### 1.3 `SyllableState`는 불변(immutable)

`SyllableState`는 data class이며 불변입니다. 매 입력 후 반드시 `result.newState`로 교체해야 합니다.

```kotlin
// 올바른 패턴
var state = automata.initialState()
val result = automata.process(state, input)
state = result.newState  // 반드시 교체

// 잘못된 패턴 — state를 교체하지 않으면 이전 상태로 계속 처리됨
automata.process(state, input)  // state를 교체하지 않음 → 버그
```

### 1.4 `flush()` 호출 시점

다음 상황에서 반드시 `flush()`를 호출해 조합 중인 음절을 확정해야 합니다:

- 입력 포커스 이탈 (다른 텍스트 필드로 이동)
- 커서 위치 변경 (사용자가 텍스트 중간을 터치)
- 한/영 전환
- 키보드 숨김

```kotlin
fun onFocusLost() {
    val result = automata.flush(state)
    if (result.committed.isNotEmpty()) appendText(result.committed)
    state = result.newState  // S0으로 초기화됨
}
```

---

## 2. 설치

### Android / JVM (Gradle — GitHub Packages)

`settings.gradle.kts`에 저장소 추가:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/daramkun/nutcracker")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull
                    ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

`build.gradle.kts`에 의존성 추가:

```kotlin
dependencies {
    implementation("com.daram:nutcracker:<version>")
}
```

### iOS / macOS (Swift Package Manager)

`Package.swift`:

```swift
dependencies: [
    .package(url: "https://github.com/daramkun/nutcracker", from: "<version>")
],
targets: [
    .target(name: "MyApp", dependencies: ["Nutcracker"])
]
```

또는 Xcode > File > Add Package Dependencies에서 저장소 URL 입력.

> 바이너리 XCFramework 배포. iOS 13+, macOS 10.15+ 지원.

---

## 3. 기본 통합 패턴

### Step 1: 오토마타 인스턴스화

```kotlin
import com.daram.nutcracker.automata.DubeolsikAutomata

val automata = DubeolsikAutomata()
```

레이아웃 선택 기준은 [LAYOUT_SELECTION.md](LAYOUT_SELECTION.md) 참고.

### Step 2: 세션 초기화

```kotlin
var state = automata.initialState()
```

텍스트 필드 포커스 진입 시 호출합니다.

### Step 3: 키 입력 처리

```kotlin
import com.daram.nutcracker.KeyInput
import com.daram.nutcracker.SpecialKey

// 일반 문자 키
fun onCharKey(char: Char) {
    val result = automata.process(state, KeyInput.Char(char))
    state = result.newState
    applyResult(result)
}

// 특수 키
fun onBackspace() {
    val result = automata.process(state, KeyInput.Special(SpecialKey.BACKSPACE))
    state = result.newState
    applyResult(result)
}

fun applyResult(result: InputResult) {
    when {
        result.committed == "\b" -> deleteLastCharacter()
        result.committed.isNotEmpty() -> appendToField(result.committed)
    }
    setComposingText(result.composing)  // 빈 문자열이면 composing 영역 제거
}
```

### Step 4: 포커스 이탈 시 flush

```kotlin
fun onFocusLost() {
    val result = automata.flush(state)
    state = result.newState
    if (result.committed.isNotEmpty()) appendToField(result.committed)
    clearComposingArea()
}
```

---

## 4. SpecialKey 매핑표

| `SpecialKey` 값 | 의미 | 일반적인 물리 키 |
|---|---|---|
| `BACKSPACE` | 뒤로 지우기 | ⌫ / Del |
| `SPACE` | 공백 (조합 확정 후 삽입) | Space |
| `ENTER` | 줄바꿈 (조합 확정 후 삽입) | Enter / Return |
| `CONFIRM` | 조합 확정만 (문자 삽입 없음) | — |
| `SHIFT` | 시프트 (쌍자음 등) | Shift |
| `SHIFT_LOCK` | 쉬프트 잠금 | Caps Lock |
| `DIRECTION_LEFT` | 커서 좌 (조합 확정) | ← |
| `DIRECTION_RIGHT` | 커서 우 (조합 확정) | → |
| `DIRECTION_UP` | 커서 위 (조합 확정) | ↑ |
| `DIRECTION_DOWN` | 커서 아래 (조합 확정) | ↓ |
| `STROKE_ADD` | 획추가/쌍자음 변환 | `*` (나랏글 전용) |
| `MODE_SWITCH` | 한/영 전환 (조합 확정) | 한/영 키 |

> 방향키와 `MODE_SWITCH`는 현재 조합 중인 글자를 먼저 확정(commit)한 후 처리됩니다.
>
> `SPACE`와 `ENTER`는 확정 후 해당 문자(`" "` / `"\n"`)를 `committed`에 포함해 반환합니다.

---

## 5. 레이아웃 선택

| 클래스 | `layoutName` | 주요 용도 |
|---|---|---|
| `DubeolsikAutomata` | `"두벌식"` | 표준 QWERTY 기반 PC/모바일 키보드 |
| `DanmoemAutomata` | `"단모음"` | 숫자패드형, 단모음 두 번 입력 방식 |
| `CheonjiinAutomata` | `"천지인"` | 숫자패드형, 피처폰/스마트TV 리모컨 |
| `NaratgeulAutomata` | `"KT 나랏글"` | KT 기기 기본 배치 |
| `SkyIIAutomata` | `"SKY-II"` | SKY/팬택 기기 호환 |
| `MotorolaAutomata` | `"모토로라"` | 모토로라 기기 호환 |
| `Mue128Automata` | `"무이128"` | 12자음+8모음 멀티탭 |
| `EnglishAutomata` | `"English"` | 영문 pass-through (조합 없음) |

자세한 결정표는 [LAYOUT_SELECTION.md](LAYOUT_SELECTION.md) 참고.

---

## 6. 단어 예측 통합

### 6.1 사전 초기화

번들 사전을 사용하면 별도 설정 없이 한국어·영어 10만 단어를 즉시 사용할 수 있습니다:

```kotlin
import com.daram.nutcracker.prediction.*
import com.daram.nutcracker.prediction.trie.TrieDictionary

// 번들 사전으로 TrieDictionary 생성
val koDictionary = TrieDictionary(InputLanguage.KOREAN).apply {
    initialize(BundledDictionary.getWords(InputLanguage.KOREAN))
}
val enDictionary = TrieDictionary(InputLanguage.ENGLISH).apply {
    initialize(BundledDictionary.getWords(InputLanguage.ENGLISH))
}
```

### 6.2 WordPredictor 초기화

```kotlin
val predictor: WordPredictor = DefaultWordPredictor(
    dictionaries = listOf(koDictionary, enDictionary),
    learningDelegate = myLearningDelegate,  // null이면 학습 기능 비활성화
)
```

### 6.3 매 키 입력 후 예측 호출

```kotlin
// 키 처리 후
val result = automata.process(state, input)
state = result.newState

// 현재 단어의 이미 확정된 부분 (마지막 공백 이후)
val committedWord = getWordBeforeCursor()  // 앱에서 직접 관리

val query = PredictionQuery(
    committedText = committedWord,       // 현재 단어 내 확정된 텍스트
    composingState = result.newState,    // InputResult.newState
    composingText = result.composing,    // InputResult.composing
    language = InputLanguage.KOREAN,
    maxResults = 5,
)

val candidates = predictor.predict(query)
showSuggestionBar(candidates)
```

> **주의**: `committedText`는 전체 필드 텍스트가 아니라 **현재 단어 내 확정된 부분** (마지막 공백 이후의 문자열)입니다.

### 6.4 키 하이라이트 힌트

```kotlin
val keyMapper = DubeolsikKeyMapper()  // 현재 레이아웃에 맞는 KeyMapper 사용
val hints = predictor.nextKeyHints(candidates, keyMapper)
// hints.keyScores: Map<Char, Float>  — 0.0~1.0 강조 강도
highlightKeys(hints.keyScores)
```

### 6.5 사용자 학습 보고

```kotlin
// 사용자가 예측 후보를 선택했을 때
fun onCandidateSelected(word: String) {
    predictor.onCandidateSelected(word, InputLanguage.KOREAN, contextWord)
}

// 사용자가 직접 입력 완료했을 때 (공백/엔터)
fun onWordCommitted(word: String) {
    predictor.onWordCommitted(word, InputLanguage.KOREAN, contextWord)
}
```

### 6.6 UserLearningDelegate 구현 (선택)

라이브러리는 저장소에 의존하지 않습니다. 직접 구현해 주입합니다:

```kotlin
class MyLearningDelegate(private val db: AppDatabase) : UserLearningDelegate {
    override fun loadUserWords(): List<UserWordEntry> =
        db.userWordDao().getAll().map { it.toUserWordEntry() }

    override fun saveUserWord(entry: UserWordEntry) =
        db.userWordDao().upsert(entry.toEntity())

    override fun decayAndCleanup(minScore: Float) =
        db.userWordDao().deleteBelow(minScore)
}
```

`decayAndCleanup()`은 앱 주기적 유지보수 시점(예: 앱 시작 7일마다)에 호출합니다.

---

## 7. 패키지 구조

```
com.daram.nutcracker
├── KeyInput                 sealed class — 키 입력 (Char / Special)
├── SpecialKey               enum — 특수 키 목록
├── FSMState                 enum — S0~S4
├── SyllableState            data class — 조합 상태 (불변)
├── InputResult              data class — 처리 결과
├── HangulAutomata           interface — 모든 레이아웃 공통 인터페이스
├── automata/
│   ├── DubeolsikAutomata
│   ├── DanmoemAutomata
│   ├── CheonjiinAutomata
│   ├── NaratgeulAutomata
│   ├── SkyIIAutomata
│   ├── MotorolaAutomata
│   ├── Mue128Automata
│   └── EnglishAutomata
└── prediction/
    ├── WordPredictor        interface
    ├── DefaultWordPredictor class
    ├── PredictionQuery      data class
    ├── PredictionCandidate  data class
    ├── PredictionDictionary interface
    ├── BundledDictionary    object — 내장 사전 (한국어·영어)
    ├── InputLanguage        enum — KOREAN / ENGLISH / OTHER
    ├── KeyMapper            interface
    ├── UserLearningDelegate interface
    ├── UserWordEntry        data class
    ├── NextKeyHint          data class
    ├── trie/TrieDictionary  class — In-Memory Trie 구현체
    └── mapper/              DubeolsikKeyMapper 등 레이아웃별 KeyMapper
```

---

## 8. 자주 하는 실수

| 실수 | 증상 | 해결 |
|---|---|---|
| `flush()` 미호출 | 포커스 이탈 후 조합 중 글자 손실 | `onFocusLost()`, 커서 이동, 한영전환 시 `flush()` |
| `committed == "\b"` 미처리 | 백스페이스가 아무 동작 안 함 | `committed == "\b"` 체크 후 마지막 문자 삭제 |
| `state` 미교체 | 같은 글자가 반복 입력됨 | 매 `process()` 후 `state = result.newState` |
| `PredictionQuery.committedText`에 전체 텍스트 전달 | 예측 결과 없음 | 현재 단어의 **확정된 부분**만 전달 (마지막 공백 이후) |
| `KeyMapper.layoutName`과 `HangulAutomata.layoutName` 불일치 | `nextKeyHints()` 오작동 | 같은 레이아웃의 쌍을 사용 (예: `DubeolsikAutomata` + `DubeolsikKeyMapper`) |
