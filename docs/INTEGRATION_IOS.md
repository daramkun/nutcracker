# iOS / macOS 통합 가이드

> Swift Package Manager를 통해 nutcracker XCFramework를 iOS/macOS 앱에 연동하는 방법.

---

## 아키텍처 개요

```
UIButton / Custom Key View (UI)
    ↓ 터치 이벤트
UIInputViewController
    ↓ NutcrackerKeyInput 변환
HangulAutomata.process() / flush()
    ↓ NutcrackerInputResult
textDocumentProxy.insertText() / deleteBackward()
    ↓
UITextField / UITextView (타겟 앱)
```

iOS는 `UITextDocumentProxy`에 `setComposingText()` API가 없습니다. 조합 중인 글자 표시는 **앱 내 커스텀 오버레이**로 구현해야 합니다.

---

## 1. 의존성 추가

### Xcode (권장)

1. Xcode > File > Add Package Dependencies
2. 저장소 URL: `https://github.com/daramkun/nutcracker`
3. 버전 규칙 선택 후 **Nutcracker** 타겟 추가

### Package.swift

```swift
// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "MyKeyboard",
    platforms: [.iOS(.v13), .macOS(.v10_15)],
    dependencies: [
        .package(url: "https://github.com/daramkun/nutcracker", from: "<version>")
    ],
    targets: [
        .target(
            name: "MyKeyboard",
            dependencies: ["Nutcracker"]
        )
    ]
)
```

> 바이너리 XCFramework 배포. iOS 13+, macOS 10.15+ 지원.

---

## 2. Swift 브리징 주의사항

Kotlin Multiplatform은 Swift에서 다음과 같이 노출됩니다:

| Kotlin 타입 | Swift 노출 형태 |
|---|---|
| `class HangulAutomata` (interface) | `NutcrackerHangulAutomata` protocol |
| `class DubeolsikAutomata` | `NutcrackerDubeolsikAutomata` class |
| `data class SyllableState` | `NutcrackerSyllableState` class (copy() 없음) |
| `data class InputResult` | `NutcrackerInputResult` class |
| `sealed class KeyInput` | `NutcrackerKeyInput` base + 서브클래스 |
| `KeyInput.Char(key)` | `NutcrackerKeyInputChar(key: KotlinChar)` |
| `KeyInput.Special(type)` | `NutcrackerKeyInputSpecial(type: NutcrackerSpecialKey)` |
| `enum class SpecialKey` | `NutcrackerSpecialKey` enum |

> 정확한 Swift 타입명은 빌드 후 Xcode의 자동완성으로 확인하세요. Kotlin 버전에 따라 접두사가 달라질 수 있습니다.

---

## 3. 기본 통합 패턴

### 상태 관리 래퍼

```swift
import Nutcracker

class HangulInputSession {
    private let automata: NutcrackerHangulAutomata
    private(set) var state: NutcrackerSyllableState

    init(automata: NutcrackerHangulAutomata) {
        self.automata = automata
        self.state = automata.initialState()
    }

    func process(_ input: NutcrackerKeyInput) -> NutcrackerInputResult {
        let result = automata.process(state: state, input: input)
        state = result.newState
        return result
    }

    func flush() -> NutcrackerInputResult {
        let result = automata.flush(state: state)
        state = result.newState
        return result
    }

    func reset() {
        state = automata.initialState()
    }
}
```

### InputResult → textDocumentProxy 적용

```swift
func applyResult(_ result: NutcrackerInputResult, to proxy: UITextDocumentProxy) {
    // 1. committed 처리
    let committed = result.committed
    if committed == "\u{08}" {
        // Backspace sentinel: 이전 확정 문자 삭제
        proxy.deleteBackward()
    } else if !committed.isEmpty {
        proxy.insertText(committed)
    }

    // 2. composing 처리
    // iOS proxy는 setComposingText() 미지원 → 커스텀 오버레이 사용
    updateComposingOverlay(result.composing)
}
```

---

## 4. Custom Keyboard Extension 전체 예시

```swift
import UIKit
import Nutcracker

class KoreanKeyboardViewController: UIInputViewController {

    private let session = HangulInputSession(automata: NutcrackerDubeolsikAutomata())
    private var composingLabel: UILabel!  // 조합 중 글자 표시 오버레이

    // ── 생명주기 ──────────────────────────────────────────────────────────

    override func viewDidLoad() {
        super.viewDidLoad()
        setupKeyboardUI()
        setupComposingOverlay()
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        // 키보드가 사라질 때 조합 확정
        flushComposing()
    }

    override func textWillChange(_ textInput: UITextInput?) {
        // 커서 이동 등 외부 텍스트 변경 시 조합 확정
        flushComposing()
    }

    // ── 키 핸들러 ──────────────────────────────────────────────────────────

    @objc func onCharKeyTapped(_ sender: UIButton) {
        guard let char = sender.currentTitle?.first else { return }
        let input = NutcrackerKeyInputChar(key: KotlinChar(value: char.utf16.first!))
        handleInput(input)
    }

    @objc func onBackspaceTapped() {
        let input = NutcrackerKeyInputSpecial(type: .backspace)
        handleInput(input)
    }

    @objc func onSpaceTapped() {
        let input = NutcrackerKeyInputSpecial(type: .space)
        handleInput(input)
    }

    @objc func onReturnTapped() {
        let input = NutcrackerKeyInputSpecial(type: .enter)
        handleInput(input)
    }

    // ── 내부 처리 ──────────────────────────────────────────────────────────

    private func handleInput(_ input: NutcrackerKeyInput) {
        let result = session.process(input)
        applyResult(result)
    }

    private func applyResult(_ result: NutcrackerInputResult) {
        let proxy = textDocumentProxy

        // committed 처리
        let committed = result.committed
        if committed == "\u{08}" {
            proxy.deleteBackward()
        } else if !committed.isEmpty {
            proxy.insertText(committed)
        }

        // composing 오버레이 업데이트
        updateComposingOverlay(result.composing)
    }

    private func flushComposing() {
        let result = session.flush()
        if !result.committed.isEmpty {
            textDocumentProxy.insertText(result.committed)
        }
        updateComposingOverlay("")
    }

    private func updateComposingOverlay(_ text: String) {
        composingLabel.text = text
        composingLabel.isHidden = text.isEmpty
    }

    // setupKeyboardUI(), setupComposingOverlay() — 앱에서 직접 구현
}
```

---

## 5. macOS Input Method (IMKInputController)

macOS에서는 `IMKInputController`를 사용합니다. iOS와 차이점:

- `inputText(_:client:)` 메서드에서 키 입력 수신
- `composedString` 프로퍼티로 조합 중 문자열 반환 (OS가 underline 처리)
- `commitComposition(_:)` 호출로 확정

```swift
import InputMethodKit
import Nutcracker

class KoreanInputController: IMKInputController {

    private let session = HangulInputSession(automata: NutcrackerDubeolsikAutomata())

    override func inputText(_ string: String!, client sender: Any!) -> Bool {
        guard let char = string.first else { return false }
        let input = NutcrackerKeyInputChar(key: KotlinChar(value: char.utf16.first!))
        let result = session.process(input)
        applyResult(result, client: sender as! IMKTextInput)
        return true
    }

    override func handle(_ event: NSEvent!, client sender: Any!) -> Bool {
        if event.keyCode == 51 { // Delete
            let result = session.process(NutcrackerKeyInputSpecial(type: .backspace))
            applyResult(result, client: sender as! IMKTextInput)
            return true
        }
        return false
    }

    private func applyResult(_ result: NutcrackerInputResult, client: IMKTextInput) {
        let committed = result.committed
        if committed == "\u{08}" {
            client.insertText("", replacementRange: NSRange(location: NSNotFound, length: NSNotFound))
            // macOS: 이전 문자 삭제는 별도 처리 필요
        } else if !committed.isEmpty {
            client.insertText(committed, replacementRange: NSRange(location: NSNotFound, length: NSNotFound))
        }

        if !result.composing.isEmpty {
            client.setMarkedText(
                result.composing,
                selectionRange: NSRange(location: result.composing.count, length: 0),
                replacementRange: NSRange(location: NSNotFound, length: NSNotFound)
            )
        }
    }
}
```

> macOS IME는 `setMarkedText()`로 composing 문자 표시를 OS에 위임할 수 있습니다. iOS와 달리 커스텀 오버레이 불필요.

---

## 6. Backspace Sentinel 처리 상세

iOS `textDocumentProxy`는 `deleteBackward()`가 단순히 커서 앞 문자 하나를 삭제합니다.

`committed == "\u{08}"` 수신 시:

```swift
// 일반적인 경우 — 바로 이전 문자 삭제
proxy.deleteBackward()
```

단, 조합 중인 글자(composing)가 있는 상태라면 `applyResult()`의 composing 처리에서 오버레이를 갱신합니다. 조합 중인 글자가 있을 때 backspace는 `committed == "\b"`가 아닌 `composing` 문자열이 짧아지는 방식으로 반환됩니다.
