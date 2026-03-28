package `in`.daram.nutcracker.app

import `in`.daram.nutcracker.SyllableState
import java.util.concurrent.ConcurrentHashMap
import java.util.UUID

data class Session(
    val id: String,
    var layout: String,
    var state: SyllableState,
    var committed: String,
    val createdAt: Long = System.currentTimeMillis(),
    var lastUsedAt: Long = System.currentTimeMillis(),
)

object SessionStore {
    private val sessions = ConcurrentHashMap<String, Session>()
    private const val TTL_MS = 30 * 60 * 1000L // 30분

    fun create(layout: String): Session {
        evictExpired()
        val session = Session(
            id = UUID.randomUUID().toString(),
            layout = layout,
            state = SyllableState(),
            committed = "",
        )
        sessions[session.id] = session
        return session
    }

    fun get(id: String): Session? {
        val session = sessions[id] ?: return null
        session.lastUsedAt = System.currentTimeMillis()
        return session
    }

    fun delete(id: String) {
        sessions.remove(id)
    }

    private fun evictExpired() {
        val now = System.currentTimeMillis()
        sessions.entries.removeIf { now - it.value.lastUsedAt > TTL_MS }
    }
}
