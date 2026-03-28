package `in`.daram.nutcracker.app

import `in`.daram.nutcracker.prediction.DefaultWordPredictor
import `in`.daram.nutcracker.prediction.InputLanguage
import `in`.daram.nutcracker.prediction.KeyMapper
import `in`.daram.nutcracker.prediction.WordPredictor
import `in`.daram.nutcracker.prediction.mapper.*
import `in`.daram.nutcracker.prediction.trie.TrieDictionary

object PredictorRegistry {
    private val koreanDict: TrieDictionary by lazy {
        val dict = TrieDictionary(InputLanguage.KOREAN)
        dict.initialize(SampleDictionary.getKoreanWords())
        dict
    }

    private val predictors: Map<String, WordPredictor?> by lazy {
        linkedMapOf(
            "dubeolsik" to createPredictor(),
            "danmoem" to createPredictor(),
            "cheonjiin" to createPredictor(),
            "naratgeul" to createPredictor(),
            "skyii" to createPredictor(),
            "motorola" to createPredictor(),
        )
    }

    private val keyMappers: Map<String, KeyMapper?> by lazy {
        linkedMapOf(
            "dubeolsik" to DubeolsikKeyMapper(),
            "danmoem" to DanmoemKeyMapper(),
            "cheonjiin" to CheonjiinKeyMapper(),
            "naratgeul" to NaratgeulKeyMapper(),
            "skyii" to SkyIIKeyMapper(),
            "motorola" to MotorolaKeyMapper(),
        )
    }

    fun getPredictor(layout: String): WordPredictor? = predictors[layout]

    fun getKeyMapper(layout: String): KeyMapper? = keyMappers[layout]

    private fun createPredictor(): WordPredictor {
        return DefaultWordPredictor(
            dictionaries = listOf(koreanDict),
            learningDelegate = null,
        )
    }
}
