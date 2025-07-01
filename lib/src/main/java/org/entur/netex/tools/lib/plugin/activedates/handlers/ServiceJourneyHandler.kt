package org.entur.netex.tools.lib.plugin.activedates.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.plugin.activedates.NetexDataCollector
import org.xml.sax.Attributes

class ServiceJourneyHandler(val activeDatesRepository: ActiveDatesRepository): NetexDataCollector() {
    override fun startElement(
        attributes: Attributes?,
        currentEntity: Entity
    ) {
        activeDatesRepository.currentServiceJourneyId = currentEntity.id
    }

    override fun endElement(currentEntity: Entity) {
        activeDatesRepository.currentServiceJourneyId = null
    }
}