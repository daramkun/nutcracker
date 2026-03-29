package com.daram.nutcracker.prediction

/**
 * 라이브러리에 번들된 기본 사전.
 * dictionary.tsv 리소스에서 단어 목록을 로드합니다.
 *
 * TSV 포맷: word\tfrequency\tlanguage (헤더 없음)
 * language: "ko" 또는 "en"
 */
object BundledDictionary {
    private val wordsByLanguage: Map<InputLanguage, List<WordEntry>> by lazy { loadAll() }

    fun getWords(language: InputLanguage): List<WordEntry> =
        wordsByLanguage[language] ?: emptyList()

    private fun loadAll(): Map<InputLanguage, List<WordEntry>> {
        val text = loadDictionaryText()
        val ko = mutableListOf<WordEntry>()
        val en = mutableListOf<WordEntry>()
        for (line in text.lineSequence()) {
            if (line.isBlank()) continue
            val parts = line.split('\t')
            if (parts.size != 3) continue
            val word = parts[0]
            val freq = parts[1].toIntOrNull() ?: continue
            if (word.isEmpty() || freq <= 0) continue
            when (parts[2].trim()) {
                "ko" -> ko.add(WordEntry(word = word, frequency = freq))
                "en" -> en.add(WordEntry(word = word, frequency = freq))
            }
        }
        return mapOf(InputLanguage.KOREAN to ko, InputLanguage.ENGLISH to en)
    }
}
