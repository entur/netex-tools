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

    override fun characters(ch: CharArray?, start: Int, length: Int, currentEntity: Entity) {
        stringBuilder.append(ch, start, length)
    }

    override fun endElement(uri: String?, localName: String?, qName: String?, currentEntity: Entity) {
        val arrivalTime = stringBuilder.toString().trim()
        val serviceJourneyId = activeDatesModel.currentServiceJourneyId
        if (serviceJourneyId != null && arrivalTime.isNotEmpty()) {
            val arrivalTime = LocalTime.parse(arrivalTime)
            activeDatesModel.serviceJourneyToFinalArrivalTimeMap.put(serviceJourneyId, arrivalTime)
        }
        stringBuilder.clear()
    }
}

