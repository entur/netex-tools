package org.entur.netex.tools.lib.selectors.entities

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.PublicationEnumeration
import org.entur.netex.tools.lib.selections.EntitySelection
import kotlin.collections.set

class PublicEntitiesSelector: EntitySelector() {

    override fun selectEntities(model: EntityModel): EntitySelection {
        val publicEntitiesMap = mutableMapOf<String, MutableMap<String, Entity>>()
        val allEntities = model.listAllEntities()
        allEntities.filter { it.publication == PublicationEnumeration.PUBLIC.value }.forEach { entity ->
            publicEntitiesMap.computeIfAbsent(entity.type) { mutableMapOf() }[entity.id] = entity
        }
        return EntitySelection(publicEntitiesMap, model)
    }

}