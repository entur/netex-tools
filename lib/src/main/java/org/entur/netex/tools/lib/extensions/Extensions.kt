package org.entur.netex.tools.lib.extensions

import org.entur.netex.tools.lib.model.Entity

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

fun Map<String, Map<String, Entity>>.intersectWith(
    otherMap: Map<String, Map<String, Entity>>
): MutableMap<String, MutableMap<String, Entity>> {
    val resultingMap = mutableMapOf<String, MutableMap<String, Entity>>()
    val typesInCommon = keys.intersect(otherMap.keys)

    for (commonType in typesInCommon) {
        val entitiesOfTypeById = this[commonType]
        val idsFromSelfMap = entitiesOfTypeById?.keys
        val idsFromOtherMap = otherMap[commonType]?.keys
        if (idsFromSelfMap == null || idsFromOtherMap == null) {
            continue
        }
        val idsInCommon = idsFromSelfMap.intersect(idsFromOtherMap)
        if (idsInCommon.isNotEmpty()) {
            val commonEntities = entitiesOfTypeById.filter { it.key in idsInCommon }
            resultingMap.put(commonType, commonEntities.toMutableMap())
        }
    }

    return resultingMap
}