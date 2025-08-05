package org.entur.netex.tools.lib.plugin.activedates.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesParsingContext
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.plugin.activedates.NetexDataCollector

/**
 * Handler to collect ArrivalDayOffset values as they are encountered in the XML.
 * The last value encountered for a ServiceJourney will be stored.
 */
class ArrivalDayOffsetHandler(private val activeDatesRepository: ActiveDatesRepository) : NetexDataCollector() {
    private val stringBuilder = StringBuilder()

    override fun characters(context: ActiveDatesParsingContext, ch: CharArray?, start: Int, length: Int) {
        stringBuilder.append(ch, start, length)
    }

    override fun endElement(context: ActiveDatesParsingContext, currentEntity: Entity) {
        val arrivalDayOffset = stringBuilder.toString().trim()
        currentEntity.grandParent()?.let { grandParentEntity ->
            when (grandParentEntity.type) {
                NetexTypes.DEAD_RUN -> {
                    val deadRunId = context.currentDeadRunId
                    if (deadRunId != null && arrivalDayOffset.isNotEmpty()) {
                        activeDatesRepository.getDeadRunData(deadRunId).finalArrivalDayOffset = arrivalDayOffset.toLong()
                    }
                }
                NetexTypes.SERVICE_JOURNEY -> {
                    val serviceJourneyId = context.currentServiceJourneyId
                    if (serviceJourneyId != null && arrivalDayOffset.isNotEmpty()) {
                        activeDatesRepository.getServiceJourneyData(serviceJourneyId).finalArrivalDayOffset = arrivalDayOffset.toLong()
                    }
                }
            }
        }
        stringBuilder.clear()
    }
}
