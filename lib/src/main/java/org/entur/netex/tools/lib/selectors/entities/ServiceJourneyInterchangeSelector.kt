package org.entur.netex.tools.lib.selectors.entities

import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.selections.EntitySelection

class ServiceJourneyInterchangeSelector(private val entitySelection: EntitySelection): EntitySelector() {
    override fun selectEntities(model: EntityModel): EntitySelection {
        val serviceJourneyInterchangeEntities = model.getEntitiesOfType("ServiceJourneyInterchange").toSet()

        val serviceJourneyInterchangesToKeep = serviceJourneyInterchangeEntities
            .filter { serviceJourneyInterchange ->
                val fromJourneyRef = model.getRefsOfTypeFrom(serviceJourneyInterchange.id, "FromJourneyRef").firstOrNull()?.ref
                val toJourneyRef = model.getRefsOfTypeFrom(serviceJourneyInterchange.id, "ToJourneyRef").firstOrNull()?.ref
                val hasRefToFromJourney = entitySelection.isSelected("ServiceJourney", fromJourneyRef)
                val hasRefToToJourney = entitySelection.isSelected("ServiceJourney", toJourneyRef)
                hasRefToFromJourney && hasRefToToJourney
            }

        entitySelection.selection["ServiceJourneyInterchange"] = mutableMapOf()
        serviceJourneyInterchangesToKeep.forEach { interchange ->
            entitySelection.selection["ServiceJourneyInterchange"]!![interchange.id] = interchange
        }

        return entitySelection
    }
}