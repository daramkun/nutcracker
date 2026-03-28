package com.daram.nutcracker.prediction

import com.daram.nutcracker.FSMState
import com.daram.nutcracker.decompose
import com.daram.nutcracker.splitJongseong
import kotlin.math.exp
import kotlin.math.ln

/**
 * 기본 WordPredictor 구현체.
 *
 * 동작 원리:
 * 1. committedText를 트리 검색 프리픽스로 사용한다.
 * 2. 검색 결과를 FSM 상태(cho/jung/jong)로 음절 단위 필터링한다.
 * 3. 번들 사전과 사용자 학습 점수를 합산해 최종 score를 계산한다.
 * 4. 각 후보의 nextJamos를 계산해 KeyMapper 역매핑에 쓸 수 있도록 제공한다.
 *
 * [dictionaries]    - 언어별 PredictionDictionary. 같은 언어 여러 개면 병합 검색.
 * [learningDelegate]- 사용자 학습 위임 객체. null이면 학습 기능 비활성화.
 * [userWeight]      - 사용자 단어 점수 부스트 계수
 * [recencyWeight]   - 최근 사용 점수 부스트 계수
 * [recencyHalfLifeMs] - 최근성 감쇠 반감기 (기본 30일)
 */
class DefaultWordPredictor(
    private val dictionaries: List<PredictionDictionary>,
    private val learningDelegate: UserLearningDelegate? = null,
    private val userWeight: Float = 2.0f,
    private val recencyWeight: Float = 0.5f,
    private val recencyHalfLifeMs: Long = 30L * 24 * 60 * 60 * 1000,
) : WordPredictor {

    // 사용자 학습 단어 인메모리 캐시 (word → entry)
    private val userWords: MutableMap<String, UserWordEntry> =
        learningDelegate?.loadUserWords()?.associateBy { it.word }?.toMutableMap()
            ?: mutableMapOf()

    override fun predict(query: PredictionQuery): List<PredictionCandidate> {
        val dict = dictionaries.filter { it.language == query.language }
        if (dict.isEmpty()) return emptyList()

        // committedText를 프리픽스로 사용: S0는 그대로, S1/S2/S3은 이후 필터링으로 처리
        val prefix = query.committedText

        // 아무것도 입력되지 않은 상태에서는 예측 없음
        if (prefix.isEmpty() && query.composingState.fsm == FSMState.S0) return emptyList()

        val searchLimit = if (prefix.isEmpty()) query.maxResults * 20 else query.maxResults * 5

        // 번들 사전 검색
        val rawResults: List<Pair<String, Float>> = dict
            .flatMap { it.search(prefix, searchLimit) }
            .sortedByDescending { it.second }
            .distinctBy { it.first }

        // 사용자 학습 단어 검색 (committedText로 시작하는 것만)
        val userResults: List<Pair<String, Float>> = userWords.values
            .filter { it.language == query.language }
            .filter { it.word.startsWith(prefix) }
            .map { it.word to computeUserScore(it) }

        // 병합 및 score 계산
        val merged = (rawResults + userResults)
            .groupBy { it.first }
            .map { (word, entries) ->
                val baseScore = entries.maxOf { it.second }
                val userEntry = userWords[word]
                val finalScore = computeFinalScore(baseScore, userEntry)
                word to finalScore
            }
            .sortedByDescending { it.second }

        // FSM 상태에 따라 조합 중인 음절 위치에서 필터링
        val filtered = filterByComposingState(merged, query)

        return filtered.take(query.maxResults).map { (word, score) ->
            val isUserWord = userWords.containsKey(word)
            val nextJamos = computeNextJamos(word, query)
            PredictionCandidate(
                word = word,
                score = score,
                language = query.language,
                isUserWord = isUserWord,
                nextJamos = nextJamos,
            )
        }
    }

    override fun nextKeyHints(
        candidates: List<PredictionCandidate>,
        mapper: KeyMapper,
    ): NextKeyHint {
        if (candidates.isEmpty()) return NextKeyHint(mapper.layoutName, emptyMap())

        val keyScores = mutableMapOf<Char, Float>()
        for (candidate in candidates) {
            val nextJamo = candidate.nextJamos.firstOrNull() ?: continue
            val keys = mapper.firstKeysFor(nextJamo)
            for (key in keys) {
                keyScores[key] = (keyScores[key] ?: 0f) + candidate.score
            }
        }

        if (keyScores.isEmpty()) return NextKeyHint(mapper.layoutName, emptyMap())

        // 0.0~1.0으로 정규화
        val maxScore = keyScores.values.max()
        val normalized = keyScores.mapValues { (_, v) -> v / maxScore }
        return NextKeyHint(mapper.layoutName, normalized)
    }

    override fun onCandidateSelected(word: String, language: InputLanguage, context: String) {
        updateUserWord(word, language)
    }

    override fun onWordCommitted(word: String, language: InputLanguage, context: String) {
        updateUserWord(word, language)
    }

    // ── 내부 헬퍼 ──────────────────────────────────────────────────────────────

    /**
     * FSM 상태에 따라 committedText 위치(idx)의 음절을 검사해 후보를 필터링한다.
     *
     * S0          → 필터 없음
     * S1 (초성)   → 해당 위치 음절의 초성이 state.cho와 일치해야 함
     * S2/S4       → 초성 일치 + 중성이 state.jung과 호환(동일하거나 복합모음 접두)되어야 함
     * S3/S3D (받침) → S2 조건 + 받침이 state.jong과 일치하거나 겹받침 접두이거나
     *                 받침 없이 다음 음절 초성이 state.jong인 경우도 허용
     */
    private fun filterByComposingState(
        candidates: List<Pair<String, Float>>,
        query: PredictionQuery,
    ): List<Pair<String, Float>> {
        val state = query.composingState
        val idx = query.committedText.length

        return when (state.fsm) {
            FSMState.S0 -> candidates

            FSMState.S1 -> {
                val cho = state.cho ?: return candidates
                candidates.filter { (word, _) ->
                    val syllable = word.getOrNull(idx) ?: return@filter false
                    if (syllable in '\uAC00'..'\uD7A3') {
                        decompose(syllable).first == cho
                    } else {
                        syllable == cho
                    }
                }
            }

            FSMState.S2 -> {
                val cho = state.cho ?: return candidates
                val jung = state.jung ?: return candidates
                candidates.filter { (word, _) ->
                    val syllable = word.getOrNull(idx) ?: return@filter false
                    if (syllable !in '\uAC00'..'\uD7A3') return@filter false
                    val (sCho, sJung, _) = decompose(syllable)
                    sCho == cho && isJungCompatible(jung, sJung)
                }
            }

            FSMState.S4 -> {
                val jung = state.jung ?: return candidates
                candidates.filter { (word, _) ->
                    val syllable = word.getOrNull(idx) ?: return@filter false
                    if (syllable !in '\uAC00'..'\uD7A3') return@filter false
                    val (sCho, sJung, _) = decompose(syllable)
                    sCho == 'ㅇ' && isJungCompatible(jung, sJung)
                }
            }

            FSMState.S3, FSMState.S3D -> {
                val cho = state.cho ?: return candidates
                val jung = state.jung ?: return candidates
                val jong = state.jong
                candidates.filter { (word, _) ->
                    val syllable = word.getOrNull(idx) ?: return@filter false
                    if (syllable !in '\uAC00'..'\uD7A3') return@filter false
                    val (sCho, sJung, sJong) = decompose(syllable)
                    if (sCho != cho || !isJungCompatible(jung, sJung)) return@filter false
                    if (jong == null) return@filter true
                    // 받침 일치: 동일, 겹받침의 첫 번째, 또는 받침 없이 다음 음절 초성과 일치
                    sJong == jong ||
                        (sJong != null && splitJongseong(sJong)?.first == jong) ||
                        (sJong == null && word.getOrNull(idx + 1)?.let { next ->
                            if (next in '\uAC00'..'\uD7A3') decompose(next).first == jong
                            else next == jong
                        } == true)
                }
            }
        }
    }

    /**
     * stateJung과 wordJung의 호환 여부.
     * 동일하거나, wordJung이 stateJung으로 시작하는 복합 중성인 경우 true.
     */
    private fun isJungCompatible(stateJung: Char, wordJung: Char): Boolean {
        if (stateJung == wordJung) return true
        return COMPOUND_JUNG_STARTS[stateJung]?.contains(wordJung) == true
    }

    /**
     * 후보 단어에서 현재 입력 이후 필요한 자모/문자 목록을 계산한다.
     *
     * 한국어: FSM 상태에 따라 조합 중인 음절에서 이미 입력된 자모를 건너뛰고,
     * 아직 입력되지 않은 자모부터 시작한다.
     *   - S1: 초성 입력됨 → 조합 음절의 [중성, 종성들] + 이후 음절들
     *   - S2/S4: 초성+중성(또는 모음만) 입력됨 → 조합 음절의 [종성들] + 이후 음절들
     *   - S3: 홑받침까지 입력됨 → 겹받침이면 [두 번째 종성], 단어에 종성 없으면 다음 음절 [중성, 종성들]
     *   - S3D: 겹받침까지 입력됨 → 이후 음절들만
     * 영어/기타: 문자 그대로.
     */
    private fun computeNextJamos(word: String, query: PredictionQuery): List<Char> {
        val committedLen = query.committedText.length
        val state = query.composingState
        val language = query.language

        if (language != InputLanguage.KOREAN) {
            val currentLength = when (state.fsm) {
                FSMState.S0 -> committedLen
                else -> if (query.composingText.isNotEmpty()) committedLen + 1 else committedLen
            }
            return if (currentLength >= word.length) emptyList() else word.drop(currentLength).toList()
        }

        if (committedLen >= word.length) return emptyList()

        val result = mutableListOf<Char>()
        val composingIdx = committedLen
        val composingSyllable = word.getOrNull(composingIdx)

        if (state.fsm != FSMState.S0 && composingSyllable != null && composingSyllable in '\uAC00'..'\uD7A3') {
            val (_, sJung, sJong) = decompose(composingSyllable)

            when (state.fsm) {
                FSMState.S1 -> {
                    // 초성 입력됨: 중성 + 종성들
                    result.add(sJung)
                    if (sJong != null) appendJong(result, sJong)
                }
                FSMState.S2, FSMState.S4 -> {
                    // 초성+중성 입력됨: 종성들만
                    if (sJong != null) appendJong(result, sJong)
                }
                FSMState.S3 -> {
                    // 홑받침까지 입력됨
                    if (sJong != null) {
                        val split = splitJongseong(sJong)
                        if (split != null) {
                            // 겹받침: 두 번째 자모만 남음
                            result.add(split.second)
                        }
                        // 단순 받침: 이미 완전히 입력됨 → 추가 없음
                    } else {
                        // 조합 음절에 종성 없음 = state.jong이 다음 음절 초성으로 이동하는 케이스
                        // 다음 음절의 중성과 종성을 힌트로 제공
                        val nextSyllable = word.getOrNull(composingIdx + 1)
                        if (nextSyllable != null && nextSyllable in '\uAC00'..'\uD7A3') {
                            val (_, nJung, nJong) = decompose(nextSyllable)
                            result.add(nJung)
                            if (nJong != null) appendJong(result, nJong)
                        }
                        // 다음 음절은 이미 처리했으므로 composingIdx+2부터 이후 음절 추가
                        appendSyllables(result, word, composingIdx + 2)
                        return result
                    }
                }
                FSMState.S3D -> {
                    // 겹받침까지 모두 입력됨: 조합 음절 내 남은 자모 없음
                }
                FSMState.S0 -> { /* 조합 중 상태가 아님 */ }
            }
        }

        // 조합 음절 이후 음절들 추가
        appendSyllables(result, word, composingIdx + 1)
        return result
    }

    private fun appendJong(result: MutableList<Char>, jong: Char) {
        val split = splitJongseong(jong)
        if (split != null) {
            result.add(split.first)
            result.add(split.second)
        } else {
            result.add(jong)
        }
    }

    private fun appendSyllables(result: MutableList<Char>, word: String, fromIdx: Int) {
        for (i in fromIdx until word.length) {
            val syllable = word[i]
            if (syllable in '\uAC00'..'\uD7A3') {
                val (cho, jung, jong) = decompose(syllable)
                result.add(cho)
                result.add(jung)
                if (jong != null) appendJong(result, jong)
            } else {
                result.add(syllable)
            }
        }
    }

    private fun computeFinalScore(baseScore: Float, userEntry: UserWordEntry?): Float {
        if (userEntry == null) return baseScore
        val userBoost = ln(userEntry.useCount + 1f) * userWeight * 0.1f
        val nowMs = System.currentTimeMillis()
        val recencyBoost = exp(-(nowMs - userEntry.lastUsedMs).toFloat() / recencyHalfLifeMs) * recencyWeight
        return baseScore + userBoost + recencyBoost
    }

    private fun computeUserScore(entry: UserWordEntry): Float {
        val nowMs = System.currentTimeMillis()
        val recencyBoost = exp(-(nowMs - entry.lastUsedMs).toFloat() / recencyHalfLifeMs) * recencyWeight
        return entry.score + recencyBoost
    }

    private fun updateUserWord(word: String, language: InputLanguage) {
        val now = System.currentTimeMillis()
        val existing = userWords[word]
        val updated = if (existing != null) {
            existing.copy(
                score = (existing.score + 0.5f).coerceAtMost(10f),
                useCount = existing.useCount + 1,
                lastUsedMs = now,
            )
        } else {
            UserWordEntry(
                word = word,
                language = language,
                score = 1.0f,
                useCount = 1,
                lastUsedMs = now,
            )
        }
        userWords[word] = updated
        learningDelegate?.saveUserWord(updated)
    }

    companion object {
        // 복합 중성의 첫 번째 자모 → 해당 자모로 시작하는 복합 중성 집합
        private val COMPOUND_JUNG_STARTS: Map<Char, Set<Char>> = mapOf(
            'ㅗ' to setOf('ㅘ', 'ㅙ', 'ㅚ'),
            'ㅜ' to setOf('ㅝ', 'ㅞ', 'ㅟ'),
            'ㅡ' to setOf('ㅢ'),
        )
    }
}
