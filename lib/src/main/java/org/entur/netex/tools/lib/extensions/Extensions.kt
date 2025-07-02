package org.entur.netex.tools.lib.extensions

fun <K, V> MutableMap<K, MutableSet<V>>.putOrAddToSet(
    key: K,
    value: V
): Boolean {
    return this.computeIfAbsent(key) { mutableSetOf() }.add(value)
}
