package org.entur.netex.tools.lib.utils

inline fun <T> timed(block: () -> T): Pair<T, Long> {
    val startTime = System.currentTimeMillis()
    val result: T = block()
    return result to System.currentTimeMillis() - startTime
}