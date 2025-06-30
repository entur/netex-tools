package org.entur.netex.tools.lib.sax.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.sax.ActiveDatesModel
import org.entur.netex.tools.lib.sax.NetexDataCollector
import org.xml.sax.Attributes

class ServiceJourneyHandler(val activeDatesModel: ActiveDatesModel): NetexDataCollector() {
    override fun startElement(
        attributes: Attributes?,
        currentEntity: Entity
    ) {
        activeDatesModel.currentServiceJourneyId = currentEntity.id
    }

    override fun endElement(currentEntity: Entity) {
        activeDatesModel.currentServiceJourneyId = null
    }
}