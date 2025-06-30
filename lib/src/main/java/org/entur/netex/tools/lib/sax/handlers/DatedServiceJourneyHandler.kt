package org.entur.netex.tools.lib.sax.handlers

import org.entur.netex.tools.lib.extensions.putOrAddToExistingList
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.sax.ActiveDatesModel
import org.entur.netex.tools.lib.sax.NetexDataCollector

class DatedServiceJourneyHandler(val activeDatesModel: ActiveDatesModel): NetexDataCollector() {

    override fun endElement(currentEntity: Entity) {
        if (activeDatesModel.currentServiceJourneyRef != null && activeDatesModel.currentOperatingDayRef != null) {
            activeDatesModel.serviceJourneyToOperatingDayRefMap.putOrAddToExistingList(
                activeDatesModel.currentServiceJourneyRef!!,
                activeDatesModel.currentOperatingDayRef!!,
            )
        }
        if (activeDatesModel.currentOperatingDayRef != null) {
            activeDatesModel.datedServiceJourneyToOperatingDayRefMap[currentEntity.id] = activeDatesModel.currentOperatingDayRef!!
        }
        activeDatesModel.currentOperatingDayRef = null
        activeDatesModel.currentServiceJourneyRef = null
    }
}