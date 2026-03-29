package com.daram.nutcracker.prediction

internal actual fun loadDictionaryText(): String {
    val stream = object {}.javaClass.getResourceAsStream("/dictionary.tsv")
        ?: error("dictionary.tsv not found in classpath")
    return stream.use { it.readBytes().decodeToString() }
}
