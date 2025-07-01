package org.entur.netex.tools.lib.plugin.activedates.handlers

import org.entur.netex.tools.lib.extensions.putOrAddToExistingList
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.plugin.activedates.NetexDataCollector

class DatedServiceJourneyHandler(val activeDatesRepository: ActiveDatesRepository): NetexDataCollector() {

    override fun endElement(currentEntity: Entity) {
        if (activeDatesRepository.currentServiceJourneyRef != null && activeDatesRepository.currentOperatingDayRef != null) {
            activeDatesRepository.serviceJourneyToOperatingDayRefMap.putOrAddToExistingList(
                activeDatesRepository.currentServiceJourneyRef!!,
                activeDatesRepository.currentOperatingDayRef!!,
            )
        }
        if (activeDatesRepository.currentOperatingDayRef != null) {
            activeDatesRepository.datedServiceJourneyToOperatingDayRefMap[currentEntity.id] = activeDatesRepository.currentOperatingDayRef!!
        }
        activeDatesRepository.currentOperatingDayRef = null
        activeDatesRepository.currentServiceJourneyRef = null
    }
}