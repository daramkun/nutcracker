# Nutcracker — 한글 입력 오토마타 라이브러리

> **Claude Code 빠른 경로**
>
> - 앱에서 라이브러리를 **사용(통합)**하려면 → **[docs/USAGE.md](docs/USAGE.md)** 먼저 읽기
> - 레이아웃 클래스 선택이 필요하면 → **[docs/LAYOUT_SELECTION.md](docs/LAYOUT_SELECTION.md)**
> - Android IME 통합 코드 → **[docs/INTEGRATION_ANDROID.md](docs/INTEGRATION_ANDROID.md)**
> - iOS/macOS 통합 코드 → **[docs/INTEGRATION_IOS.md](docs/INTEGRATION_IOS.md)**
> - 오토마타 로직을 **구현/수정**하려면 → 아래 문서 구조 따르기

---

> 용도는 Android 키보드 구현용이나 추후 다른 키보드 구현으로도 사용하면 좋을 것 같아 프로젝트를 분리함.
> 대상 레이아웃: 두벌식 · 단모음 · 천지인 · KT 나랏글 · SKY-II · 모토로라 · 무이128

---

## 📋 문서 구조

이 명세는 다음과 같이 구성되어 있습니다:

### 📌 핵심 문서

| 문서 | 내용 | 대상 |
|------|------|------|
| **[SPEC_COMMON.md](docs/SPEC_COMMON.md)** | 공통 기반 규칙<br/>- 유니코드 음절 조합 공식<br/>- 자모 인덱스 (초성·중성·종성)<br/>- 복합 자모 테이블<br/>- 공통 FSM 상태 및 전이 규칙<br/>- Backspace 동작 명세 | 모든 레이아웃에 적용 |
| **[SPEC_LAYOUTS.md](docs/SPEC_LAYOUTS.md)** | 레이아웃별 규칙<br/>- 두벌식 (Dubeolsik)<br/>- 단모음 (Danmoeum)<br/>- 천지인 (Cheonjiin)<br/>- KT 나랏글 (Naratgeul)<br/>- SKY-II<br/>- 모토로라 (Motorola)<br/>- 무이128 (Mue128) | 각 레이아웃 구현 |
| **[IMPLEMENTATION.md](docs/IMPLEMENTATION.md)** | 공통 구현 지시<br/>- 데이터 모델 (Kotlin)<br/>- 핵심 함수 시그니처<br/>- 불변 구현 규칙<br/>- 레이아웃별 클래스 | 개발자 |
| **[VALIDATION.md](docs/VALIDATION.md)** | 검증 케이스<br/>- 단어 입력 검증 (한글)<br/>- Backspace 동작 검증<br/>- 겹받침 분리 검증<br/>- 이중모음 Backspace 검증 | QA / 테스트 |

---

## 🚀 빠른 시작

### 전체 흐름 이해하기

1. **[SPEC_COMMON.md](docs/SPEC_COMMON.md)** 읽기 → 모든 레이아웃이 공유하는 기본 원리 파악
2. **[SPEC_LAYOUTS.md](docs/SPEC_LAYOUTS.md)** 읽기 → 각 레이아웃의 고유 특징 이해
3. **[IMPLEMENTATION.md](docs/IMPLEMENTATION.md)** 읽기 → 구현할 데이터 모델과 함수 확인
4. **[VALIDATION.md](docs/VALIDATION.md)** 참고 → 테스트 케이스로 검증

### 특정 레이아웃만 구현하기

각 레이아웃 문서에서 해당 섹션만 읽고 구현합니다:

- **두벌식**: [SPEC_LAYOUTS.md의 섹션 2](docs/SPEC_LAYOUTS.md#2-두벌식-dubeolsik)
- **단모음**: [SPEC_LAYOUTS.md의 섹션 3](docs/SPEC_LAYOUTS.md#3-단모음-danmoeum)
- **천지인**: [SPEC_LAYOUTS.md의 섹션 4](docs/SPEC_LAYOUTS.md#4-천지인-cheonjiin)
- **KT 나랏글**: [SPEC_LAYOUTS.md의 섹션 5](docs/SPEC_LAYOUTS.md#5-kt-나랏글-naratgeul)
- **SKY-II**: [SPEC_LAYOUTS.md의 섹션 6](docs/SPEC_LAYOUTS.md#6-sky-ii)
- **모토로라**: [SPEC_LAYOUTS.md의 섹션 7](docs/SPEC_LAYOUTS.md#7-모토로라-motorola)
- **무이128**: [SPEC_LAYOUTS.md의 섹션 8](docs/SPEC_LAYOUTS.md#8-무이128-mue128)

---

## 📖 전체 목차

### SPEC_COMMON.md (공통 기반)

1. 유니코드 음절 조합 공식
2. 초성 인덱스 (19개)
3. 중성 인덱스 (21개)
4. 종성 인덱스 (28개)
5. 복합 중성 조합 테이블
6. 겹받침 조합 테이블
7. 공통 FSM 상태 정의
8. 공통 FSM 전이 규칙
9. 공통 특수 처리
10. Backspace 동작 명세

### SPEC_LAYOUTS.md (레이아웃별 규칙)

- **섹션 2**: 두벌식 (Dubeolsik) - 키 레이아웃, 매핑, FSM, 검증
- **섹션 3**: 단모음 (Danmoeum) - 동일 키 두 번 입력 방식
- **섹션 4**: 천지인 (Cheonjiin) - 모음 조합 컨텍스트
- **섹션 5**: KT 나랏글 (Naratgeul) - 획추가 및 쌍자음 생성
- **섹션 6**: SKY-II - 멀티탭 (1회/2회/3회)
- **섹션 7**: 모토로라 (Motorola) - 변환키(#) 방식
- **섹션 8**: 무이128 (Mue128) - 12자음+8모음 멀티탭, 인접 모음 이어치기

### IMPLEMENTATION.md (구현 지시)

- 데이터 모델 (Kotlin)
- 핵심 함수 시그니처
- 불변 구현 규칙
- 레이아웃별 클래스

### VALIDATION.md (검증 케이스)

- 크로스 레이아웃 단어 검증: "한글"
- 공통 Backspace 검증
- 겹받침 분리 검증
- 이중모음 Backspace 검증

---

## 📝 버전 정보

- **문서 버전**: 1.0
- **대상**: Claude Code 자동화 구현
- **최종 업데이트**: 2026

---

> **💡 팁**: 각 문서는 독립적으로 읽을 수 있지만, **SPEC_COMMON.md 먼저 읽은 후** 필요한 레이아웃 문서를 참고하는 것을 권장합니다.
