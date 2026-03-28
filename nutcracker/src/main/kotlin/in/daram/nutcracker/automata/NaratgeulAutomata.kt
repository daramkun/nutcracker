package `in`.daram.nutcracker.automata

import `in`.daram.nutcracker.*

/**
 * §5. KT 나랏글 오토마타
 *
 * 자음 6개 + 모음 4개 키 구성 3×4 키패드.
 * `*`(획추가): 자음 파생 또는 이중모음 파생
 * `#`(쌍자음): 자음 → 쌍자음 변환
 * 모음 `3`/`6`: 1회=기본, 2회=파생 (타임아웃/다른 키 시 확정)
 *
 * SyllableState.cycleKey: 현재 순환 중인 모음/자음 키 ('3' 또는 '6')
 * SyllableState.cycleCount: 순환 횟수
 */
class NaratgeulAutomata : HangulAutomata {
    override val layoutName = "KT 나랏글"

    companion object {
        // §5.3 자음 파생 테이블 (파생 자음 포함, 획추가 순환)
        private val CONSONANT_DERIVE = mapOf(
            'ㄱ' to listOf('ㄱ', 'ㅋ'), 'ㅋ' to listOf('ㄱ', 'ㅋ'),
            'ㄴ' to listOf('ㄴ', 'ㄷ', 'ㅌ'), 'ㄷ' to listOf('ㄴ', 'ㄷ', 'ㅌ'), 'ㅌ' to listOf('ㄴ', 'ㄷ', 'ㅌ'),
            'ㄹ' to listOf('ㄹ'),
            'ㅁ' to listOf('ㅁ', 'ㅂ', 'ㅍ'), 'ㅂ' to listOf('ㅁ', 'ㅂ', 'ㅍ'), 'ㅍ' to listOf('ㅁ', 'ㅂ', 'ㅍ'),
            'ㅅ' to listOf('ㅅ', 'ㅈ', 'ㅊ'), 'ㅈ' to listOf('ㅅ', 'ㅈ', 'ㅊ'), 'ㅊ' to listOf('ㅅ', 'ㅈ', 'ㅊ'),
            'ㅇ' to listOf('ㅇ', 'ㅎ'), 'ㅎ' to listOf('ㅇ', 'ㅎ'),
        )

        // §5.4 이중모음 파생 (기본 모음 → 획추가 후)
        private val VOWEL_DOUBLE = mapOf(
            'ㅏ' to 'ㅑ', 'ㅓ' to 'ㅕ', 'ㅗ' to 'ㅛ', 'ㅜ' to 'ㅠ',
        )

        // §5.5 나랏글 전용 복합 중성 (§1.5에 없는 추가 조합)
        private val NARATGEUL_COMPOUND_JUNG = mapOf(
            ('ㅏ' to 'ㅣ') to 'ㅐ',
            ('ㅓ' to 'ㅣ') to 'ㅔ',
            ('ㅑ' to 'ㅣ') to 'ㅒ',
            ('ㅕ' to 'ㅣ') to 'ㅖ',
        )

        // §5.6 쌍자음 변환 (#키)
        private val DOUBLE_CONSONANT = mapOf(
            'ㄱ' to 'ㄲ', 'ㄷ' to 'ㄸ', 'ㅂ' to 'ㅃ', 'ㅅ' to 'ㅆ', 'ㅈ' to 'ㅉ',
        )

        // §5.2 기본 키 매핑 (1회)
        private val BASE_KEY_MAP = mapOf(
            '1' to 'ㄱ', '2' to 'ㄴ',
            '4' to 'ㄹ', '5' to 'ㅁ',
            '7' to 'ㅅ', '8' to 'ㅇ',
            '3' to 'ㅏ',  // 1회
            '6' to 'ㅗ',  // 1회
            '9' to 'ㅣ',
            '0' to 'ㅡ',
        )

        // 모음 순환 키: 3(ㅏ→ㅓ), 6(ㅗ→ㅜ)
        private val VOWEL_CYCLE_KEY = mapOf(
            '3' to listOf('ㅏ', 'ㅓ'),
            '6' to listOf('ㅗ', 'ㅜ'),
        )
    }

    override fun process(state: SyllableState, input: KeyInput): InputResult {
        return when (input) {
            is KeyInput.Special -> handleSpecial(state, input.type)
            is KeyInput.Char -> handleChar(state, input.key)
        }
    }

    private fun handleSpecial(state: SyllableState, key: SpecialKey): InputResult {
        return when (key) {
            SpecialKey.BACKSPACE -> processBackspace(state.copy(cycleKey = null, cycleCount = 0))
            SpecialKey.STROKE_ADD -> handleStrokeAdd(state)
            else -> processSpecialCommon(state.copy(cycleKey = null, cycleCount = 0), key)
        }
    }

    /**
     * §5.7 획추가(*) 처리:
     * - 자음 조합 중(S1/S3 상태에서 jong 또는 cho가 획추가 가능): 자음 파생
     * - 모음 조합 중(cycleKey가 모음 키): 이중모음 파생
     * - 확정 후: 무시
     */
    private fun handleStrokeAdd(state: SyllableState): InputResult {
        // 모음 순환 중 (미확정 모음)
        if (state.cycleKey in VOWEL_CYCLE_KEY) {
            val currentVowel = VOWEL_CYCLE_KEY[state.cycleKey!!]!![(state.cycleCount - 1) % 2]
            val doubled = VOWEL_DOUBLE[currentVowel]
            if (doubled != null) {
                // 현재 모음을 이중모음으로 교체
                val backResult = processBackspace(state.copy(cycleKey = null, cycleCount = 0))
                val r = processCommonFSM(backResult.newState, doubled, false, true)
                return InputResult(r.committed, r.composing, r.newState.copy(cycleKey = null, cycleCount = 0))
            }
            return InputResult("", state.toComposingString(), state)
        }

        // 자음 조합 중: FSM 상태에서 현재 자음을 파생
        val currentConsonant: Char? = when (state.fsm) {
            FSMState.S1 -> state.cho
            FSMState.S3, FSMState.S3D -> state.jong  // 종성 자음 파생
            else -> null
        }

        if (currentConsonant != null) {
            val deriveList = CONSONANT_DERIVE[currentConsonant]
            if (deriveList != null && deriveList.size > 1) {
                val currentIdx = deriveList.indexOf(currentConsonant)
                val nextJamo = deriveList[(currentIdx + 1) % deriveList.size]
                // 현재 자음을 되돌리고 파생 자음으로 교체
                val backResult = processBackspace(state)
                val r = processCommonFSM(backResult.newState, nextJamo, true, false)
                return InputResult(r.committed, r.composing, r.newState)
            }
        }

        // 무효
        return InputResult("", state.toComposingString(), state)
    }

    private fun handleChar(state: SyllableState, key: Char): InputResult {
        // 모음 순환 키 처리
        VOWEL_CYCLE_KEY[key]?.let { cycleList ->
            return if (state.cycleKey == key) {
                // 동일 키 2회: 다음 모음으로 전환 (index = (nextCount-1) % size)
                val nextCount = state.cycleCount + 1
                val nextVowel = cycleList[(nextCount - 1) % cycleList.size]
                // 이전 모음을 되돌리고 새 모음으로
                val backResult = processBackspace(state.copy(cycleKey = null, cycleCount = 0))
                val r = processCommonFSM(backResult.newState, nextVowel, false, true)
                InputResult(r.committed, r.composing, r.newState.copy(cycleKey = key, cycleCount = nextCount))
            } else {
                // 새 모음 키
                val s = state.copy(cycleKey = null, cycleCount = 0)
                val vowel = cycleList[0]
                val r = processCommonFSM(s, vowel, false, true)
                InputResult(r.committed, r.composing, r.newState.copy(cycleKey = key, cycleCount = 1))
            }
        }

        // 획추가(*) 키
        if (key == '*') {
            return handleStrokeAdd(state)
        }

        // 쌍자음(#) 키
        if (key == '#') {
            return handleDoubleConsonant(state)
        }

        // 일반 자음/모음 키
        val jamo = BASE_KEY_MAP[key]
            ?: run {
                val flushed = flushCommonFSM(state.copy(cycleKey = null, cycleCount = 0))
                return InputResult(flushed.committed + key, "", flushed.newState)
            }

        val s = state.copy(cycleKey = null, cycleCount = 0)
        val isConsonant = jamo in CONSONANTS
        val isVowel = jamo in VOWELS

        // §5.5 나랏글 전용 복합 중성 처리 (§1.5에 없는 ㅏ+ㅣ=ㅐ 등)
        if (isVowel) {
            if (s.fsm == FSMState.S2) {
                val naratgeulCompound = NARATGEUL_COMPOUND_JUNG[s.jung!! to jamo]
                if (naratgeulCompound != null) {
                    val newState = s.copy(jung = naratgeulCompound)
                    return InputResult("", compose(s.cho!!, naratgeulCompound).toString(), newState)
                }
            } else if (s.fsm == FSMState.S4) {
                val naratgeulCompound = NARATGEUL_COMPOUND_JUNG[s.jung!! to jamo]
                if (naratgeulCompound != null) {
                    val newState = s.copy(jung = naratgeulCompound)
                    return InputResult("", compose('ㅇ', naratgeulCompound).toString(), newState)
                }
            }
        }

        val r = processCommonFSM(s, jamo, isConsonant, isVowel)
        return InputResult(r.committed, r.composing, r.newState)
    }

    /** §5.6 쌍자음(#) 처리 */
    private fun handleDoubleConsonant(state: SyllableState): InputResult {
        val consonant: Char? = when (state.fsm) {
            FSMState.S1 -> state.cho
            FSMState.S3 -> state.jong
            else -> null
        }
        if (consonant != null) {
            val doubled = DOUBLE_CONSONANT[consonant]
            if (doubled != null) {
                val backResult = processBackspace(state.copy(cycleKey = null, cycleCount = 0))
                val r = processCommonFSM(backResult.newState, doubled, true, false)
                return InputResult(r.committed, r.composing, r.newState)
            }
        }
        return InputResult("", state.toComposingString(), state)
    }

    override fun flush(state: SyllableState): InputResult =
        flushCommonFSM(state.copy(cycleKey = null, cycleCount = 0))
}
