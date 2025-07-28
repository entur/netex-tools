package org.entur.netex.tools.lib.selectors

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.selections.EntitySelection

class SkipElementsSelector(val elementsToSkip: Set<String>): EntitySelector() {

    override fun selectEntities(model: EntityModel): EntitySelection {
        val mapOfElementsToKeep = mutableMapOf<String, MutableMap<String, Entity>>()
        val allEntities = model.listAllEntities()
        allEntities.filter { it.type !in elementsToSkip }.forEach { entity ->
            mapOfElementsToKeep.computeIfAbsent(entity.type) { mutableMapOf() }[entity.id] = entity
        }
        return EntitySelection(mapOfElementsToKeep)
    }

}