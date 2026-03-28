package com.daram.nutcracker.app

import kotlinx.serialization.Serializable

@Serializable
data class InputRequest(
    val type: String,   // "char" | "special"
    val key: String,    // 단일 문자 또는 SpecialKey 이름 (예: "BACKSPACE")
)

@Serializable
data class PredictionCandidateDto(
    val word: String,
    val score: Float,
    val isUserWord: Boolean,
)

@Serializable
data class InputResponse(
    val committed: String,
    val composing: String,
    val fsm: String,
    val cho: String?,
    val jung: String?,
    val jong: String?,
    val jong2: String?,
    val cycleCount: Int,
    val predictions: List<PredictionCandidateDto>,
    val nextKeyHints: Map<Char, Float>,
)

@Serializable
data class SessionResponse(val sessionId: String, val layout: String)

@Serializable
data class LayoutResponse(val key: String, val displayName: String)
