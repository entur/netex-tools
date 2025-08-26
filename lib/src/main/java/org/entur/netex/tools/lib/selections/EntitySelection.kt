package org.entur.netex.tools.lib.selections

import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel

class EntitySelection(
    val selection: MutableMap<String, MutableMap<String, Entity>>,
    val model: EntityModel
): Selection() {

    private var externalRefs: MutableSet<String> = mutableSetOf()

    init {
        // TODO: Fix this line
        externalRefs = selection.values.flatMap { it.values.flatMap { entity -> entity.externalRefs } }.toMutableSet()
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
        val id = element.attributes?.getValue("id")
        return isSelected(element.name, id)
    }

    fun includes(entity: Entity) : Boolean {
        return selection[entity.type]?.containsKey(entity.id) ?: false
    }

    fun allIds(): Set<String> {
        return selection.values.flatMap { it.keys }.toHashSet()
    }

    fun hasEntitiesReferringTo(entity: Entity) = entity.id in externalRefs

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