package org.entur.netex.tools.lib.plugin.activedates.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.plugin.activedates.NetexDataCollector

/**
 * Handler to collect ArrivalDayOffset values as they are encountered in the XML.
 * The last value encountered for a ServiceJourney will be stored.
 */
class ArrivalDayOffsetHandler(private val activeDatesRepository: ActiveDatesRepository) : NetexDataCollector() {
    private val stringBuilder = StringBuilder()

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        stringBuilder.append(ch, start, length)
    }

    override fun endElement(currentEntity: Entity) {
        val arrivalDayOffset = stringBuilder.toString().trim()
        val serviceJourneyId = activeDatesRepository.currentServiceJourneyId
        if (serviceJourneyId != null && arrivalDayOffset.isNotEmpty()) {
            activeDatesRepository.serviceJourneyToFinalArrivalDayOffsetMap[serviceJourneyId] = arrivalDayOffset.toInt()
        }
        stringBuilder.clear()
    }
}
