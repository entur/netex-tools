package org.entur.netex.tools.lib.selectors.refs

import org.entur.netex.tools.lib.config.TimePeriod
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesPlugin
import org.entur.netex.tools.lib.plugin.activedates.data.DayTypeData
import org.entur.netex.tools.lib.selections.RefSelection
import java.time.LocalDate

class ActiveDatesRefSelector(val activeDatesPlugin: ActiveDatesPlugin, val period: TimePeriod): RefSelector() {

    private fun isDateInPeriod(date: LocalDate, dayOffset: Long, startDate: LocalDate, endDate: LocalDate): Boolean {
        val adjustedDate = date.plusDays(dayOffset)
        return !date.isAfter(endDate) && !adjustedDate.isBefore(startDate)
    }

    override fun selectRefs(model: EntityModel): RefSelection {
        val selectedRefs = model.listAllRefs().toMutableSet()
        val repository = activeDatesPlugin.getCollectedData()
        val serviceJourneys = repository.serviceJourneys
        val dayTypes = repository.dayTypes

        for ((serviceJourneyId, serviceJourneyData) in serviceJourneys) {
            val dayTypesOfServiceJourney = mutableMapOf<String, DayTypeData>()
            serviceJourneyData.dayTypes.forEach { dayTypesOfServiceJourney.put(it, dayTypes[it]!!) }
            for ((dayTypeRef, dayTypeData) in dayTypesOfServiceJourney) {
                val datesWithinPeriod = dayTypeData.dates.filter {
                    isDateInPeriod(it, serviceJourneyData.finalArrivalDayOffset, period.start, period.end)
                }
                if (datesWithinPeriod.isEmpty()) {
                    val refToRemove = model.getRefOfTypeFromSourceIdAndRef(
                        sourceId = serviceJourneyId,
                        ref = dayTypeRef,
                        type = "DayTypeRef"
                    )
                    selectedRefs.remove(refToRemove)
                }
            }
        }

        return RefSelection(selectedRefs)
    }
}