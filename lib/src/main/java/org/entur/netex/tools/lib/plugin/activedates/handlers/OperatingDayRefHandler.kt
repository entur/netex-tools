package org.entur.netex.tools.lib.plugin.activedates.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.plugin.activedates.NetexDataCollector
import org.xml.sax.Attributes

class OperatingDayRefHandler(val activeDatesRepository: ActiveDatesRepository) : NetexDataCollector() {
    override fun startElement(
        attributes: Attributes?,
        currentEntity: Entity
    ) {
        if (currentEntity.type == NetexTypes.DAY_TYPE_ASSIGNMENT) {
            val ref = attributes?.getValue("ref")
            if (ref != null) {
                activeDatesRepository.currentDayTypeAssignmentOperatingDay = ref
            }
        }
        if (currentEntity.type == NetexTypes.DATED_SERVICE_JOURNEY) {
            activeDatesRepository.currentOperatingDayRef = attributes?.getValue("ref")
        }
    }
}