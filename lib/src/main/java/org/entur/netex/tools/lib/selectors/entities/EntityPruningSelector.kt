package org.entur.netex.tools.lib.selectors.entities

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.selections.EntitySelection

class EntityPruningSelector(private val unreferencedEntitiesToRemove: Set<String>, private val entitySelection: EntitySelection): EntitySelector() {
    private fun shouldKeep(entity: Entity, currentEntitySelection: EntitySelection): Boolean {
        if (entity.parent != null && !currentEntitySelection.includes(entity.parent)) {
            return false
        }

        val entityTypeIsKeptRegardless = entity.type !in unreferencedEntitiesToRemove
        if (entityTypeIsKeptRegardless) {
            return true
        }

        return currentEntitySelection.hasEntitiesReferringTo(entity)
    }

    override fun selectEntities(model: EntityModel): EntitySelection {
        var currentEntitySelection: EntitySelection = entitySelection
        var hasPrunedEntities: Boolean
        do {
            hasPrunedEntities = false
            val entitiesToKeep = mutableMapOf<String, MutableMap<String, Entity>>()
            val entities = currentEntitySelection.selection.values.flatMap { it.values }
            entities
                .filter({ entity ->
                    val shouldKeepEntity = shouldKeep(entity, currentEntitySelection)
                    if (!shouldKeepEntity) {
                        hasPrunedEntities = true
                    }
                    shouldKeepEntity
                })
                .forEach { entity ->
                    entitiesToKeep.computeIfAbsent(entity.type) { mutableMapOf() }[entity.id] = entity
                }
            currentEntitySelection = EntitySelection(entitiesToKeep)
        } while (hasPrunedEntities)

        return currentEntitySelection
    }
}