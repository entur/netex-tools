package org.entur.netex.tools.lib.sax.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.sax.ActiveDatesModel
import org.entur.netex.tools.lib.sax.NetexDataCollector
import org.xml.sax.Attributes

class OperatingPeriodRefHandler(val activeDatesModel: ActiveDatesModel) : NetexDataCollector() {
    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?,
        currentEntity: Entity
    ) {
        if (currentEntity.type == "DayTypeAssignment") {
            val ref = attributes?.getValue("ref")
            if (ref != null) {
                activeDatesModel.currentDayTypeAssignmentOperatingPeriod = ref
            }
        }
    }
}