package org.entur.netex.tools.lib.sax.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.sax.ActiveDatesModel
import org.entur.netex.tools.lib.sax.NetexDataCollector
import java.time.LocalTime

/**
 * Handler to collect ArrivalTime values as they are encountered in the XML.
 * The last value encountered for a ServiceJourney will be stored.
 */
class ArrivalTimeHandler(private val activeDatesModel: ActiveDatesModel) : NetexDataCollector() {
    private val stringBuilder = StringBuilder()

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        stringBuilder.append(ch, start, length)
    }

    override fun endElement(currentEntity: Entity) {
        val arrivalTimeString = stringBuilder.toString().trim()
        val serviceJourneyId = activeDatesModel.currentServiceJourneyId
        if (serviceJourneyId != null && arrivalTimeString.isNotEmpty()) {
            activeDatesModel.serviceJourneyToFinalArrivalTimeMap[serviceJourneyId] = LocalTime.parse(arrivalTimeString)
        }
        stringBuilder.clear()
    }
}

