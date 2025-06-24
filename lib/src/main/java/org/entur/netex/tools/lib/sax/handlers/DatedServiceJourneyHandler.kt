package org.entur.netex.tools.lib.sax.handlers

import org.entur.netex.tools.lib.extensions.putOrAddToExistingList
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.sax.ActiveDatesModel
import org.entur.netex.tools.lib.sax.NetexDataCollector

class DatedServiceJourneyHandler(val activeDatesModel: ActiveDatesModel): NetexDataCollector() {
    override fun endElement(uri: String?, localName: String?, qName: String?, currentEntity: Entity) {
        if (activeDatesModel.currentServiceJourneyRef != null && activeDatesModel.currentOperatingDayRef != null) {
            activeDatesModel.serviceJourneyToOperatingDayRefMap.putOrAddToExistingList(
                activeDatesModel.currentServiceJourneyRef!!,
                activeDatesModel.currentOperatingDayRef!!,
            )
        }
        activeDatesModel.currentOperatingDayRef = null
        activeDatesModel.currentServiceJourneyRef = null
    }
}