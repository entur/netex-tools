package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.model.NetexTypes
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
    val datedServiceJourneyToOperatingDayRefMap: MutableMap<String, String> = mutableMapOf(),

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

    /**
     * Get day types that have dates within the specified range.
     */
    fun dayTypesToKeep(startDate: LocalDate, endDate: LocalDate): List<String> {
        val activeDayTypes = mutableSetOf<String>()
        
        // Check DayTypeAssignments with direct dates
        dayTypeRefToDateMap.forEach { (dayTypeRef, dates) ->
            if (dates.any { date -> !date.isBefore(startDate) && !date.isAfter(endDate) }) {
                activeDayTypes.add(dayTypeRef)
            }
        }
        
        // Check DayTypeAssignments with operating days
        dayTypeRefToOperatingDayRefMap.forEach { (dayTypeRef, operatingDayRefs) ->
            val hasActiveDates = operatingDayRefs.any { operatingDayRef ->
                val calendarDate = operatingDayToCalendarDateMap[operatingDayRef]
                calendarDate != null && !calendarDate.isBefore(startDate) && !calendarDate.isAfter(endDate)
            }
            if (hasActiveDates) {
                activeDayTypes.add(dayTypeRef)
            }
        }
        
        // Check DayTypeAssignments with operating periods
        dayTypeRefToOperatingPeriodRefMap.forEach { (dayTypeRef, operatingPeriodRefs) ->
            val hasActiveDates = operatingPeriodRefs.any { periodRef ->
                periodOverlapsDateRange(periodRef, startDate, endDate)
            }
            if (hasActiveDates) {
                activeDayTypes.add(dayTypeRef)
            }
        }
        
        return activeDayTypes.toList()
    }

    /**
     * Get operating periods that overlap with the specified date range.
     */
    fun operatingPeriodsToKeep(startDate: LocalDate, endDate: LocalDate): List<String> {
        return operatingPeriodIdToPeriodMap.keys.filter { periodRef ->
            periodOverlapsDateRange(periodRef, startDate, endDate)
        }
    }

    /**
     * Get day type assignments that reference active day types or operating periods within the date range.
     */
    fun dayTypeAssignmentsToKeep(startDate: LocalDate, endDate: LocalDate): List<String> {
        val activeDayTypes = dayTypesToKeep(startDate, endDate).toSet()
        val activeOperatingPeriods = operatingPeriodsToKeep(startDate, endDate).toSet()
        
        val activeDayTypeAssignments = mutableSetOf<String>()
        
        // Find assignments that reference active day types
        dayTypeRefToDateMap.keys.forEach { dayTypeRef ->
            if (activeDayTypes.contains(dayTypeRef)) {
                // In a real implementation, we'd need to track assignment IDs
                // For now, we use the dayTypeRef as a proxy
                activeDayTypeAssignments.add(dayTypeRef)
            }
        }
        
        return activeDayTypeAssignments.toList()
    }

    /**
     * Get all entities (by type and ID) that have dates within the specified range.
     */
    fun entitiesWithinDateRange(startDate: LocalDate, endDate: LocalDate): Map<String, List<String>> {
        return mapOf(
            NetexTypes.DAY_TYPE to dayTypesToKeep(startDate, endDate),
            NetexTypes.OPERATING_PERIOD to operatingPeriodsToKeep(startDate, endDate),
            NetexTypes.DAY_TYPE_ASSIGNMENT to dayTypeAssignmentsToKeep(startDate, endDate),
            NetexTypes.OPERATING_DAY to operatingDayToCalendarDateMap.filter { (_, date) ->
                !date.isBefore(startDate) && !date.isAfter(endDate)
            }.keys.toList()
        )
    }

    /**
     * Calculate the last active date for each service journey.
     * This is extracted from the existing serviceJourneysToKeep() method for reuse.
     */
    private fun calculateServiceJourneyLastActiveDates(): Map<String, LocalDate> {
        val dayTypeRefToLatestLocalDate = calculateDayTypeRefToLatestLocalDate()

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
                        latestDayTypeDate.plusDays(sjLastArrivalDayOffset.toLong())
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
                                calendarDate.plusDays(sjLastArrivalDayOffset.toLong())
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

        return serviceJourneyLastActiveDate
    }

    private fun calculateDayTypeRefToLatestLocalDate(): MutableMap<String, LocalDate> {
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
        return dayTypeRefToLatestLocalDate
    }

    /**
     * Check if an operating period overlaps with the specified date range.
     */
    private fun periodOverlapsDateRange(periodRef: String, startDate: LocalDate, endDate: LocalDate): Boolean {
        val fromDate = if (operatingPeriodIdToPeriodMap[periodRef]?.fromDate != null) {
            operatingPeriodIdToPeriodMap[periodRef]?.fromDate!!
        } else {
            operatingDayToCalendarDateMap[operatingPeriodIdToFromDateRefMap[periodRef]]
        }
        
        val toDate = if (operatingPeriodIdToPeriodMap[periodRef]?.toDate != null) {
            operatingPeriodIdToPeriodMap[periodRef]?.toDate!!
        } else {
            operatingDayToCalendarDateMap[operatingPeriodIdToToDateRefMap[periodRef]]
        }
        
        return fromDate != null && toDate != null && 
               !toDate.isBefore(startDate) && !fromDate.isAfter(endDate)
    }

    /**
     * Get entities that are inactive after the specified end date.
     * This includes ServiceJourneys that have no active dates after the endDate,
     * and date-related entities (DayType, OperatingPeriod, etc.) that are only
     * referenced by inactive ServiceJourneys.
     */
    fun getEntitiesInactiveAfter(endDate: LocalDate): Map<String, Set<String>> {
        val inactiveEntities = mutableMapOf<String, MutableSet<String>>()

        // 1. Identify inactive ServiceJourneys
        val serviceJourneyLastActiveDate = calculateServiceJourneyLastActiveDates()
        val inactiveServiceJourneys = serviceJourneyLastActiveDate.filter { (_, lastActiveDate) ->
            lastActiveDate.isBefore(endDate)
        }.keys.toSet()

        if (inactiveServiceJourneys.isNotEmpty()) {
            inactiveEntities.computeIfAbsent(NetexTypes.SERVICE_JOURNEY) { mutableSetOf() }.addAll(inactiveServiceJourneys)
        }

        // 1a. Identify inactive DatedServiceJourneys
        val inactiveDatedServiceJourneys = datedServiceJourneyToOperatingDayRefMap.filter { (_, operatingDayRef) ->
            operatingDayToCalendarDateMap[operatingDayRef]?.isBefore(endDate) ?: true
        }.keys.toSet()
        if (inactiveDatedServiceJourneys.isNotEmpty()) {
            inactiveEntities.computeIfAbsent(NetexTypes.DATED_SERVICE_JOURNEY) { mutableSetOf() }.addAll(inactiveDatedServiceJourneys)
        }

        // 2. Identify date-related entities that are only referenced by inactive ServiceJourneys
        val allActiveServiceJourneys = serviceJourneyLastActiveDate.filter { (_, lastActiveDate) ->
            !lastActiveDate.isBefore(endDate)
        }.keys.toSet()

        // Helper to check if a date entity is referenced by any active service journey
        fun isReferencedByActiveServiceJourney(entityId: String, entityType: String): Boolean {
            return when (entityType) {
                NetexTypes.DAY_TYPE -> {
                    serviceJourneyToDayTypeRefMap.any { (sjId, dayTypeRefs) ->
                        allActiveServiceJourneys.contains(sjId) && dayTypeRefs.contains(entityId)
                    }
                }
                NetexTypes.OPERATING_DAY -> {
                    serviceJourneyToOperatingDayRefMap.any { (sjId, operatingDayRefs) ->
                        allActiveServiceJourneys.contains(sjId) && operatingDayRefs.contains(entityId)
                    }
                }
                NetexTypes.OPERATING_PERIOD -> {
                    dayTypeRefToOperatingPeriodRefMap.any { (dayTypeRef, operatingPeriodRefs) ->
                        operatingPeriodRefs.contains(entityId) &&
                                serviceJourneyToDayTypeRefMap.any { (sjId, sjDayTypeRefs) ->
                                    allActiveServiceJourneys.contains(sjId) && sjDayTypeRefs.contains(dayTypeRef)
                                }
                    }
                }
                // Add other date-related types as needed
                else -> false
            }
        }

        // Check DayTypes
        val dayTypeRefToLatestLocalDate = calculateDayTypeRefToLatestLocalDate()
        dayTypeRefToLatestLocalDate.keys.forEach { dayTypeRef ->
            if (!isReferencedByActiveServiceJourney(dayTypeRef, NetexTypes.DAY_TYPE)) {
                inactiveEntities.computeIfAbsent(NetexTypes.DAY_TYPE) { mutableSetOf() }.add(dayTypeRef)
            }
        }

        // Check OperatingDays
        operatingDayToCalendarDateMap.keys.forEach { operatingDayRef ->
            if (!isReferencedByActiveServiceJourney(operatingDayRef, NetexTypes.OPERATING_DAY)) {
                inactiveEntities.computeIfAbsent(NetexTypes.OPERATING_DAY) { mutableSetOf() }.add(operatingDayRef)
            }
        }

        // Check OperatingPeriods
        operatingPeriodIdToPeriodMap.keys.forEach { operatingPeriodRef ->
            if (!isReferencedByActiveServiceJourney(operatingPeriodRef, NetexTypes.OPERATING_PERIOD)) {
                inactiveEntities.computeIfAbsent(NetexTypes.OPERATING_PERIOD) { mutableSetOf() }.add(operatingPeriodRef)
            }
        }

        // TODO: Add logic for DayTypeAssignment if they have their own IDs and need to be removed

        return inactiveEntities
    }
}