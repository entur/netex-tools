package org.entur.netex.tools.lib.plugin.activedates.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesParsingContext
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.plugin.activedates.NetexDataCollector
import org.xml.sax.Attributes

class OperatingDayRefHandler(val activeDatesRepository: ActiveDatesRepository) : NetexDataCollector() {
    override fun startElement(
        context: ActiveDatesParsingContext,
        attributes: Attributes?,
        currentEntity: Entity
    ) {
        if (currentEntity.type == NetexTypes.DAY_TYPE_ASSIGNMENT) {
            val ref = attributes?.getValue("ref")
            if (ref != null) {
                context.currentDayTypeAssignmentOperatingDay = ref
            }
        }
        if (currentEntity.type == NetexTypes.DATED_SERVICE_JOURNEY) {
            context.currentOperatingDayRef = attributes?.getValue("ref")
        }
    }
}