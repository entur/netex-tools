package org.entur.netex.tools.lib.plugin.activedates.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesParsingContext
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.plugin.activedates.NetexDataCollector

class DayTypeAssignmentHandler(
    val activeDatesRepository: ActiveDatesRepository
) : NetexDataCollector() {

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
            activeDatesRepository.dayTypeAssignmentToDate.put(currentEntity.id, it)
        }

        context.currentDayTypeAssignmentDayTypeRef = null
        context.currentDayTypeAssignmentOperatingDay = null
        context.currentDayTypeAssignmentOperatingPeriod = null
        context.currentDayTypeAssignmentDate = null
    }
}