package org.entur.netex.tools.lib.selectors

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.PublicationEnumeration
import org.entur.netex.tools.lib.selections.EntitySelection
import kotlin.collections.set

class PublicEntitiesSelector: EntitySelector() {

    override fun selector(entities: Collection<Entity>): EntitySelection {
        val publicEntitiesMap = mutableMapOf<String, MutableMap<String, Entity>>()
        entities.filter { it.publication == PublicationEnumeration.PUBLIC.value }.forEach { entity ->
            publicEntitiesMap.computeIfAbsent(entity.type) { mutableMapOf() }[entity.id] = entity
        }
        return EntitySelection(publicEntitiesMap)
    }

}