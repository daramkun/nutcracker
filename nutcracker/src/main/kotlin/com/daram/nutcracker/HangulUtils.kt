package com.daram.nutcracker

// §1-2. 초성 인덱스 (19개)
private val CHOSEONG = charArrayOf(
    'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ',
    'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
)

// §1-3. 중성 인덱스 (21개)
private val JUNGSEONG = charArrayOf(
    'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ',
    'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
)

// §1-4. 종성 인덱스 (28개, 0 = 없음)
private val JONGSEONG = charArrayOf(
    '\u0000', // 0: 없음
    'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ',
    'ㄺ', 'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ',
    'ㅄ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
)

// §1-5. 복합 중성 조합 테이블
private val COMPOUND_JUNGSEONG = mapOf(
    ('ㅗ' to 'ㅏ') to 'ㅘ',
    ('ㅗ' to 'ㅐ') to 'ㅙ',
    ('ㅗ' to 'ㅣ') to 'ㅚ',
    ('ㅜ' to 'ㅓ') to 'ㅝ',
    ('ㅜ' to 'ㅔ') to 'ㅞ',
    ('ㅜ' to 'ㅣ') to 'ㅟ',
    ('ㅡ' to 'ㅣ') to 'ㅢ',
)

// §1-6. 겹받침 조합 테이블
private val COMPOUND_JONGSEONG = mapOf(
    ('ㄱ' to 'ㅅ') to 'ㄳ',
    ('ㄴ' to 'ㅈ') to 'ㄵ',
    ('ㄴ' to 'ㅎ') to 'ㄶ',
    ('ㄹ' to 'ㄱ') to 'ㄺ',
    ('ㄹ' to 'ㅁ') to 'ㄻ',
    ('ㄹ' to 'ㅂ') to 'ㄼ',
    ('ㄹ' to 'ㅅ') to 'ㄽ',
    ('ㄹ' to 'ㅌ') to 'ㄾ',
    ('ㄹ' to 'ㅍ') to 'ㄿ',
    ('ㄹ' to 'ㅎ') to 'ㅀ',
    ('ㅂ' to 'ㅅ') to 'ㅄ',
)

// 겹받침 분리 테이블 (겹받침 → 왼쪽, 오른쪽)
private val SPLIT_JONGSEONG = mapOf(
    'ㄳ' to ('ㄱ' to 'ㅅ'),
    'ㄵ' to ('ㄴ' to 'ㅈ'),
    'ㄶ' to ('ㄴ' to 'ㅎ'),
    'ㄺ' to ('ㄹ' to 'ㄱ'),
    'ㄻ' to ('ㄹ' to 'ㅁ'),
    'ㄼ' to ('ㄹ' to 'ㅂ'),
    'ㄽ' to ('ㄹ' to 'ㅅ'),
    'ㄾ' to ('ㄹ' to 'ㅌ'),
    'ㄿ' to ('ㄹ' to 'ㅍ'),
    'ㅀ' to ('ㄹ' to 'ㅎ'),
    'ㅄ' to ('ㅂ' to 'ㅅ'),
)

/** §1-1. 유니코드 음절 조합 */
fun compose(cho: Char, jung: Char, jong: Char? = null): Char {
    val choIdx = CHOSEONG.indexOf(cho)
    val jungIdx = JUNGSEONG.indexOf(jung)
    val jongIdx = if (jong != null) JONGSEONG.indexOf(jong) else 0
    require(choIdx >= 0) { "Invalid choseong: $cho" }
    require(jungIdx >= 0) { "Invalid jungseong: $jung" }
    require(jongIdx >= 0) { "Invalid jongseong: $jong" }
    return (0xAC00 + (choIdx * 21 + jungIdx) * 28 + jongIdx).toChar()
}

/** §1-1. 유니코드 음절 분해 */
fun decompose(syllable: Char): Triple<Char, Char, Char?> {
    val code = syllable.code - 0xAC00
    require(code in 0..(0xD7A3 - 0xAC00)) { "Not a Korean syllable: $syllable" }
    val jongIdx = code % 28
    val jungIdx = (code / 28) % 21
    val choIdx = code / 28 / 21
    val jong = if (jongIdx == 0) null else JONGSEONG[jongIdx]
    return Triple(CHOSEONG[choIdx], JUNGSEONG[jungIdx], jong)
}

/** §1-5. 복합 중성 조합 시도 */
fun compoundJungseong(first: Char, second: Char): Char? = COMPOUND_JUNGSEONG[first to second]

/** §1-6. 겹받침 조합 시도 */
fun compoundJongseong(first: Char, second: Char): Char? = COMPOUND_JONGSEONG[first to second]

/** 겹받침 분리 (왼쪽 자모, 오른쪽 자모) */
fun splitJongseong(jong: Char): Pair<Char, Char>? = SPLIT_JONGSEONG[jong]

/** 조합 중인 SyllableState를 화면 표시용 문자열로 변환 */
fun SyllableState.toComposingString(): String = when (fsm) {
    FSMState.S0 -> ""
    FSMState.S1 -> cho!!.toString()
    FSMState.S2 -> compose(cho!!, jung!!).toString()
    FSMState.S3 -> compose(cho!!, jung!!, jong).toString()
    FSMState.S3D -> compose(cho!!, jung!!, compoundJongseong(jong!!, jong2!!)!!).toString()
    FSMState.S4 -> compose('ㅇ', jung!!).toString()
}
