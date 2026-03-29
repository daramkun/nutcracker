package com.daram.nutcracker.prediction

/** 사전 초기화용 단어 항목 */
data class WordEntry(
    val word: String,
    val frequency: Int,   // 높을수록 자주 쓰이는 단어
)
