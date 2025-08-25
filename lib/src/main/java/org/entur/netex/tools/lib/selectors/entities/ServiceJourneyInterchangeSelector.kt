package org.entur.netex.tools.lib.selectors.entities

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.selections.EntitySelection

class ServiceJourneyInterchangeSelector(private val entitySelection: EntitySelection): EntitySelector() {
    override fun selectEntities(model: EntityModel): EntitySelection {
        val serviceJourneyInterchangeEntities = model.getEntitiesOfType("ServiceJourneyInterchange").toSet()

        val serviceJourneyInterchangesToKeep = serviceJourneyInterchangeEntities
            .filter { serviceJourneyInterchange ->
                val fromJourneyRef = model.getRefsOfTypeFrom(serviceJourneyInterchange.id, "FromJourneyRef")[0].ref
                val toJourneyRef = model.getRefsOfTypeFrom(serviceJourneyInterchange.id, "ToJourneyRef")[0].ref
                val hasRefToFromJourney = entitySelection.isSelected("ServiceJourney", fromJourneyRef)
                val hasRefToToJourney = entitySelection.isSelected("ServiceJourney", toJourneyRef)
                hasRefToFromJourney && hasRefToToJourney
            }

        val entitiesToKeep = mutableMapOf<String, MutableMap<String, Entity>>()
        val allEntities = model.listAllEntities()
        allEntities.filter { it.type != "ServiceJourneyInterchange" }.forEach { entity ->
            entitiesToKeep.computeIfAbsent(entity.type) { mutableMapOf() }[entity.id] = entity
        }

        serviceJourneyInterchangesToKeep.forEach { entity ->
            entitiesToKeep.computeIfAbsent(entity.type) { mutableMapOf() }[entity.id] = entity
        }

        return EntitySelection(entitiesToKeep)
    }
}