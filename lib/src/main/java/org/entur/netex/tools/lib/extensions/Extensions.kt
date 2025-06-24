package org.entur.netex.tools.lib.extensions

import java.time.LocalDate

fun <K, V> MutableMap<K, MutableList<V>>.putOrAddToExistingList(
    key: K,
    value: V
): Boolean {
    return this.computeIfAbsent(key) { mutableListOf() }.add(value)
}

fun <K, V>MutableMap<K, MutableList<V>>.putOrAddToExistingList(
    key: K,
    value: List<V>
): Boolean {
    return this.computeIfAbsent(key) { mutableListOf() }.addAll(value)
}
