package org.entur.netex.tools.lib.selection

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.PublicationEnumeration
import org.entur.netex.tools.lib.model.SimpleEntitySelection
import kotlin.collections.set

class PublicEntitiesSelector: EntitySelector() {

    override fun selector(entities: Collection<Entity>): SimpleEntitySelection {
        val publicEntitiesMap = mutableMapOf<String, MutableMap<String, Entity>>()
        entities.filter { it.publication == PublicationEnumeration.PUBLIC.value }.forEach { entity ->
            publicEntitiesMap.computeIfAbsent(entity.type) { mutableMapOf() }[entity.id] = entity
        }
        return SimpleEntitySelection(publicEntitiesMap)
    }

}