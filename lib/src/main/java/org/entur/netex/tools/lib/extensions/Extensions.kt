package org.entur.netex.tools.lib.extensions

import java.time.LocalDate

fun MutableMap<String, MutableList<String>>.putOrAddToExistingList(
    key: String,
    value: String
): Boolean {
    return this.computeIfAbsent(key) { mutableListOf() }.add(value)
}

fun MutableMap<String, MutableList<LocalDate>>.putOrAddToExistingList(
    key: String,
    value: LocalDate
): Boolean {
    return this.computeIfAbsent(key) { mutableListOf() }.add(value)
}