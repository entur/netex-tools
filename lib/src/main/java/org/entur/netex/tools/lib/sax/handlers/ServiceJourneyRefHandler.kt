package org.entur.netex.tools.lib.sax.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.sax.ActiveDatesModel
import org.entur.netex.tools.lib.sax.NetexDataCollector
import org.xml.sax.Attributes

class ServiceJourneyRefHandler(val activeDatesModel: ActiveDatesModel): NetexDataCollector() {
    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?,
        currentEntity: Entity
    ) {
        if (currentEntity.type == "DatedServiceJourney") {
            activeDatesModel.currentServiceJourneyRef = attributes?.getValue("ref")
        }
    }
}