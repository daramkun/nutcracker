# Android IME 통합 가이드

> `InputMethodService`를 사용하는 Android 소프트 키보드에 nutcracker를 연동하는 방법.

---

## 아키텍처 개요

```
KeyboardView (UI)
    ↓ 키 클릭 이벤트
InputMethodService.onKeyDown()
    ↓ KeyInput 변환
HangulAutomata.process() / flush()
    ↓ InputResult
InputConnection.commitText() / setComposingText() / deleteSurroundingText()
    ↓
EditText (타겟 앱)
```

nutcracker는 **UI와 OS에 무관한 순수 입력 처리 라이브러리**입니다. `InputConnection` 연동 코드는 앱에서 직접 작성해야 합니다.

---

## 1. 의존성 추가

`settings.gradle.kts`:

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

`build.gradle.kts` (IME 모듈):

```kotlin
dependencies {
    implementation("com.daram:nutcracker:<version>")
}
```

---

## 2. InputMethodService 생명주기 연동

| 생명주기 메서드 | nutcracker 처리 |
|---|---|
| `onStartInput()` | `state = automata.initialState()` |
| `onFinishInput()` | `flush(state)` 후 commitText |
| 커서 이동 감지 | `flush(state)` 후 commitText |
| 한/영 전환 | `flush(state)` 후 오토마타 교체 |

---

## 3. KeyEvent → SpecialKey 매핑

```kotlin
import android.view.KeyEvent
import com.daram.nutcracker.KeyInput
import com.daram.nutcracker.SpecialKey

fun keyEventToInput(keyCode: Int, event: KeyEvent): KeyInput? = when (keyCode) {
    KeyEvent.KEYCODE_DEL -> KeyInput.Special(SpecialKey.BACKSPACE)
    KeyEvent.KEYCODE_SPACE -> KeyInput.Special(SpecialKey.SPACE)
    KeyEvent.KEYCODE_ENTER -> KeyInput.Special(SpecialKey.ENTER)
    KeyEvent.KEYCODE_DPAD_LEFT -> KeyInput.Special(SpecialKey.DIRECTION_LEFT)
    KeyEvent.KEYCODE_DPAD_RIGHT -> KeyInput.Special(SpecialKey.DIRECTION_RIGHT)
    KeyEvent.KEYCODE_DPAD_UP -> KeyInput.Special(SpecialKey.DIRECTION_UP)
    KeyEvent.KEYCODE_DPAD_DOWN -> KeyInput.Special(SpecialKey.DIRECTION_DOWN)
    else -> {
        val char = event.getUnicodeChar(event.metaState)
        if (char != 0) KeyInput.Char(char.toChar()) else null
    }
}
```

소프트 키보드에서는 직접 `KeyInput`을 생성해 전달합니다:

```kotlin
// 소프트 키보드 키 클릭 핸들러
fun onSoftKeyClick(label: Char) {
    handleInput(KeyInput.Char(label))
}

fun onBackspaceClick() {
    handleInput(KeyInput.Special(SpecialKey.BACKSPACE))
}
```

---

## 4. InputResult → InputConnection 적용

```kotlin
import android.view.inputmethod.InputConnection

fun applyResultToConnection(ic: InputConnection, result: InputResult) {
    // 1. committed 처리
    when {
        result.committed == "\b" -> {
            // S0 상태에서 backspace: 이전 확정 문자 삭제
            ic.deleteSurroundingText(1, 0)
        }
        result.committed.isNotEmpty() -> {
            // 확정 문자 커밋 (composing 종료 포함)
            ic.commitText(result.committed, 1)
        }
    }

    // 2. composing 처리
    if (result.composing.isNotEmpty()) {
        ic.setComposingText(result.composing, 1)
    } else if (result.committed.isNotEmpty() && result.committed != "\b") {
        // committed가 있고 composing이 없으면 composing 영역 종료
        ic.finishComposingText()
    }
}
```

---

## 5. 완성된 IME 코드 템플릿

두벌식 기준 최소 구현 예시입니다.

```kotlin
import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import com.daram.nutcracker.*
import com.daram.nutcracker.automata.DubeolsikAutomata

class KoreanInputMethodService : InputMethodService() {

    private val automata: HangulAutomata = DubeolsikAutomata()
    private var state: SyllableState = automata.initialState()

    // ── 생명주기 ────────────────────────────────────────────────────────────

    override fun onCreateInputView(): View {
        // 키보드 레이아웃 inflate — 앱에서 직접 구현
        return layoutInflater.inflate(R.layout.keyboard_view, null)
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        state = automata.initialState()
    }

    override fun onFinishInput() {
        super.onFinishInput()
        flushComposing()
    }

    // ── 키 처리 ────────────────────────────────────────────────────────────

    /** 소프트 키보드 일반 문자 키 */
    fun onCharKey(char: Char) {
        handleInput(KeyInput.Char(char))
    }

    /** 소프트 키보드 특수 키 */
    fun onSpecialKey(key: SpecialKey) {
        handleInput(KeyInput.Special(key))
    }

    /** 하드웨어 키보드 이벤트 */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val input = keyEventToInput(keyCode, event) ?: return super.onKeyDown(keyCode, event)
        handleInput(input)
        return true
    }

    // ── 내부 처리 ────────────────────────────────────────────────────────────

    private fun handleInput(input: KeyInput) {
        val ic = currentInputConnection ?: return
        val result = automata.process(state, input)
        state = result.newState
        applyResultToConnection(ic, result)
    }

    private fun flushComposing() {
        val ic = currentInputConnection ?: return
        val result = automata.flush(state)
        state = result.newState
        if (result.committed.isNotEmpty()) {
            ic.commitText(result.committed, 1)
        }
        ic.finishComposingText()
    }

    private fun applyResultToConnection(ic: InputConnection, result: InputResult) {
        when {
            result.committed == "\b" -> ic.deleteSurroundingText(1, 0)
            result.committed.isNotEmpty() -> ic.commitText(result.committed, 1)
        }
        if (result.composing.isNotEmpty()) {
            ic.setComposingText(result.composing, 1)
        } else if (result.committed.isNotEmpty() && result.committed != "\b") {
            ic.finishComposingText()
        }
    }

    private fun keyEventToInput(keyCode: Int, event: KeyEvent): KeyInput? = when (keyCode) {
        KeyEvent.KEYCODE_DEL -> KeyInput.Special(SpecialKey.BACKSPACE)
        KeyEvent.KEYCODE_SPACE -> KeyInput.Special(SpecialKey.SPACE)
        KeyEvent.KEYCODE_ENTER -> KeyInput.Special(SpecialKey.ENTER)
        KeyEvent.KEYCODE_DPAD_LEFT -> KeyInput.Special(SpecialKey.DIRECTION_LEFT)
        KeyEvent.KEYCODE_DPAD_RIGHT -> KeyInput.Special(SpecialKey.DIRECTION_RIGHT)
        else -> {
            val char = event.getUnicodeChar(event.metaState)
            if (char != 0) KeyInput.Char(char.toChar()) else null
        }
    }
}
```

---

## 6. 단어 예측 연동

```kotlin
import com.daram.nutcracker.prediction.*
import com.daram.nutcracker.prediction.trie.TrieDictionary
import com.daram.nutcracker.prediction.mapper.DubeolsikKeyMapper

class KoreanInputMethodService : InputMethodService() {

    private val automata = DubeolsikAutomata()
    private val keyMapper = DubeolsikKeyMapper()
    private var state = automata.initialState()

    // 번들 사전으로 초기화 (별도 설정 불필요)
    private val predictor: WordPredictor by lazy {
        val koDict = TrieDictionary(InputLanguage.KOREAN).apply {
            initialize(BundledDictionary.getWords(InputLanguage.KOREAN))
        }
        DefaultWordPredictor(listOf(koDict))
    }

    private fun handleInput(input: KeyInput) {
        val ic = currentInputConnection ?: return
        val result = automata.process(state, input)
        state = result.newState
        applyResultToConnection(ic, result)
        updatePredictions(result)
    }

    private fun updatePredictions(result: InputResult) {
        // 현재 단어의 확정된 부분 (마지막 공백 이후)
        val committedWord = getCommittedWordBeforeCursor()

        val query = PredictionQuery(
            committedText = committedWord,
            composingState = result.newState,
            composingText = result.composing,
            language = InputLanguage.KOREAN,
        )

        // IO 스레드에서 실행 권장
        val candidates = predictor.predict(query)

        // 예측 후보 표시 (CandidatesView 또는 커스텀 UI)
        showCandidates(candidates)

        // 다음 키 하이라이트
        val hints = predictor.nextKeyHints(candidates, keyMapper)
        highlightKeys(hints.keyScores)
    }

    /** EditText의 마지막 공백 이후 확정 텍스트 반환 */
    private fun getCommittedWordBeforeCursor(): String {
        val ic = currentInputConnection ?: return ""
        val textBefore = ic.getTextBeforeCursor(100, 0)?.toString() ?: return ""
        return textBefore.substringAfterLast(' ')
    }

    fun onCandidateSelected(candidate: PredictionCandidate) {
        val ic = currentInputConnection ?: return
        // 현재 단어(확정+조합)를 candidate.word로 교체
        flushComposing()
        val committedWord = getCommittedWordBeforeCursor()
        ic.deleteSurroundingText(committedWord.length, 0)
        ic.commitText(candidate.word + " ", 1)
        state = automata.initialState()

        predictor.onCandidateSelected(candidate.word, InputLanguage.KOREAN)
    }

    // showCandidates(), highlightKeys() — 앱에서 직접 구현
}
```

---

## 7. AndroidManifest.xml 설정

```xml
<service
    android:name=".KoreanInputMethodService"
    android:label="@string/app_name"
    android:permission="android.permission.BIND_INPUT_METHOD"
    android:exported="true">
    <intent-filter>
        <action android:name="android.view.InputMethod" />
    </intent-filter>
    <meta-data
        android:name="android.view.im"
        android:resource="@xml/method" />
</service>
```

`res/xml/method.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<input-method xmlns:android="http://schemas.android.com/apk/res/android">
    <subtype
        android:label="한국어"
        android:imeSubtypeLocale="ko"
        android:imeSubtypeMode="keyboard" />
</input-method>
```
