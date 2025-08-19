package org.entur.netex.tools.lib.selectors

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.selections.EntitySelection

class EntityPruningSelector(
    private val entitySelection: EntitySelection,
    private val unreferencedEntitiesToRemove: Set<String>
): EntitySelector() {
    private fun shouldKeep(entity: Entity) =
        entity.type !in unreferencedEntitiesToRemove || entitySelection.hasEntitiesReferringTo(entity)

    override fun selectEntities(model: EntityModel): EntitySelection {
        val entitiesToKeep = mutableMapOf<String, MutableMap<String, Entity>>()

        model
            .listAllEntities()
            .filter({ entity -> shouldKeep(entity) })
            .forEach { entity ->
                entitiesToKeep.computeIfAbsent(entity.type) { mutableMapOf() }[entity.id] = entity
            }

        return EntitySelection(entitiesToKeep)
    }
}