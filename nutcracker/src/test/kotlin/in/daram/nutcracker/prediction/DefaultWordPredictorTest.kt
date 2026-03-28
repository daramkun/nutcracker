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
        // S1: 초성(ㄷ) 입력됨 → 중성+종성들만 [ㅏ, ㄹ, ㄱ]
        assertEquals(listOf('ㅏ', 'ㄹ', 'ㄱ'), dak!!.nextJamos)
    }

    @Test
    fun `nextJamos - 한글 예측 시 S2(ㅎ+ㅏ) 에서 ㄴ,ㄱ,ㅡ,ㄹ 반환`() {
        val query = PredictionQuery(
            committedText = "",
            composingState = SyllableState(fsm = FSMState.S2, cho = 'ㅎ', jung = 'ㅏ'),
            composingText = "하",
            language = InputLanguage.KOREAN,
        )
        val results = predictor.predict(query)
        val hangul = results.firstOrNull { it.word == "한글" }
        assertNotNull(hangul, "한글이 예측 결과에 있어야 함")
        // S2: 초성+중성(ㅎ+ㅏ) 입력됨 → 조합 음절 한(ㅎ+ㅏ+ㄴ)의 종성 ㄴ + 글(ㄱ+ㅡ+ㄹ)
        assertEquals(listOf('ㄴ', 'ㄱ', 'ㅡ', 'ㄹ'), hangul!!.nextJamos)
    }

    @Test
    fun `nextJamos - 닭 예측 시 S2(ㄷ+ㅏ) 에서 종성 ㄹ,ㄱ 반환`() {
        val query = PredictionQuery(
            committedText = "",
            composingState = SyllableState(fsm = FSMState.S2, cho = 'ㄷ', jung = 'ㅏ'),
            composingText = "다",
            language = InputLanguage.KOREAN,
        )
        val results = predictor.predict(query)
        val dak = results.firstOrNull { it.word == "닭" }
        assertNotNull(dak, "닭이 예측 결과에 있어야 함")
        // S2: 초성+중성(ㄷ+ㅏ) 입력됨 → 조합 음절 닭(ㄷ+ㅏ+ㄺ)의 겹받침 [ㄹ, ㄱ]
        assertEquals(listOf('ㄹ', 'ㄱ'), dak!!.nextJamos)
    }

    @Test
    fun `nextJamos - 닭 예측 시 S3(ㄷ+ㅏ+ㄹ) 에서 겹받침 두 번째 ㄱ 반환`() {
        val query = PredictionQuery(
            committedText = "",
            composingState = SyllableState(fsm = FSMState.S3, cho = 'ㄷ', jung = 'ㅏ', jong = 'ㄹ'),
            composingText = "달",
            language = InputLanguage.KOREAN,
        )
        val results = predictor.predict(query)
        val dak = results.firstOrNull { it.word == "닭" }
        assertNotNull(dak, "닭이 예측 결과에 있어야 함")
        // S3: ㄷ+ㅏ+ㄹ 입력됨 → 닭의 겹받침 ㄺ 중 두 번째 자모 [ㄱ]
        assertEquals(listOf('ㄱ'), dak!!.nextJamos)
    }

    @Test
    fun `nextJamos - 다람쥐 예측 시 S3(ㄷ+ㅏ+ㄹ) 에서 종성이 다음 초성으로 이동`() {
        val query = PredictionQuery(
            committedText = "",
            composingState = SyllableState(fsm = FSMState.S3, cho = 'ㄷ', jung = 'ㅏ', jong = 'ㄹ'),
            composingText = "달",
            language = InputLanguage.KOREAN,
        )
        val results = predictor.predict(query)
        val daramjwi = results.firstOrNull { it.word == "다람쥐" }
        assertNotNull(daramjwi, "다람쥐가 예측 결과에 있어야 함")
        // S3(ㄷ+ㅏ+ㄹ): "다"는 종성 없음 → ㄹ이 "람"의 초성이 됨
        // 다음 입력: "람"의 중성 ㅏ + 종성 ㅁ + "쥐"(ㅈ+ㅜ+ㅣ? 실제 쥐=ㅈ+ㅜ+ㅣ 아니고 ㅈ+ㅜ)
        val jamos = daramjwi!!.nextJamos
        assertTrue(jamos.isNotEmpty(), "다람쥐 nextJamos는 비어있지 않아야 함")
        assertEquals('ㅏ', jamos[0], "다음 입력은 람의 중성 ㅏ여야 함")
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

    @Test
    fun `S4 - 모음만 입력 시 ㅇ 초성 단어만 예측`() {
        val dict2 = TrieDictionary(InputLanguage.KOREAN)
        dict2.initialize(listOf(
            WordEntry("아이", 100),
            WordEntry("안녕", 90),
            WordEntry("바나나", 80),
        ))
        val pred2 = DefaultWordPredictor(listOf(dict2))
        val query = PredictionQuery(
            committedText = "",
            composingState = SyllableState(fsm = FSMState.S4, jung = 'ㅏ'),
            composingText = "아",
            language = InputLanguage.KOREAN,
        )
        val results = pred2.predict(query)
        val words = results.map { it.word }
        // ㅏ 중성 + ㅇ 초성: 아이(아=ㅇ+ㅏ) ✓, 안녕(안=ㅇ+ㅏ+ㄴ) ✓, 바나나(바=ㅂ+ㅏ) ✗
        assertTrue("아이" in words, "아이는 ㅇ+ㅏ 초성 → 예측되어야 함")
        assertTrue("안녕" in words, "안녕은 ㅇ+ㅏ+ㄴ → 예측되어야 함")
        assertFalse("바나나" in words, "바나나는 ㅂ 초성 → 제외되어야 함")
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
