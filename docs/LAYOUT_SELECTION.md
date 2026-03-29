# 레이아웃 선택 가이드

> 어떤 `HangulAutomata` 구현체를 사용해야 할지 결정하는 참조 문서.

---

## 시나리오별 추천 레이아웃

| 시나리오 | 추천 레이아웃 | 클래스 |
|---|---|---|
| 표준 QWERTY 기반 모바일/PC 키보드 | 두벌식 | `DubeolsikAutomata` |
| 12키 숫자패드, 피처폰 방식 (기본) | 천지인 | `CheonjiinAutomata` |
| 12키 숫자패드, 단모음 두 번 입력 | 단모음 | `DanmoemAutomata` |
| KT 통신사 기기 기본 배치 | KT 나랏글 | `NaratgeulAutomata` |
| SKY/팬택 기기 호환 | SKY-II | `SkyIIAutomata` |
| 모토로라 기기 호환 | 모토로라 | `MotorolaAutomata` |
| 12키, 무이128 방식 멀티탭 | 무이128 | `Mue128Automata` |
| 영문 입력 (한글 조합 없음) | English | `EnglishAutomata` |

**일반적인 권장사항:**
- 신규 앱의 풀 키보드 → **두벌식**
- 신규 앱의 숫자패드 레이아웃 → **천지인** (국내 최다 보급)
- 레거시 기기 호환 필요 → 해당 기기의 레이아웃 선택

---

## 입력 방식 분류

| 클래스 | 키 배치 | 자음 입력 방식 | 모음 입력 방식 |
|---|---|---|---|
| `DubeolsikAutomata` | QWERTY | 직접 입력 (Shift로 쌍자음) | 직접 입력 |
| `DanmoemAutomata` | 숫자패드 | 직접 입력 | 동일 키 2회 입력 |
| `CheonjiinAutomata` | 숫자패드 | 멀티탭 순환 | ㅣ/ㆍ/ㅡ 획 조합 |
| `NaratgeulAutomata` | 숫자패드 | `*`(획추가)로 변환 | 순차 키 조합 |
| `SkyIIAutomata` | 숫자패드 | 1회/2회/3회 탭 구분 | 1회/2회/3회 탭 구분 |
| `MotorolaAutomata` | 숫자패드 | `#` 변환키 방식 | 멀티탭 |
| `Mue128Automata` | 12키 | 멀티탭 순환 | 인접 모음 이어치기 |
| `EnglishAutomata` | QWERTY | pass-through | pass-through |

---

## `layoutName` 정확한 문자열 값

`KeyMapper`와 `HangulAutomata`를 함께 사용할 때 `layoutName`이 일치해야 합니다.

| 클래스 | `layoutName` 값 | 대응 KeyMapper |
|---|---|---|
| `DubeolsikAutomata` | `"두벌식"` | `DubeolsikKeyMapper` |
| `DanmoemAutomata` | `"단모음"` | `DanmoemKeyMapper` |
| `CheonjiinAutomata` | `"천지인"` | `CheonjiinKeyMapper` |
| `NaratgeulAutomata` | `"KT 나랏글"` | `NaratgeulKeyMapper` |
| `SkyIIAutomata` | `"SKY-II"` | `SkyIIKeyMapper` |
| `MotorolaAutomata` | `"모토로라"` | `MotorolaKeyMapper` |
| `Mue128Automata` | `"무이128"` | `Mue128KeyMapper` |
| `EnglishAutomata` | `"English"` | `QwertyKeyMapper` |

> **주의**: `"KT 나랏글"` 에는 공백이 포함됩니다. 문자열 비교 시 주의하세요.

---

## 레이아웃별 특수 동작 요약

### 두벌식 (DubeolsikAutomata)
- Shift 키 또는 대문자 입력으로 쌍자음(ㄲ, ㄸ, ㅃ, ㅆ, ㅉ) 입력
- 표준 한글 2벌식 자판 배열 사용

### 단모음 (DanmoemAutomata)
- 같은 모음 키를 두 번 눌러 다른 모음 생성 (예: ㅏ → ㅏ → ㅑ)
- `cycleKey`, `cycleCount`로 현재 순환 상태 추적

### 천지인 (CheonjiinAutomata)
- ㅣ(1번), ㆍ(2번), ㅡ(3번) 세 키의 조합으로 모든 모음 생성
- `vowelBuffer`에 조합 중인 모음 키 시퀀스 저장

### KT 나랏글 (NaratgeulAutomata)
- `SpecialKey.STROKE_ADD` (`*` 키)로 자음에 획 추가 (ㄱ→ㅋ, ㄴ→ㄷ→ㄹ...)
- 쌍자음은 `*` 두 번으로 생성

### SKY-II (SkyIIAutomata)
- 동일 키 탭 횟수로 자모 결정 (1탭/2탭/3탭)
- `cycleKey`, `cycleCount`로 현재 순환 상태 추적

### 모토로라 (MotorolaAutomata)
- `#` 키(`SpecialKey.STROKE_ADD`)가 변환 키 역할
- 자음 입력 후 `#`으로 다음 자음으로 변환

### 무이128 (Mue128Automata)
- 12자음 + 8모음 배치
- 멀티탭으로 자음 선택, 인접 모음 연속 입력으로 복합 모음 생성
