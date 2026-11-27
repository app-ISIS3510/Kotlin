package com.example.parku.data

import java.util.concurrent.ConcurrentHashMap

class SessionSummaryCache(private val ttlMillis: Long = 10 * 60_000L) {

    private class Entry(val summary: SessionSummary, val savedAt: Long)

    private val entries = ConcurrentHashMap<String, Entry>()

    fun put(summary: SessionSummary) {
        entries[summary.sessionId] = Entry(summary, System.currentTimeMillis())
    }

    fun get(sessionId: String): SessionSummary? {
        val entry = entries[sessionId] ?: return null
        if (System.currentTimeMillis() - entry.savedAt > ttlMillis) {
            entries.remove(sessionId)
            return null
        }
        return entry.summary
    }

    fun clear() = entries.clear()
}
