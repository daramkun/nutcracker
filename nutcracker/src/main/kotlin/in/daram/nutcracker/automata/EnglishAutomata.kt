package `in`.daram.nutcracker.automata

import `in`.daram.nutcracker.*

class EnglishAutomata : HangulAutomata {
    override val layoutName: String = "English"

    override fun process(state: SyllableState, input: KeyInput): InputResult {
        return when (input) {
            is KeyInput.Char -> {
                val flushed = flush(state)
                InputResult(flushed.committed + input.key, "", flushed.newState)
            }
            is KeyInput.Special -> processSpecialCommon(state, input.type)
        }
    }

    override fun flush(state: SyllableState): InputResult = flushCommonFSM(state)
}
