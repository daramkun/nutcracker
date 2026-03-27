package `in`.daram.nutcracker

// §1.2 초성으로 사용 가능한 자음 집합 (쌍자음 포함, 단 ㄸ/ㅃ/ㅉ 은 종성 불가)
internal val CONSONANTS = setOf(
    'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ',
    'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
)

// §1.3 중성으로 사용 가능한 모음 집합 (복합 중성 포함)
internal val VOWELS = setOf(
    'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ',
    'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
)

// 초성으로만 사용 가능 (종성 불가): ㄸ, ㅃ, ㅉ
private val CHOSEONG_ONLY = setOf('ㄸ', 'ㅃ', 'ㅉ')

/** 현재 SyllableState의 composing 문자를 문자열로 반환 (commit용) */
private fun SyllableState.toCommitString(): String = toComposingString()

/** SyllableState를 S0으로 초기화 (레이아웃 컨텍스트 초기화 포함) */
internal fun SyllableState.clearFSM(): SyllableState = copy(
    fsm = FSMState.S0, cho = null, jung = null, jong = null, jong2 = null,
    cycleKey = null, cycleCount = 0, vowelBuffer = emptyList()
)

/** commit 후 S0으로 전환하는 헬퍼 */
private fun commitAndReset(state: SyllableState, extraCommit: String = ""): InputResult {
    val committed = state.toCommitString() + extraCommit
    return InputResult(committed, "", state.clearFSM())
}

/**
 * §1.8 공통 FSM 전이.
 *
 * 레이아웃이 이미 자모로 변환한 [jamo]를 받아 FSM 상태를 전이한다.
 * [isConsonant]/[isVowel]은 [jamo]의 분류이며 호출자가 직접 계산해 전달한다.
 * (자음인지 모음인지 중복 검사 없이 신뢰)
 *
 * 레이아웃 컨텍스트(cycleKey, cycleCount, vowelBuffer)는 변경하지 않는다.
 * 각 레이아웃 automata가 전이 전후에 필요에 따라 관리한다.
 */
fun processCommonFSM(
    state: SyllableState,
    jamo: Char,
    isConsonant: Boolean,
    isVowel: Boolean,
): InputResult {
    return when (state.fsm) {
        // ── S0: 버퍼 비어있음 ──────────────────────────────────────────────
        FSMState.S0 -> when {
            isConsonant -> InputResult(
                "", jamo.toString(),
                state.copy(fsm = FSMState.S1, cho = jamo)
            )
            isVowel -> InputResult(
                "", compose('ㅇ', jamo).toString(),
                state.copy(fsm = FSMState.S4, jung = jamo)
            )
            else -> InputResult(jamo.toString(), "", state)
        }

        // ── S1: 초성만 입력됨 ─────────────────────────────────────────────
        FSMState.S1 -> when {
            isVowel -> {
                val newState = state.copy(fsm = FSMState.S2, jung = jamo)
                InputResult("", compose(state.cho!!, jamo).toString(), newState)
            }
            isConsonant -> {
                // 기존 초성 commit, 새 초성 시작
                val committed = state.cho!!.toString()
                val newState = state.copy(
                    fsm = FSMState.S1, cho = jamo, jung = null, jong = null, jong2 = null
                )
                InputResult(committed, jamo.toString(), newState)
            }
            else -> commitAndReset(state, jamo.toString())
        }

        // ── S2: 초성+중성 ─────────────────────────────────────────────────
        FSMState.S2 -> when {
            isVowel -> {
                val compound = compoundJungseong(state.jung!!, jamo)
                if (compound != null) {
                    // 복합 중성 조합 성공
                    val newState = state.copy(jung = compound)
                    InputResult("", compose(state.cho!!, compound).toString(), newState)
                } else {
                    // 복합 중성 불가 → 현재 글자 확정 후 ㅇ 초성 + 새 모음
                    val committed = compose(state.cho!!, state.jung!!).toString()
                    val newState = state.copy(
                        fsm = FSMState.S2, cho = 'ㅇ', jung = jamo, jong = null, jong2 = null
                    )
                    InputResult(committed, compose('ㅇ', jamo).toString(), newState)
                }
            }
            isConsonant -> {
                if (jamo in CHOSEONG_ONLY) {
                    // 종성 불가 자음 → 현재 글자 확정, 새 초성
                    val committed = compose(state.cho!!, state.jung!!).toString()
                    val newState = state.copy(
                        fsm = FSMState.S1, cho = jamo, jung = null, jong = null, jong2 = null
                    )
                    InputResult(committed, jamo.toString(), newState)
                } else {
                    val newState = state.copy(fsm = FSMState.S3, jong = jamo)
                    InputResult("", compose(state.cho!!, state.jung!!, jamo).toString(), newState)
                }
            }
            else -> commitAndReset(state, jamo.toString())
        }

        // ── S3: 초성+중성+홑받침 ──────────────────────────────────────────
        FSMState.S3 -> when {
            isVowel -> {
                // 종성 → 다음 글자 초성으로 이동
                val committed = compose(state.cho!!, state.jung!!).toString()
                val newState = state.copy(
                    fsm = FSMState.S2, cho = state.jong!!, jung = jamo, jong = null, jong2 = null
                )
                InputResult(committed, compose(state.jong!!, jamo).toString(), newState)
            }
            isConsonant -> {
                val double = compoundJongseong(state.jong!!, jamo)
                if (double != null) {
                    // 겹받침 조합
                    val newState = state.copy(fsm = FSMState.S3D, jong2 = jamo)
                    InputResult("", compose(state.cho!!, state.jung!!, double).toString(), newState)
                } else {
                    // 겹받침 불가 → 현재 글자 확정, 새 초성
                    val committed = compose(state.cho!!, state.jung!!, state.jong).toString()
                    val newState = state.copy(
                        fsm = FSMState.S1, cho = jamo, jung = null, jong = null, jong2 = null
                    )
                    InputResult(committed, jamo.toString(), newState)
                }
            }
            else -> commitAndReset(state, jamo.toString())
        }

        // ── S3D: 초성+중성+겹받침 ─────────────────────────────────────────
        FSMState.S3D -> when {
            isVowel -> {
                // 겹받침 오른쪽 → 다음 글자 초성
                val committed = compose(state.cho!!, state.jung!!, state.jong).toString()
                val newState = state.copy(
                    fsm = FSMState.S2, cho = state.jong2!!, jung = jamo, jong = null, jong2 = null
                )
                InputResult(committed, compose(state.jong2!!, jamo).toString(), newState)
            }
            isConsonant -> {
                val committed = compoundJongseong(state.jong!!, state.jong2!!)!!
                    .let { compose(state.cho!!, state.jung!!, it) }.toString()
                val newState = state.copy(
                    fsm = FSMState.S1, cho = jamo, jung = null, jong = null, jong2 = null
                )
                InputResult(committed, jamo.toString(), newState)
            }
            else -> commitAndReset(state, jamo.toString())
        }

        // ── S4: 중성만 (모음 단독) ────────────────────────────────────────
        FSMState.S4 -> when {
            isVowel -> {
                val compound = compoundJungseong(state.jung!!, jamo)
                if (compound != null) {
                    val newState = state.copy(jung = compound)
                    InputResult("", compose('ㅇ', compound).toString(), newState)
                } else {
                    val committed = compose('ㅇ', state.jung!!).toString()
                    val newState = state.copy(fsm = FSMState.S4, jung = jamo)
                    InputResult(committed, compose('ㅇ', jamo).toString(), newState)
                }
            }
            isConsonant -> {
                val committed = compose('ㅇ', state.jung!!).toString()
                val newState = state.copy(
                    fsm = FSMState.S1, cho = jamo, jung = null, jong = null, jong2 = null
                )
                InputResult(committed, jamo.toString(), newState)
            }
            else -> commitAndReset(state, jamo.toString())
        }
    }
}

/**
 * §1.10 공통 Backspace 처리.
 *
 * committed 마지막 글자 삭제는 [deleteLastCommitted]를 통해 호출자에게 위임한다.
 * (오토마타가 committed를 직접 관리하지 않는 구조이므로)
 * S0 상태에서 BS 시 [deleteLastCommitted]=true 인 InputResult 를 반환한다.
 */
fun processBackspace(state: SyllableState): InputResult {
    return when (state.fsm) {
        FSMState.S0 -> {
            // committed 마지막 글자 삭제 신호 (composing = "\b" 특수 처리 약속)
            InputResult("\b", "", state)
        }
        FSMState.S1 -> {
            // 초성 제거 → S0
            InputResult("", "", state.clearFSM())
        }
        FSMState.S2 -> {
            // 복합 중성이면 첫 모음으로 되돌림, 단순 중성이면 초성만 남김
            val simpleJung = splitJungseong(state.jung!!)
            if (simpleJung != null) {
                // 복합 중성 → 첫 번째 단모음 (S2 유지)
                val newState = state.copy(jung = simpleJung.first)
                InputResult("", compose(state.cho!!, simpleJung.first).toString(), newState)
            } else {
                // 단순 중성 제거 → S1
                val newState = state.copy(fsm = FSMState.S1, jung = null)
                InputResult("", state.cho!!.toString(), newState)
            }
        }
        FSMState.S3 -> {
            // 종성 제거 → S2
            val newState = state.copy(fsm = FSMState.S2, jong = null)
            InputResult("", compose(state.cho!!, state.jung!!).toString(), newState)
        }
        FSMState.S3D -> {
            // 겹받침 → 홑받침 (S3)
            val newState = state.copy(fsm = FSMState.S3, jong2 = null)
            InputResult("", compose(state.cho!!, state.jung!!, state.jong).toString(), newState)
        }
        FSMState.S4 -> {
            // 복합 중성이면 첫 모음으로 되돌림, 단순 모음이면 제거 → S0
            val simpleJung = splitJungseong(state.jung!!)
            if (simpleJung != null) {
                val newState = state.copy(jung = simpleJung.first)
                InputResult("", compose('ㅇ', simpleJung.first).toString(), newState)
            } else {
                InputResult("", "", state.clearFSM())
            }
        }
    }
}

/**
 * 복합 중성을 첫 번째/두 번째 단모음으로 분리한다.
 * 단독 모음(복합이 아닌 경우)은 null 반환.
 */
private fun splitJungseong(jung: Char): Pair<Char, Char>? = when (jung) {
    'ㅘ' -> 'ㅗ' to 'ㅏ'
    'ㅙ' -> 'ㅗ' to 'ㅐ'
    'ㅚ' -> 'ㅗ' to 'ㅣ'
    'ㅝ' -> 'ㅜ' to 'ㅓ'
    'ㅞ' -> 'ㅜ' to 'ㅔ'
    'ㅟ' -> 'ㅜ' to 'ㅣ'
    'ㅢ' -> 'ㅡ' to 'ㅣ'
    else -> null
}

/**
 * §1.9 강제 확정 (커서 이동, 포커스 아웃, 한영 전환 등).
 * 현재 조합 중인 글자를 commit하고 S0으로 전이.
 */
fun flushCommonFSM(state: SyllableState): InputResult {
    if (state.fsm == FSMState.S0) return InputResult("", "", state)
    return InputResult(state.toCommitString(), "", state.clearFSM())
}

/**
 * Special 입력(Backspace 제외)의 공통 처리.
 * Space, Enter, Confirm, 방향키 등은 현재 글자를 확정 후 해당 문자를 commit한다.
 */
fun processSpecialCommon(state: SyllableState, key: SpecialKey): InputResult {
    return when (key) {
        SpecialKey.BACKSPACE -> processBackspace(state)
        SpecialKey.SPACE -> {
            val base = flushCommonFSM(state)
            InputResult(base.committed + " ", "", base.newState)
        }
        SpecialKey.ENTER -> {
            val base = flushCommonFSM(state)
            InputResult(base.committed + "\n", "", base.newState)
        }
        SpecialKey.CONFIRM,
        SpecialKey.DIRECTION_LEFT,
        SpecialKey.DIRECTION_RIGHT,
        SpecialKey.DIRECTION_UP,
        SpecialKey.DIRECTION_DOWN -> flushCommonFSM(state)
        SpecialKey.SHIFT, SpecialKey.SHIFT_LOCK -> InputResult("", state.toComposingString(), state)
        SpecialKey.MODE_SWITCH -> flushCommonFSM(state)
        SpecialKey.STROKE_ADD -> InputResult("", state.toComposingString(), state) // 레이아웃별 처리
    }
}
