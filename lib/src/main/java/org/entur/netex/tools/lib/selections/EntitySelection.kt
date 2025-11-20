package org.entur.netex.tools.lib.selections

import org.entur.netex.tools.lib.model.CompositeEntityId
import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel

class EntitySelection(
    val selection: Map<String, Map<String, Entity>>,
    val model: EntityModel
): Selection() {
    val allIds = selection.values.flatMap { it.keys }.toHashSet()

    fun isEqualTo(other: EntitySelection): Boolean {
        return allIds == other.allIds
    }

    fun withReplaced(key: String, newInnerMap: Map<String, Entity>): EntitySelection {
        val newSelection = selection.toMutableMap().apply {
            this[key] = newInnerMap
        }.toMap()
        return EntitySelection(newSelection, model)
    }

    fun copy(
        selection: Map<String, Map<String, Entity>> = this.selection,
        model: EntityModel = this.model
    ): EntitySelection {
        return EntitySelection(selection, model)
    }

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
        val id = element.attributes["id"] ?: ""
        val version = element.attributes["version"] ?: ""
        val order = element.attributes["order"] ?: ""

        return isSelected(element.name, id) || isSelected(element.name, CompositeEntityId.ByIdVersionAndOrder(baseId = id, version = version, order = order).id)
    }

    fun includes(entity: Entity) : Boolean {
        val entitiesOfType = selection[entity.type]
        return entitiesOfType?.containsKey(entity.id) ?: false
    }

    fun includes(id: String) : Boolean {
        return allIds.contains(id)
    }

    fun hasEntitiesReferringTo(entity: Entity): Boolean {
        val entities = model.getEntitiesReferringTo(entity)
        return entities.any { includes(it) }
    }

    fun allIds(): Set<String> {
        return allIds
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
            val entitiesInCommon = idsInCommon.map { model.getEntity(it) }
            for (entity in entitiesInCommon) {
                if (entity != null) {
                    resultingMap.computeIfAbsent(commonType) { mutableMapOf() }[entity.id] = entity
                }
            }
        }

        return EntitySelection(resultingMap, model)
    }
}