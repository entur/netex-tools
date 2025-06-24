package org.entur.netex.tools.lib.sax.handlers

import org.entur.netex.tools.lib.extensions.putOrAddToExistingList
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.sax.ActiveDatesModel
import org.entur.netex.tools.lib.sax.NetexDataCollector
import org.xml.sax.Attributes

class DayTypeRefHandler(val activeDatesModel: ActiveDatesModel) : NetexDataCollector() {
    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?,
        currentEntity: Entity
    ) {
        val ref = attributes?.getValue("ref")
        if (currentEntity.type == "DayTypeAssignment") {
            if (ref != null) {
                activeDatesModel.currentDayTypeAssignmentDayTypeRef = ref
            }
        }
        if (currentEntity.type == "ServiceJourney") {
            if (ref != null) {
                val serviceJourneyId = currentEntity.id
                activeDatesModel.serviceJourneyToDayTypeRefMap.putOrAddToExistingList(serviceJourneyId, ref)
            }
        }
    }
}