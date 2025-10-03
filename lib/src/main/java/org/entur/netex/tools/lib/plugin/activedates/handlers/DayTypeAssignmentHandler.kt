package org.entur.netex.tools.lib.plugin.activedates.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesParsingContext
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.plugin.activedates.NetexDataCollector
import org.xml.sax.Attributes

class DayTypeAssignmentHandler(
    val activeDatesRepository: ActiveDatesRepository
) : NetexDataCollector() {
    override fun startElement(context: ActiveDatesParsingContext, attributes: Attributes?, currentEntity: Entity) {
        context.currentDayTypeAssignmentId = attributes?.getValue("id")
        context.currentDayTypeAssignmentVersion = attributes?.getValue("version")
        context.currentDayTypeAssignmentOrder = attributes?.getValue("order")
    }

    override fun endElement(context: ActiveDatesParsingContext, currentEntity: Entity) {
        if (context.currentDayTypeAssignmentDayTypeRef == null) {
            return
        }
        context.currentDayTypeAssignmentOperatingDay?.let {
            activeDatesRepository.getDayTypeData(context.currentDayTypeAssignmentDayTypeRef!!)
                .operatingDays.add(it)
        }

        context.currentDayTypeAssignmentOperatingPeriod?.let {
            activeDatesRepository.getDayTypeData(context.currentDayTypeAssignmentDayTypeRef!!)
                .operatingPeriods.add(it)
        }

        context.currentDayTypeAssignmentDate?.let {
            activeDatesRepository.getDayTypeData(context.currentDayTypeAssignmentDayTypeRef!!)
                .dates.add(it)
        }
        context.currentDayTypeAssignmentDayTypeRef = null
        context.currentDayTypeAssignmentOperatingDay = null
        context.currentDayTypeAssignmentOperatingPeriod = null
        context.currentDayTypeAssignmentDate = null
    }
}