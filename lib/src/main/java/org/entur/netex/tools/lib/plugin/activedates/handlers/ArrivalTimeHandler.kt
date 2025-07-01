package org.entur.netex.tools.lib.plugin.activedates.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesParsingContext
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.plugin.activedates.NetexDataCollector
import java.time.LocalTime

/**
 * Handler to collect ArrivalTime values as they are encountered in the XML.
 * The last value encountered for a ServiceJourney will be stored.
 */
class ArrivalTimeHandler(private val activeDatesRepository: ActiveDatesRepository) : NetexDataCollector() {
    private val stringBuilder = StringBuilder()

    override fun characters(context: ActiveDatesParsingContext, ch: CharArray?, start: Int, length: Int) {
        stringBuilder.append(ch, start, length)
    }

    override fun endElement(context: ActiveDatesParsingContext, currentEntity: Entity) {
        val arrivalTimeString = stringBuilder.toString().trim()
        val serviceJourneyId = context.currentServiceJourneyId
        if (serviceJourneyId != null && arrivalTimeString.isNotEmpty()) {
            activeDatesRepository.serviceJourneyToFinalArrivalTimeMap[serviceJourneyId] = LocalTime.parse(arrivalTimeString)
        }
        stringBuilder.clear()
    }
}

