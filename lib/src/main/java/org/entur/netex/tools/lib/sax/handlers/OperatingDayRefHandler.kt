package org.entur.netex.tools.lib.sax.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.sax.ActiveDatesModel
import org.entur.netex.tools.lib.sax.NetexDataCollector
import org.xml.sax.Attributes

class OperatingDayRefHandler(val activeDatesModel: ActiveDatesModel) : NetexDataCollector() {
    override fun startElement(
        attributes: Attributes?,
        currentEntity: Entity
    ) {
        if (currentEntity.type == NetexTypes.DAY_TYPE_ASSIGNMENT) {
            val ref = attributes?.getValue("ref")
            if (ref != null) {
                activeDatesModel.currentDayTypeAssignmentOperatingDay = ref
            }
        }
        if (currentEntity.type == NetexTypes.DATED_SERVICE_JOURNEY) {
            activeDatesModel.currentOperatingDayRef = attributes?.getValue("ref")
        }
    }
}