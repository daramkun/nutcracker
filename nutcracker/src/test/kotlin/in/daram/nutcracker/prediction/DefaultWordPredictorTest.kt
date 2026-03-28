package `in`.daram.nutcracker.prediction

import `in`.daram.nutcracker.FSMState
import `in`.daram.nutcracker.SyllableState
import `in`.daram.nutcracker.prediction.trie.TrieDictionary
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultWordPredictorTest {

    private lateinit var dict: TrieDictionary
    private lateinit var predictor: DefaultWordPredictor

    @BeforeEach
    fun setUp() {
        dict = TrieDictionary(InputLanguage.KOREAN)
        dict.initialize(listOf(
            WordEntry("닭", 100),
            WordEntry("달", 80),
            WordEntry("다람쥐", 70),
            WordEntry("한글", 90),
            WordEntry("한국어", 85),
            WordEntry("밥", 60),
        ))
        predictor = DefaultWordPredictor(listOf(dict))
    }

    // ── 기본 예측 ─────────────────────────────────────────────────────────────

    @Test
    fun `S0 - 빈 상태에서 예측하면 빈 리스트 반환`() {
        val query = PredictionQuery(
            committedText = "",
            composingState = SyllableState(),
            composingText = "",
            language = InputLanguage.KOREAN,
        )
        val results = predictor.predict(query)
        assertTrue(results.isEmpty())
    }

    @Test
    fun `S2 - 초성+중성 조합 중 예측 (한 입력 중)`() {
        // committedText="" composingText="한" → "한글", "한국어" 예측
        val query = PredictionQuery(
            committedText = "",
            composingState = SyllableState(fsm = FSMState.S2, cho = 'ㅎ', jung = 'ㅏ'),
            composingText = "한",
            language = InputLanguage.KOREAN,
        )
        val results = predictor.predict(query)
        val words = results.map { it.word }
        assertTrue("한글" in words || "한국어" in words, "한 으로 시작하는 단어가 예측되어야 함")
    }

    @Test
    fun `S1 - 초성만 입력 시 해당 초성으로 시작하는 음절의 단어 필터링`() {
        // S1(ㄷ) → "닭", "달", "다람쥐" 예측 (ㅂ으로 시작하는 "밥"은 제외)
        val query = PredictionQuery(
            committedText = "",
            composingState = SyllableState(fsm = FSMState.S1, cho = 'ㄷ'),
            composingText = "ㄷ",
            language = InputLanguage.KOREAN,
        )
        val results = predictor.predict(query)
        val words = results.map { it.word }
        assertTrue(words.any { it.startsWith("다") || it.startsWith("닭") || it.startsWith("달") },
            "ㄷ 초성으로 시작하는 단어가 있어야 함")
        assertFalse("밥" in words, "ㅂ 초성 단어는 제외되어야 함")
    }

    @Test
    fun `S3 - 받침 포함 상태에서 이중 프리픽스 예측 (닭 받침 처리)`() {
        // "닭" 입력 중 (받침 ㄱ 있음): 받침 포함 "닭" 프리픽스 → "닭" 예측
        val query = PredictionQuery(
            committedText = "",
            composingState = SyllableState(fsm = FSMState.S3, cho = 'ㄷ', jung = 'ㅏ', jong = 'ㄹ'),
            composingText = "달",
            language = InputLanguage.KOREAN,
        )
        val results = predictor.predict(query)
        val words = results.map { it.word }
        // "달" 프리픽스로 "달" 자체, 받침 없는 "다"로도 검색
        assertTrue(words.isNotEmpty(), "S3 상태에서 예측 결과가 있어야 함")
    }

    // ── nextJamos 검증 ───────────────────────────────────────────────────────

    @Test
    fun `nextJamos - 닭 예측 시 S1(ㄷ) 에서 ㅏ,ㄹ,ㄱ 반환`() {
        val query = PredictionQuery(
            committedText = "",
            composingState = SyllableState(fsm = FSMState.S1, cho = 'ㄷ'),
            composingText = "ㄷ",
            language = InputLanguage.KOREAN,
        )
        val results = predictor.predict(query)
        val dak = results.firstOrNull { it.word == "닭" }
        assertNotNull(dak, "닭이 예측 결과에 있어야 함")
        // S1에서 currentLength=0이므로 닭(ㄷ+ㅏ+ㄹ+ㄱ) 전체 분해
        assertEquals(listOf('ㄷ', 'ㅏ', 'ㄹ', 'ㄱ'), dak!!.nextJamos)
    }

    @Test
    fun `nextJamos - 한글 예측 시 S2(한) 에서 ㄱ,ㅡ,ㄹ 반환`() {
        val query = PredictionQuery(
            committedText = "",
            composingState = SyllableState(fsm = FSMState.S2, cho = 'ㅎ', jung = 'ㅏ'),
            composingText = "한",
            language = InputLanguage.KOREAN,
        )
        val results = predictor.predict(query)
        val hangul = results.firstOrNull { it.word == "한글" }
        assertNotNull(hangul, "한글이 예측 결과에 있어야 함")
        // currentLength=1 (한 포함), 나머지 글(ㄱ+ㅡ+ㄹ)
        assertEquals(listOf('ㄱ', 'ㅡ', 'ㄹ'), hangul!!.nextJamos)
    }

    @Test
    fun `nextJamos - 이미 완전히 입력된 단어면 빈 리스트 반환`() {
        val query = PredictionQuery(
            committedText = "한글",
            composingState = SyllableState(),
            composingText = "",
            language = InputLanguage.KOREAN,
        )
        val results = predictor.predict(query)
        val hangul = results.firstOrNull { it.word == "한글" }
        if (hangul != null) {
            assertTrue(hangul.nextJamos.isEmpty())
        }
    }

    // ── 영어 예측 ─────────────────────────────────────────────────────────────

    @Test
    fun `영어 사전으로 영어 예측`() {
        val engDict = TrieDictionary(InputLanguage.ENGLISH)
        engDict.initialize(listOf(
            WordEntry("hello", 100),
            WordEntry("help", 80),
            WordEntry("world", 60),
        ))
        val engPredictor = DefaultWordPredictor(listOf(engDict))
        val query = PredictionQuery(
            committedText = "hel",
            composingState = SyllableState(),
            composingText = "",
            language = InputLanguage.ENGLISH,
        )
        val results = engPredictor.predict(query)
        val words = results.map { it.word }
        assertTrue("hello" in words)
        assertTrue("help" in words)
        assertFalse("world" in words)
    }

    @Test
    fun `영어 예측 nextJamos는 알파벳 문자 그대로 반환`() {
        val engDict = TrieDictionary(InputLanguage.ENGLISH)
        engDict.initialize(listOf(WordEntry("hello", 100)))
        val engPredictor = DefaultWordPredictor(listOf(engDict))
        val query = PredictionQuery(
            committedText = "hel",
            composingState = SyllableState(),
            composingText = "",
            language = InputLanguage.ENGLISH,
        )
        val results = engPredictor.predict(query)
        val hello = results.firstOrNull { it.word == "hello" }
        assertNotNull(hello)
        // "hello".drop(3) = "lo" → ['l', 'o']
        assertEquals(listOf('l', 'o'), hello!!.nextJamos)
    }

    // ── 사용자 학습 ──────────────────────────────────────────────────────────

    @Test
    fun `onCandidateSelected 후 사용자 단어 score 갱신`() {
        predictor.onCandidateSelected("닭", InputLanguage.KOREAN)
        predictor.onCandidateSelected("닭", InputLanguage.KOREAN)

        val query = PredictionQuery(
            committedText = "",
            composingState = SyllableState(fsm = FSMState.S2, cho = 'ㄷ', jung = 'ㅏ'),
            composingText = "다",
            language = InputLanguage.KOREAN,
        )
        val results = predictor.predict(query)
        val dak = results.firstOrNull { it.word == "닭" }
        assertNotNull(dak)
        assertTrue(dak!!.isUserWord, "사용자 단어로 표시되어야 함")
    }

    @Test
    fun `onWordCommitted - 번들에 없는 신규 단어 등록 후 예측됨`() {
        predictor.onWordCommitted("다람쥐밥", InputLanguage.KOREAN)

        val query = PredictionQuery(
            committedText = "다람",
            composingState = SyllableState(fsm = FSMState.S2, cho = 'ㅈ', jung = 'ㅜ'),
            composingText = "주",
            language = InputLanguage.KOREAN,
        )
        val results = predictor.predict(query)
        val words = results.map { it.word }
        assertTrue("다람쥐밥" in words, "신규 등록 단어가 예측되어야 함")
    }

    // ── nextKeyHints ─────────────────────────────────────────────────────────

    @Test
    fun `nextKeyHints - 두벌식에서 ㅏ 다음 키는 k`() {
        val candidates = listOf(
            PredictionCandidate("닭", 0.9f, InputLanguage.KOREAN, false, listOf('ㅏ', 'ㄹ', 'ㄱ')),
        )
        val mapper = `in`.daram.nutcracker.prediction.mapper.DubeolsikKeyMapper()
        val hints = predictor.nextKeyHints(candidates, mapper)
        assertTrue(hints.keyHints.containsKey('k'), "ㅏ 다음 키는 두벌식에서 k")
        assertEquals(1.0f, hints.keyHints['k']!!, 0.001f)
    }

    @Test
    fun `nextKeyHints - SKY-II에서 ㄱ 다음 키는 1`() {
        val candidates = listOf(
            PredictionCandidate("가", 0.9f, InputLanguage.KOREAN, false, listOf('ㄱ', 'ㅏ')),
        )
        val mapper = `in`.daram.nutcracker.prediction.mapper.SkyIIKeyMapper()
        val hints = predictor.nextKeyHints(candidates, mapper)
        assertTrue(hints.keyHints.containsKey('1'), "ㄱ 다음 키는 SKY-II에서 1")
    }

    @Test
    fun `nextKeyHints - 후보 없으면 빈 힌트 반환`() {
        val mapper = `in`.daram.nutcracker.prediction.mapper.DubeolsikKeyMapper()
        val hints = predictor.nextKeyHints(emptyList(), mapper)
        assertTrue(hints.keyHints.isEmpty())
    }
}
