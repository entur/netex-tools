package org.entur.netex.tools.lib.selectors.entities

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.PublicationEnumeration
import org.entur.netex.tools.lib.selections.EntitySelection
import kotlin.collections.set

class PublicEntitiesSelector: EntitySelector {

    override fun selectEntities(model: EntityModel, currentEntitySelection: EntitySelection?): EntitySelection {
        val publicEntitiesMap = mutableMapOf<String, MutableMap<String, Entity>>()
        for (entity in model.listAllEntities()) {
            if (entity.publication == PublicationEnumeration.PUBLIC.value) {
                val typeMap = publicEntitiesMap.getOrPut(entity.type) { HashMap() }
                    typeMap[entity.id] = entity
            }
        }
        return EntitySelection(publicEntitiesMap, model)
    }

}