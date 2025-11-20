package org.entur.netex.tools.lib.output

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

object NetexIdGenerator {
    private val counters = ConcurrentHashMap<String, AtomicLong>()

    fun next(codespace: String, type: String): String {
        val prefix = "${codespace.uppercase()}:$type"
        val newValue = if (counters.containsKey(prefix)) {
            counters[prefix]!!.incrementAndGet()
        } else {
            counters[prefix] = AtomicLong(1L)
            1L
        }
        return "$prefix:$newValue"
    }
}