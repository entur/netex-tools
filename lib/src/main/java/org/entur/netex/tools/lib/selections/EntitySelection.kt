package org.entur.netex.tools.lib.selections

import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.Entity

class EntitySelection(val selection: MutableMap<String, MutableMap<String, Entity>>): Selection() {
    fun isSelected(e : Entity?) : Boolean {
        if (e == null) { return false }
        return isSelected(e.type, e.id)
    }

    fun isSelected(type : String, id : String?) : Boolean {
        if (id == null) return false
        val m = selection[type]
        return m != null && m.containsKey(id)
    }

    override fun includes(element : Element) : Boolean {
        if (!element.isEntity()) {
            return false
        }
        val id = element.attributes?.getValue("id")
        return isSelected(element.name, id)
    }

    fun allIds(): Set<String> {
        return selection.values.flatMap { it.keys }.toHashSet()
    }

    private fun getIdsByType(type: String): Set<String> {
        return selection[type]?.keys ?: emptySet()
    }

    private fun intersectIdsByType(otherSelection: EntitySelection, type: String): Set<String> {
        val idsFromSelf = getIdsByType(type)
        val idsFromOther = otherSelection.getIdsByType(type)
        return idsFromSelf.intersect(idsFromOther)
    }

    fun intersectWith(
        otherEntitySelection: EntitySelection
    ): EntitySelection {
        val resultingMap = mutableMapOf<String, MutableMap<String, Entity>>()
        val typesInCommon = selection.keys.intersect(otherEntitySelection.selection.keys)

        for (commonType in typesInCommon) {
            val idsInCommon = intersectIdsByType(otherEntitySelection, commonType)
            if (idsInCommon.isNotEmpty()) {
                val entitiesOfType = selection[commonType]!!
                val commonEntities = entitiesOfType.filter { it.key in idsInCommon }
                resultingMap.put(commonType, commonEntities.toMutableMap())
            }
        }

        return EntitySelection(resultingMap)
    }
}