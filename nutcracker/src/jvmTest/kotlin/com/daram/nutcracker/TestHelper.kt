package com.daram.nutcracker

import com.daram.nutcracker.automata.*

/**
 * 테스트 헬퍼: 키 시퀀스를 순차 처리하여 (committed 전체, 현재 composing) 반환.
 *
 * [keys] 형식:
 *  - 일반 Char: 해당 문자 입력
 *  - '\u0008' (BS): Backspace
 *  - '\u0020' (SPACE): Space
 *  - '\u000A' (LF): Enter
 *  - '\u0007' (BEL): flush (강제 확정)
 */
fun processSequence(automata: HangulAutomata, vararg keys: Char): Pair<String, String> {
    var state = automata.initialState()
    val committed = StringBuilder()
    var lastComposing = ""
    for (key in keys) {
        val input = when (key) {
            '\u0008' -> KeyInput.Special(SpecialKey.BACKSPACE)
            '\u0020' -> KeyInput.Special(SpecialKey.SPACE)
            '\u000A' -> KeyInput.Special(SpecialKey.ENTER)
            '\u0007' -> null  // flush
            else -> KeyInput.Char(key)
        }
        val result = if (input == null) automata.flush(state) else automata.process(state, input)
        // committed "\b"는 마지막 committed 글자 삭제 신호
        if (result.committed == "\b") {
            if (committed.isNotEmpty()) committed.deleteCharAt(committed.length - 1)
        } else {
            committed.append(result.committed)
        }
        lastComposing = result.composing
        state = result.newState
    }
    return committed.toString() to lastComposing
}

/** 특수키 입력 포함 시퀀스 */
fun processSequenceWithSpecial(automata: HangulAutomata, inputs: List<KeyInput>): Pair<String, String> {
    var state = automata.initialState()
    val committed = StringBuilder()
    var lastComposing = ""
    for (input in inputs) {
        val result = automata.process(state, input)
        if (result.committed == "\b") {
            if (committed.isNotEmpty()) committed.deleteCharAt(committed.length - 1)
        } else {
            committed.append(result.committed)
        }
        lastComposing = result.composing
        state = result.newState
    }
    return committed.toString() to lastComposing
}

val dubeolsik = DubeolsikAutomata()
val danmoem = DanmoemAutomata()
val cheonjiin = CheonjiinAutomata()
val naratgeul = NaratgeulAutomata()
val skyII = SkyIIAutomata()
val motorola = MotorolaAutomata()
val mue128 = Mue128Automata()
