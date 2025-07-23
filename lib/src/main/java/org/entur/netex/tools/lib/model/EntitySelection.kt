package org.entur.netex.tools.lib.model

class EntitySelection(val selection: MutableMap<String, MutableMap<String, Entity>>) {
    fun isSelected(e : Entity?) : Boolean {
        if (e == null) { return false }
        return isSelected(e.type, e.id)
    }

    fun isSelected(type : String, id : String?) : Boolean {
        if (id == null) return false
        val m = selection[type]
        return m != null && m.containsKey(id)
    }

    private fun findMatchingIdsByType(otherSelection: EntitySelection, type: String): Set<String> {
        val idsFromSelfMap = selection[type]?.keys ?: emptySet()
        val idsFromOtherMap = otherSelection.selection[type]?.keys ?: emptySet()
        return idsFromSelfMap.intersect(idsFromOtherMap)
    }

    fun intersectWith(
        otherEntitySelection: EntitySelection
    ): EntitySelection {
        val resultingMap = mutableMapOf<String, MutableMap<String, Entity>>()
        val typesInCommon = selection.keys.intersect(otherEntitySelection.selection.keys)

        for (commonType in typesInCommon) {
            val idsInCommon = findMatchingIdsByType(otherEntitySelection, commonType)
            if (idsInCommon.isNotEmpty()) {
                val entitiesOfType = selection[commonType]!!
                val commonEntities = entitiesOfType.filter { it.key in idsInCommon }
                resultingMap.put(commonType, commonEntities.toMutableMap())
            }
        }

        return EntitySelection(resultingMap)
    }
}