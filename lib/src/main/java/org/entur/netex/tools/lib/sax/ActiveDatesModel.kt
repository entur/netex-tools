package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.extensions.putOrAddToExistingList
import org.entur.netex.tools.lib.sax.model.Period
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class ActiveDatesModel(
    val operatingDayToCalendarDateMap: MutableMap<String, LocalDate> = mutableMapOf(),

    // operatingPeriods
    val operatingPeriodIdToPeriodMap: MutableMap<String, Period> = mutableMapOf(),
    val operatingPeriodIdToFromDateRefMap: MutableMap<String, String> = mutableMapOf(),
    val operatingPeriodIdToToDateRefMap: MutableMap<String, String> = mutableMapOf(),

    // DayTypeAssignment
    val dayTypeRefToDateMap: MutableMap<String, MutableList<LocalDate>> = mutableMapOf(),
    val dayTypeRefToOperatingDayRefMap: MutableMap<String, MutableList<String>> = mutableMapOf(),
    val dayTypeRefToOperatingPeriodRefMap: MutableMap<String, MutableList<String>> = mutableMapOf(),

    val dayTypeToDaysOfWeek: MutableMap<String, MutableSet<DayOfWeek>> = mutableMapOf(),

    val serviceJourneyToDayTypeRefMap: MutableMap<String, MutableList<String>> = mutableMapOf(),

    // ServiceJourneyId->OperatingDay[] via DatedServiceJourney
    val serviceJourneyToOperatingDayRefMap: MutableMap<String, MutableList<String>> = mutableMapOf(),
    var currentOperatingDayRef: String? = null,
    var currentServiceJourneyRef: String? = null,

    val serviceJourneyToFinalArrivalTimeMap: MutableMap<String, LocalTime> = mutableMapOf(),
    val serviceJourneyToFinalArrivalDayOffsetMap: MutableMap<String, Int> = mutableMapOf(),
    var currentServiceJourneyId: String? = null,

    var currentDayTypeAssignmentDayTypeRef: String? = null,
    var currentDayTypeAssignmentDate: LocalDate? = null,
    var currentDayTypeAssignmentOperatingDay: String? = null,
    var currentDayTypeAssignmentOperatingPeriod: String? = null,

    var currentDayTypeId: String? = null,
) {
    fun findServiceJourneyIdsArrivingNoEarlierThanTwoDaysFromToday() : Set<String> {
        val dayTypeRefsToLocalDates = mutableMapOf<String, MutableList<LocalDate>>()

        dayTypeRefToDateMap.entries.forEach {
            dayTypeRefsToLocalDates.putOrAddToExistingList(it.key, it.value)
        }

        dayTypeRefToOperatingDayRefMap.entries.forEach {
            val dates = it.value.stream().map({ o -> operatingDayToCalendarDateMap[o]!! }).toList()
            dayTypeRefsToLocalDates.putOrAddToExistingList(it.key, dates)
        }

        dayTypeRefToOperatingPeriodRefMap.entries.forEach {
            val dayTypeRef = it.key
            val operatingPeriodRefs = it.value
            operatingPeriodRefs.forEach { periodRef ->
                val fromDate = if (operatingPeriodIdToPeriodMap.get(periodRef)?.fromDate != null) {
                    operatingPeriodIdToPeriodMap.get(periodRef)?.fromDate!!
                } else {
                    operatingDayToCalendarDateMap.get(operatingPeriodIdToFromDateRefMap.get(periodRef))
                }
                val toDate = if (operatingPeriodIdToPeriodMap.get(periodRef)?.toDate != null) {
                    operatingPeriodIdToPeriodMap.get(periodRef)?.toDate!!
                } else {
                    operatingDayToCalendarDateMap.get(operatingPeriodIdToToDateRefMap.get(periodRef))
                }

                generateSequence(fromDate) { it.plusDays(1) }
                    .takeWhile { it <= toDate }
                    .forEach {
                        if (dayTypeToDaysOfWeek.get(dayTypeRef)!!.contains(it.dayOfWeek)) {
                            dayTypeRefsToLocalDates.putOrAddToExistingList(dayTypeRef, it)
                        }
                    }

            }
        }

        return setOf()
    }

}