package org.entur.netex.tools.lib.plugin.activedates

import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.plugin.activedates.model.Period
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class ActiveDatesRepository (
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
    val datedServiceJourneyToOperatingDayRefMap: MutableMap<String, String> = mutableMapOf(),

    val serviceJourneyToFinalArrivalTimeMap: MutableMap<String, LocalTime> = mutableMapOf(),
    val serviceJourneyToFinalArrivalDayOffsetMap: MutableMap<String, Int> = mutableMapOf(),
) {
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
                    // Check direct references
                    val directlyReferenced = serviceJourneyToOperatingDayRefMap.any { (sjId, operatingDayRefs) ->
                        allActiveServiceJourneys.contains(sjId) && operatingDayRefs.contains(entityId)
                    }
                    
                    // Check indirect references through operating periods
                    val indirectlyReferencedThroughPeriods = if (!directlyReferenced) {
                        // Find operating periods that reference this operating day
                        val referencingOperatingPeriods = operatingPeriodIdToFromDateRefMap.filter { (_, fromDateRef) ->
                            fromDateRef == entityId
                        }.keys + operatingPeriodIdToToDateRefMap.filter { (_, toDateRef) ->
                            toDateRef == entityId
                        }.keys
                        
                        // Check if any of these operating periods are referenced by day types of active service journeys
                        referencingOperatingPeriods.any { operatingPeriodRef ->
                            dayTypeRefToOperatingPeriodRefMap.any { (dayTypeRef, operatingPeriodRefs) ->
                                operatingPeriodRefs.contains(operatingPeriodRef) &&
                                serviceJourneyToDayTypeRefMap.any { (sjId, sjDayTypeRefs) ->
                                    allActiveServiceJourneys.contains(sjId) && sjDayTypeRefs.contains(dayTypeRef)
                                }
                            }
                        }
                    } else false
                    
                    // Check indirect references through day type assignments
                    val indirectlyReferencedThroughDayTypes = if (!directlyReferenced && !indirectlyReferencedThroughPeriods) {
                        // Find day types that reference this operating day through day type assignments
                        val referencingDayTypes = dayTypeRefToOperatingDayRefMap.filter { (_, operatingDayRefs) ->
                            operatingDayRefs.contains(entityId)
                        }.keys
                        
                        // Check if any of these day types are referenced by active service journeys
                        referencingDayTypes.any { dayTypeRef ->
                            serviceJourneyToDayTypeRefMap.any { (sjId, sjDayTypeRefs) ->
                                allActiveServiceJourneys.contains(sjId) && sjDayTypeRefs.contains(dayTypeRef)
                            }
                        }
                    } else false
                    
                    directlyReferenced || indirectlyReferencedThroughPeriods || indirectlyReferencedThroughDayTypes
                }
                NetexTypes.OPERATING_PERIOD -> {
                    // Find day types that reference this operating period
                    val referencingDayTypes = dayTypeRefToOperatingPeriodRefMap.filter { (_, operatingPeriodRefs) ->
                        operatingPeriodRefs.contains(entityId)
                    }.keys
                    
                    // If no day types reference this operating period, it's inactive
                    if (referencingDayTypes.isEmpty()) {
                        false
                    } else {
                        // Check if any of the referencing day types are referenced by active service journeys
                        referencingDayTypes.any { dayTypeRef ->
                            serviceJourneyToDayTypeRefMap.any { (sjId, sjDayTypeRefs) ->
                                allActiveServiceJourneys.contains(sjId) && sjDayTypeRefs.contains(dayTypeRef)
                            }
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

        return inactiveEntities
    }
}