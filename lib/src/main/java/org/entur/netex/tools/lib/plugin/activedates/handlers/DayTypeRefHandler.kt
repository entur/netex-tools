package org.entur.netex.tools.lib.plugin.activedates.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesParsingContext
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.plugin.activedates.NetexDataCollector
import org.xml.sax.Attributes

class DayTypeRefHandler(val activeDatesRepository: ActiveDatesRepository) : NetexDataCollector() {
    override fun startElement(
        context: ActiveDatesParsingContext,
        attributes: Attributes?,
        currentEntity: Entity
    ) {
        val ref = attributes?.getValue("ref")
        if (currentEntity.type == NetexTypes.DAY_TYPE_ASSIGNMENT) {
            if (ref != null) {
                context.currentDayTypeAssignmentDayTypeRef = ref
            }
        }
        if (currentEntity.type == NetexTypes.SERVICE_JOURNEY) {
            if (ref != null) {
                val serviceJourneyId = currentEntity.id
                activeDatesRepository.getServiceJourneyData(serviceJourneyId).dayTypes.add(ref)
            }
        }
        if (currentEntity.type == NetexTypes.DEAD_RUN) {
            if (ref != null) {
                val deadRunId = currentEntity.id
                activeDatesRepository.getDeadRunData(deadRunId).dayTypes.add(ref)
            }
        }
    }
}