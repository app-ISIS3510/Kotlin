package com.example.parku.data

/**
 * Cache en memoria por frecuencia de uso y con vencimiento. Al llenarse sale la
 * entrada menos consultada; si empatan, la mas vieja. Lo guardado tambien caduca
 * por tiempo, para no reusar un dato de disponibilidad que ya quedo viejo.
 */
class LfuCache<K : Any, V : Any>(
    private val capacity: Int,
    private val lifetimeMillis: Long,
    private val now: () -> Long = System::currentTimeMillis,
) {

    init {
        require(capacity > 0) { "capacity must be greater than zero" }
        require(lifetimeMillis > 0) { "lifetimeMillis must be greater than zero" }
    }

    private class Entry<V>(var value: V, var storedAt: Long) {
        var hits: Int = 1
    }

    // El orden de insercion del LinkedHashMap resuelve los empates de frecuencia.
    private val entries = LinkedHashMap<K, Entry<V>>()

    val size: Int
        @Synchronized get() = entries.size

    @Synchronized
    fun get(key: K): V? {
        val entry = entries[key] ?: return null
        if (hasExpired(entry)) {
            entries.remove(key)
            return null
        }

        entry.hits++
        return entry.value
    }

    @Synchronized
    fun put(key: K, value: V) {
        val current = now()
        val previous = entries[key]
        if (previous != null) {
            previous.value = value
            previous.storedAt = current
            return
        }

        entries.entries.removeAll { hasExpired(it.value) }
        if (entries.size >= capacity) {
            entries.entries.minByOrNull { it.value.hits }?.let { entries.remove(it.key) }
        }
        entries[key] = Entry(value, current)
    }

    @Synchronized
    fun remove(key: K) {
        entries.remove(key)
    }

    @Synchronized
    fun clear() {
        entries.clear()
    }

    private fun hasExpired(entry: Entry<V>): Boolean = now() - entry.storedAt >= lifetimeMillis
}
