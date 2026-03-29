package com.daram.nutcracker.app

import com.daram.nutcracker.HangulAutomata
import com.daram.nutcracker.automata.*

data class LayoutInfo(val key: String, val displayName: String)

object AutomataRegistry {
    val all: Map<String, HangulAutomata> = linkedMapOf(
        "dubeolsik" to DubeolsikAutomata(),
        "danmoem" to DanmoemAutomata(),
        "cheonjiin" to CheonjiinAutomata(),
        "naratgeul" to NaratgeulAutomata(),
        "skyii" to SkyIIAutomata(),
        "motorola" to MotorolaAutomata(),
        "mue128" to Mue128Automata(),
        "english" to EnglishAutomata(),
    )

    val layouts: List<LayoutInfo> = all.map { (key, automata) ->
        LayoutInfo(key, automata.layoutName)
    }
}
