package org.entur.netex.tools.lib.selections

import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityId
import org.entur.netex.tools.lib.model.EntityModel

class EntitySelection(
    val selection: MutableMap<String, MutableMap<EntityId, Entity>>,
    val model: EntityModel
): Selection() {
    fun isSelected(e : Entity?) : Boolean {
        if (e == null) { return false }
        return isSelected(e.type, e.id)
    }

    fun isSelected(type : String, id : EntityId?) : Boolean {
        if (id == null) return false
        val m = selection[type]
        return m != null && m.containsKey(id)
    }

    override fun includes(element : Element) : Boolean {
        if (!element.isEntity()) {
            return false
        }
        val id = element.attributes?.getValue("id")
        val version = element.getAttribute("version")
        val order = element.getAttribute("order")
        return isSelected(element.name, EntityId.Simple(id ?: "")) ||
               isSelected(element.name, EntityId.Composite(id ?: "", version, order))
    }

    fun includes(entity: Entity) : Boolean {
        return selection[entity.type]?.containsKey(entity.id) ?: false
    }

    fun hasEntitiesReferringTo(entity: Entity): Boolean {
        val entities = model.getEntitiesReferringTo(entity)
        return entities.any { includes(it) }
    }

    fun allIds(): Set<EntityId> {
        return selection.values.flatMap { it.keys }.toHashSet()
    }

    private fun getIdsByType(type: String): Set<EntityId> {
        return selection[type]?.keys ?: emptySet()
    }

    private fun intersectIdsByType(otherSelection: EntitySelection, type: String): Set<EntityId> {
        val idsFromSelf = getIdsByType(type)
        val idsFromOther = otherSelection.getIdsByType(type)
        return idsFromSelf.intersect(idsFromOther)
    }

    fun intersectWith(
        otherEntitySelection: EntitySelection
    ): EntitySelection {
        val resultingMap = mutableMapOf<String, MutableMap<EntityId, Entity>>()
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