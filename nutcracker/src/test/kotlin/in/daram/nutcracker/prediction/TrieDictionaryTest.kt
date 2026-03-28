package `in`.daram.nutcracker.prediction

import `in`.daram.nutcracker.prediction.trie.TrieDictionary
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TrieDictionaryTest {

    private fun koreanDict(vararg words: Pair<String, Int>): TrieDictionary {
        val dict = TrieDictionary(InputLanguage.KOREAN)
        dict.initialize(words.map { (w, f) -> WordEntry(w, f) })
        return dict
    }

    @Test
    fun `initialize - size가 올바르게 설정된다`() {
        val dict = koreanDict("한글" to 100, "한국어" to 80, "영어" to 60)
        assertEquals(3, dict.size)
    }

    @Test
    fun `search - 정확한 프리픽스로 검색된다`() {
        val dict = koreanDict("한글" to 100, "한국어" to 80, "영어" to 60)
        val results = dict.search("한", 10)
        val words = results.map { it.first }
        assertTrue("한글" in words)
        assertTrue("한국어" in words)
        assertFalse("영어" in words)
    }

    @Test
    fun `search - score 내림차순 정렬된다`() {
        val dict = koreanDict("한글" to 100, "한국어" to 80)
        val results = dict.search("한", 10)
        assertTrue(results[0].second >= results[1].second)
    }

    @Test
    fun `search - limit이 적용된다`() {
        val dict = koreanDict("한글" to 100, "한국어" to 80, "한국" to 90)
        val results = dict.search("한", 2)
        assertEquals(2, results.size)
    }

    @Test
    fun `search - 일치하는 단어 없으면 빈 리스트 반환`() {
        val dict = koreanDict("한글" to 100)
        val results = dict.search("영", 10)
        assertTrue(results.isEmpty())
    }

    @Test
    fun `add - 새 단어 추가 후 검색된다`() {
        val dict = koreanDict("한글" to 100)
        dict.add("한국어", 80)
        val results = dict.search("한", 10)
        assertTrue(results.map { it.first }.contains("한국어"))
    }

    @Test
    fun `add - frequency 0이면 삭제된다`() {
        val dict = koreanDict("한글" to 100, "한국어" to 80)
        dict.add("한글", 0)
        val results = dict.search("한", 10)
        assertFalse(results.map { it.first }.contains("한글"))
    }

    @Test
    fun `initialize - 재호출 시 기존 항목 대체된다`() {
        val dict = koreanDict("한글" to 100, "한국어" to 80)
        dict.initialize(listOf(WordEntry("영어", 100), WordEntry("영국" , 80)))
        assertEquals(2, dict.size)
        assertTrue(dict.search("영", 10).isNotEmpty())
        assertTrue(dict.search("한", 10).isEmpty())
    }

    @Test
    fun `searchMulti - 여러 프리픽스 통합 검색`() {
        val dict = koreanDict("닭" to 100, "달" to 80, "밥" to 90)
        val results = dict.searchMulti(listOf("닭", "달"), 10)
        val words = results.map { it.first }
        assertTrue("닭" in words)
        assertTrue("달" in words)
        assertFalse("밥" in words)
    }

    @Test
    fun `영어 사전 검색`() {
        val dict = TrieDictionary(InputLanguage.ENGLISH)
        dict.initialize(listOf(WordEntry("hello", 100), WordEntry("help", 80), WordEntry("world", 60)))
        val results = dict.search("hel", 10)
        val words = results.map { it.first }
        assertTrue("hello" in words)
        assertTrue("help" in words)
        assertFalse("world" in words)
    }
}
