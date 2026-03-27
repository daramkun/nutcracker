package `in`.daram.nutcracker.prediction.trie

import `in`.daram.nutcracker.prediction.InputLanguage
import `in`.daram.nutcracker.prediction.PredictionDictionary
import `in`.daram.nutcracker.prediction.WordEntry

/**
 * 음절(한국어) 또는 문자(영어 등) 단위 In-Memory Trie 사전.
 *
 * - 한국어: 유니코드 음절 문자(가~힣)를 브랜치 단위로 사용 → 트리 깊이 2~5
 * - 영어: 알파벳 문자를 브랜치 단위로 사용
 * - initialize()로 단어 목록을 주입받아 초기화; frequency를 0.0~1.0 score로 정규화
 */
class TrieDictionary(override val language: InputLanguage) : PredictionDictionary {

    private var root = TrieNode()
    private var wordCount = 0

    override val size: Int get() = wordCount

    override fun initialize(words: List<WordEntry>) {
        root = TrieNode()
        wordCount = 0
        if (words.isEmpty()) return
        val maxFreq = words.maxOf { it.frequency }.toFloat().coerceAtLeast(1f)
        for (entry in words) {
            if (entry.word.isNotEmpty() && entry.frequency > 0) {
                insert(entry.word, entry.frequency / maxFreq)
            }
        }
    }

    override fun add(word: String, frequency: Int) {
        if (word.isEmpty()) return
        if (frequency <= 0) {
            remove(word)
            return
        }
        // 기존 최대 score를 기준으로 정규화하기 어려우므로 단순 삽입 (score = frequency clamp 1.0)
        val score = (frequency / 1000f).coerceIn(0.001f, 1.0f)
        val existing = findNode(word)
        if (existing?.terminalScore != null) {
            existing.terminalScore = score
        } else {
            insert(word, score)
        }
    }

    override fun search(prefix: String, limit: Int): List<Pair<String, Float>> {
        val node = if (prefix.isEmpty()) root else (findNode(prefix) ?: return emptyList())
        val results = mutableListOf<Pair<String, Float>>()
        collectTopK(node, prefix, results, limit)
        results.sortByDescending { it.second }
        return results
    }

    private fun insert(word: String, score: Float) {
        var node = root
        for (ch in word) {
            node = node.children.getOrPut(ch) { TrieNode() }
        }
        if (node.terminalScore == null) wordCount++
        node.terminalScore = score
    }

    private fun remove(word: String) {
        val node = findNode(word) ?: return
        if (node.terminalScore != null) {
            node.terminalScore = null
            wordCount--
        }
    }

    private fun findNode(prefix: String): TrieNode? {
        var node = root
        for (ch in prefix) {
            node = node.children[ch] ?: return null
        }
        return node
    }

    /**
     * [node]에서 DFS로 최대 [limit]개의 단어를 수집한다.
     * 가지치기: subtree 전체를 탐색하지 않도록 limit에 도달하면 조기 종료.
     */
    private fun collectTopK(
        node: TrieNode,
        current: String,
        results: MutableList<Pair<String, Float>>,
        limit: Int,
    ) {
        if (results.size >= limit) return
        node.terminalScore?.let { results.add(current to it) }
        if (results.size >= limit) return
        // children을 score 내림차순으로 탐색 (가장 높은 score 쪽 먼저)
        val sorted = node.children.entries.sortedByDescending { (_, child) -> maxScoreInSubtree(child) }
        for ((ch, child) in sorted) {
            if (results.size >= limit) return
            collectTopK(child, current + ch, results, limit)
        }
    }

    /**
     * 서브트리에서 가장 높은 terminal score를 반환한다 (탐색 순서 최적화용).
     * 재귀 깊이가 깊어질 수 있으므로 캐싱이 없는 간단한 구현.
     */
    private fun maxScoreInSubtree(node: TrieNode): Float {
        var max = node.terminalScore ?: 0f
        for (child in node.children.values) {
            val childMax = maxScoreInSubtree(child)
            if (childMax > max) max = childMax
        }
        return max
    }
}
