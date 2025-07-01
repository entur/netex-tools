package org.entur.netex.tools.lib.plugin.activedates.handlers

import org.entur.netex.tools.lib.extensions.putOrAddToExistingList
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.plugin.activedates.NetexDataCollector

class DayTypeAssignmentHandler(
    val activeDatesRepository: ActiveDatesRepository
) : NetexDataCollector() {

    override fun endElement(currentEntity: Entity) {
        if (activeDatesRepository.currentDayTypeAssignmentDayTypeRef == null) {
            return
        }
        activeDatesRepository.currentDayTypeAssignmentOperatingDay?.let {
            activeDatesRepository.dayTypeRefToOperatingDayRefMap.putOrAddToExistingList(
                activeDatesRepository.currentDayTypeAssignmentDayTypeRef!!, it
            )
        }

        activeDatesRepository.currentDayTypeAssignmentOperatingPeriod?.let {
            activeDatesRepository.dayTypeRefToOperatingPeriodRefMap.putOrAddToExistingList(
                activeDatesRepository.currentDayTypeAssignmentDayTypeRef!!, it
            )
        }

        activeDatesRepository.currentDayTypeAssignmentDate?.let {
            activeDatesRepository.dayTypeRefToDateMap.putOrAddToExistingList(
                activeDatesRepository.currentDayTypeAssignmentDayTypeRef!!, it
            )
        }
        activeDatesRepository.currentDayTypeAssignmentDayTypeRef = null
        activeDatesRepository.currentDayTypeAssignmentOperatingDay = null
        activeDatesRepository.currentDayTypeAssignmentOperatingPeriod = null
        activeDatesRepository.currentDayTypeAssignmentDate = null
    }
}