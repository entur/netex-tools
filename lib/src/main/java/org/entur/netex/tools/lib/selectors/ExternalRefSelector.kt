package org.entur.netex.tools.lib.selectors

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.selections.EntitySelection

class ExternalRefSelector(private val entitySelection: EntitySelection): EntitySelector() {
    override fun selectEntities(model: EntityModel): EntitySelection {
        val entitiesWithReferences = mutableMapOf<String, MutableMap<String, Entity>>()
        val unreferencedTypesToRemove = setOf("JourneyPattern")
        val externalRefsInSelection = entitySelection.allExternalRefs()
        model.listAllEntities().filter {
            it.type !in unreferencedTypesToRemove || it.id in externalRefsInSelection
        }.forEach { entity ->
            entitiesWithReferences.computeIfAbsent(entity.type) { mutableMapOf() }[entity.id] = entity
        }
        return EntitySelection(entitiesWithReferences)
    }
}