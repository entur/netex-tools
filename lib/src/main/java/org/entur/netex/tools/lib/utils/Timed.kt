package org.entur.netex.tools.lib.utils

inline fun <T> timedMs(block: () -> T): Pair<T, Long> {
    val startTime = System.currentTimeMillis()
    val result: T = block()
    return result to System.currentTimeMillis() - startTime
}

inline fun <T> timedSeconds(block: () -> T): Pair<T, Double> {
    val startTime = System.currentTimeMillis()
    val result: T = block()
    return result to (System.currentTimeMillis() - startTime) / 1000.0
}