package org.entur.netex.tools.lib.model


/**
 * Set of named counters
 */
class SetOfCounters {
    private val counters = mutableMapOf<String, Counter>()

    fun inc(element : String) {
        counters.computeIfAbsent(element) { _ -> Counter() }.inc()
    }

    fun get(element: String) : Int = counters[element]?.get() ?: 0

    fun listElements() : List<String> = counters.keys.toList()
}