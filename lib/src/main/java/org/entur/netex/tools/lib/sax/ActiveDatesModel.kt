package org.entur.netex.tools.lib.sax

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
    fun serviceJourneysToKeep() : List<String> {
        // The latest date for every dayType
        val dayTypeRefToLatestLocalDate = mutableMapOf<String, LocalDate>()

        dayTypeRefToDateMap.entries.forEach {
            val dayTypeRef = it.key
            val dates = it.value
            if (dates.isNotEmpty()) {
                val latestDate = dates.maxOrNull() ?: LocalDate.MIN
                dayTypeRefToLatestLocalDate[dayTypeRef] = latestDate
            }
        }

        dayTypeRefToOperatingDayRefMap.entries.forEach {
            val dayTypeRef = it.key
            val dates = it.value.stream().map({ o -> operatingDayToCalendarDateMap[o]!! }).toList()
            if (dates.isNotEmpty()) {
                val latestDate = dates.maxOrNull() ?: LocalDate.MIN
                dayTypeRefToLatestLocalDate[dayTypeRef] = latestDate
            }
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
                            if (dayTypeRefToLatestLocalDate.containsKey(dayTypeRef)) {
                                if (it.isAfter(dayTypeRefToLatestLocalDate[dayTypeRef]!!)) {
                                    dayTypeRefToLatestLocalDate[dayTypeRef] = it
                                }
                            } else {
                                dayTypeRefToLatestLocalDate[dayTypeRef] = it
                            }
                        }
                    }
            }
        }

        val serviceJourneyLastActiveDate = mutableMapOf<String, LocalDate>()
        serviceJourneyToDayTypeRefMap.forEach {
            val serviceJourneyId = it.key
            val dayTypeRefs = it.value

            val sjLastArrivalTime = serviceJourneyToFinalArrivalTimeMap[serviceJourneyId]
            val sjLastArrivalDayOffset = serviceJourneyToFinalArrivalDayOffsetMap[serviceJourneyId] ?: 0

            dayTypeRefs.forEach { dayTypeRef ->
                if (dayTypeRefToLatestLocalDate.containsKey(dayTypeRef)) {
                    val latestDayTypeDate = dayTypeRefToLatestLocalDate[dayTypeRef]!!
                    val latestDate = if (sjLastArrivalTime != null) {
                        latestDayTypeDate.plusDays(sjLastArrivalDayOffset.toLong())//.with(sjLastArrivalTime)
                    } else {
                        latestDayTypeDate
                    }

                    if (serviceJourneyLastActiveDate.containsKey(serviceJourneyId)) {
                        if (latestDate.isAfter(serviceJourneyLastActiveDate[serviceJourneyId]!!)) {
                            serviceJourneyLastActiveDate[serviceJourneyId] = latestDate
                        }
                    } else {
                        serviceJourneyLastActiveDate[serviceJourneyId] = latestDate
                    }
                }
            }

            serviceJourneyToOperatingDayRefMap.forEach {
                val serviceJourneyId = it.key
                val operatingDayRefs = it.value
                if (operatingDayRefs.isNotEmpty()) {
                    operatingDayRefs.forEach { operatingDayRef ->
                        val calendarDate = operatingDayToCalendarDateMap[operatingDayRef]
                        if (calendarDate != null) {
                            val latestDate = if (sjLastArrivalTime != null) {
                                calendarDate.plusDays(sjLastArrivalDayOffset.toLong())//.with(sjLastArrivalTime)
                            } else {
                                calendarDate
                            }
                            if (serviceJourneyLastActiveDate.containsKey(serviceJourneyId)) {
                                if (latestDate.isAfter(serviceJourneyLastActiveDate[serviceJourneyId]!!)) {
                                    serviceJourneyLastActiveDate[serviceJourneyId] = latestDate
                                }
                            } else {
                                serviceJourneyLastActiveDate[serviceJourneyId] = latestDate
                            }
                        }
                    }
                }
            }
        }

        val twoDaysAgo = LocalDate.now().minusDays(2)
        return serviceJourneyLastActiveDate.filter {
            !it.value.isBefore(twoDaysAgo)
        }.keys.toList()
    }
}