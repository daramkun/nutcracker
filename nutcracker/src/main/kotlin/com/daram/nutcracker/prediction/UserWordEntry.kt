package com.daram.nutcracker.prediction

/** 사용자 학습 단어 항목 */
data class UserWordEntry(
    val word: String,
    val language: InputLanguage,
    val score: Float,
    val useCount: Int,
    val lastUsedMs: Long,
)
