package org.entur.netex.tools.lib.selection

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntitySelection

class SkipElementsSelector(val elementsToSkip: Set<String>): EntitySelector() {

    override fun selector(entities: Collection<Entity>): EntitySelection {
        val mapOfElementsToKeep = mutableMapOf<String, MutableMap<String, Entity>>()
        entities.filter { it.type !in elementsToSkip }.forEach { entity ->
            mapOfElementsToKeep.computeIfAbsent(entity.type) { mutableMapOf() }[entity.id] = entity
        }
        return EntitySelection(mapOfElementsToKeep)
    }

}