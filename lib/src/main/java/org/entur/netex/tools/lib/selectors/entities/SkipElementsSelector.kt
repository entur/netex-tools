package org.entur.netex.tools.lib.selectors.entities

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.selections.EntitySelection

class SkipElementsSelector(val elementsToSkip: Set<String>): EntitySelector() {

    override fun selectEntities(model: EntityModel): EntitySelection {
        val mapOfElementsToKeep = mutableMapOf<String, MutableMap<String, Entity>>()
        for (entity in model.listAllEntities()) {
            if (entity.type !in elementsToSkip) {
                val typeMap = mapOfElementsToKeep.getOrPut(entity.type) { HashMap() }
                if (entity.compositeId != null) {
                    typeMap[entity.compositeId.id] = entity
                } else {
                    typeMap[entity.id] = entity
                }
            }
        }
        return EntitySelection(mapOfElementsToKeep, model)
    }

}