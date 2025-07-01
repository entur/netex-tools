package org.entur.netex.tools.lib.plugin.activedates.handlers

import org.entur.netex.tools.lib.extensions.putOrAddToExistingList
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesParsingContext
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.plugin.activedates.NetexDataCollector

class DatedServiceJourneyHandler(val activeDatesRepository: ActiveDatesRepository): NetexDataCollector() {

    override fun endElement(context: ActiveDatesParsingContext, currentEntity: Entity) {
        if (context.currentServiceJourneyRef != null && context.currentOperatingDayRef != null) {
            activeDatesRepository.serviceJourneyToOperatingDayRefMap.putOrAddToExistingList(
                context.currentServiceJourneyRef!!,
                context.currentOperatingDayRef!!,
            )
        }
        if (context.currentOperatingDayRef != null) {
            activeDatesRepository.datedServiceJourneyToOperatingDayRefMap[currentEntity.id] = context.currentOperatingDayRef!!
        }
        context.currentOperatingDayRef = null
        context.currentServiceJourneyRef = null
    }
}