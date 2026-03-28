package `in`.daram.nutcracker.app

import `in`.daram.nutcracker.HangulAutomata
import `in`.daram.nutcracker.automata.*

data class LayoutInfo(val key: String, val displayName: String)

object AutomataRegistry {
    val all: Map<String, HangulAutomata> = linkedMapOf(
        "dubeolsik" to DubeolsikAutomata(),
        "danmoem"   to DanmoemAutomata(),
        "cheonjiin" to CheonjiinAutomata(),
        "naratgeul" to NaratgeulAutomata(),
        "skyii"     to SkyIIAutomata(),
        "motorola"  to MotorolaAutomata(),
    )

    val layouts: List<LayoutInfo> = all.map { (key, automata) ->
        LayoutInfo(key, automata.layoutName)
    }
}
