package org.entur.netex.tools.lib.extensions

import org.xml.sax.Attributes

fun <K, V> MutableMap<K, MutableSet<V>>.putOrAddToSet(
    key: K,
    value: V
): Boolean {
    return this.computeIfAbsent(key) { mutableSetOf() }.add(value)
}

fun <K, V> MutableMap<K, MutableList<V>>.putOrAddToList(
    key: K,
    value: V
): Boolean {
    return this.computeIfAbsent(key) { mutableListOf() }.add(value)
}

fun Attributes.hasAttribute(attributeName: String): Boolean {
    return this.getValue(attributeName) != null
}

fun Attributes.toMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    for (i in 0 until this.length) {
        map[this.getQName(i)] = this.getValue(i)
    }
    return map
}