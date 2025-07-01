package org.entur.netex.tools.lib.plugin.activedates.handlers

import org.entur.netex.tools.lib.extensions.putOrAddToExistingList
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.plugin.activedates.NetexDataCollector
import org.xml.sax.Attributes

class DayTypeRefHandler(val activeDatesRepository: ActiveDatesRepository) : NetexDataCollector() {
    override fun startElement(
        attributes: Attributes?,
        currentEntity: Entity
    ) {
        val ref = attributes?.getValue("ref")
        if (currentEntity.type == NetexTypes.DAY_TYPE_ASSIGNMENT) {
            if (ref != null) {
                activeDatesRepository.currentDayTypeAssignmentDayTypeRef = ref
            }
        }
        if (currentEntity.type == NetexTypes.SERVICE_JOURNEY) {
            if (ref != null) {
                val serviceJourneyId = currentEntity.id
                activeDatesRepository.serviceJourneyToDayTypeRefMap.putOrAddToExistingList(serviceJourneyId, ref)
            }
        }
    }
}