package org.entur.netex.tools.lib.selectors.entities

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.selections.EntitySelection

class EntityPruningSelector(private val typesToRemove: Set<String>, private val entitySelection: EntitySelection): EntitySelector() {
    private fun shouldKeep(entity: Entity, currentEntitySelection: EntitySelection, model: EntityModel): Boolean {
        if (entity.parent != null && !currentEntitySelection.includes(entity.parent)) {
            return false
        }

        val allEntitiesOfTypeShouldBeKept = entity.type !in typesToRemove
        if (allEntitiesOfTypeShouldBeKept) {
            return true
        }

        val entitiesReferringToEntity = model.getEntitiesReferringTo(entity)
        return currentEntitySelection.includesOneOf(entitiesReferringToEntity)
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
                    val shouldKeepEntity = shouldKeep(entity, currentEntitySelection, model)
                    if (!shouldKeepEntity) {
                        hasPrunedEntities = true
                    }
                    shouldKeepEntity
                })
                .forEach { entity ->
                    entitiesToKeep.computeIfAbsent(entity.type) { mutableMapOf() }[entity.id] = entity
                }
            currentEntitySelection = EntitySelection(entitiesToKeep, model)
        } while (hasPrunedEntities)

        return currentEntitySelection
    }
}