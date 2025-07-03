package org.entur.netex.tools.lib.plugin.activedates

import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.plugin.activedates.data.ServiceJourneyData
import org.entur.netex.tools.lib.plugin.activedates.helper.ActiveEntitiesCollector
import org.entur.netex.tools.lib.plugin.activedates.model.Period
import java.time.DayOfWeek
import java.time.LocalDate

class ActiveDatesCalculator(private val repository: ActiveDatesRepository) {
    
    fun activeDateEntitiesInPeriod(startDate: LocalDate, endDate: LocalDate, entityModel: EntityModel): Map<String, Set<String>> {
        val activeEntities = ActiveEntitiesCollector()
        
        repository.serviceJourneys.forEach { (serviceJourneyId, serviceJourneyData) ->
            processServiceJourney(
                serviceJourneyId, 
                serviceJourneyData, 
                startDate, 
                endDate, 
                activeEntities
            )
        }

        val datedServiceJourneys = entityModel.getEntitiesOfType(NetexTypes.DATED_SERVICE_JOURNEY)
        datedServiceJourneys.forEach { (datedServiceJourneyId) ->
            val serviceJourneyRefs = entityModel.getRefsOfTypeFrom(datedServiceJourneyId, NetexTypes.SERVICE_JOURNEY_REF)
            if (serviceJourneyRefs.size != 1) {
                // Invalid dated service journey, skip processing
                return@forEach
            }
            val serviceJourneyId = serviceJourneyRefs[0].ref
            val operatingDayRefs = entityModel.getRefsOfTypeFrom(datedServiceJourneyId, NetexTypes.OPERATING_DAY_REF)
            if (operatingDayRefs.size != 1) {
                // Invalid dated service journey, skip processing
                return@forEach
            }
            val operatingDayId = operatingDayRefs[0].ref
            if (serviceJourneyId in activeEntities.serviceJourneys() && operatingDayId in activeEntities.operatingDays()) {
                activeEntities.addDatedServiceJourney(datedServiceJourneyId)
            }
        }

        val dayTypeAssignments = entityModel.getEntitiesOfType(NetexTypes.DAY_TYPE_ASSIGNMENT)
        dayTypeAssignments.forEach { (dayTypeAssignmentId) ->
            val dayTypeRefs = entityModel.getRefsOfTypeFrom(dayTypeAssignmentId, NetexTypes.DAY_TYPE_REF)
            if (dayTypeRefs.size != 1) {
                // Invalid day type assignment, skip processing
                return@forEach
            }
            val dayTypeId = dayTypeRefs[0].ref

            // Check if the day type is active in the period
            if (dayTypeId in activeEntities.dayTypes()) {
                activeEntities.addDayTypeAssignment(dayTypeAssignmentId)
            }
        }
        
        return activeEntities.toMap()
    }
    
    private fun processServiceJourney(
        serviceJourneyId: String,
        serviceJourneyData: ServiceJourneyData,
        startDate: LocalDate,
        endDate: LocalDate,
        activeEntities: ActiveEntitiesCollector
    ) {
        val dayOffset = serviceJourneyData.finalArrivalDayOffset
        
        // Process day types
        serviceJourneyData.dayTypes.forEach { dayTypeId ->
            processDayType(serviceJourneyId, dayTypeId, dayOffset, startDate, endDate, activeEntities)
        }
        
        // Process direct operating days
        serviceJourneyData.operatingDays.forEach { operatingDayId ->
            processOperatingDay(serviceJourneyId, operatingDayId, dayOffset, startDate, endDate, activeEntities)
        }
    }
    
    private fun processDayType(
        serviceJourneyId: String,
        dayTypeId: String,
        dayOffset: Long,
        startDate: LocalDate,
        endDate: LocalDate,
        activeEntities: ActiveEntitiesCollector
    ) {
        val dayTypeData = repository.dayTypes[dayTypeId] ?: return
        
        // Process operating periods
        dayTypeData.operatingPeriods.forEach { operatingPeriodId ->
            processOperatingPeriod(
                serviceJourneyId, 
                dayTypeId, 
                operatingPeriodId, 
                dayTypeData.daysOfWeek,
                dayOffset, 
                startDate, 
                endDate, 
                activeEntities
            )
        }
        
        // Process operating days
        dayTypeData.operatingDays.forEach { operatingDayId ->
            processOperatingDay(serviceJourneyId, operatingDayId, dayOffset, startDate, endDate, activeEntities) { 
                activeEntities.addDayType(dayTypeId)
            }
        }
        
        // Process dates
        dayTypeData.dates.forEach { date ->
            if (isDateInPeriod(date, dayOffset, startDate, endDate)) {
                activeEntities.addServiceJourney(serviceJourneyId)
                activeEntities.addDayType(dayTypeId)
            }
        }
    }
    
    private fun processOperatingPeriod(
        serviceJourneyId: String,
        dayTypeId: String,
        operatingPeriodId: String,
        daysOfWeek: Set<DayOfWeek>,
        dayOffset: Long,
        startDate: LocalDate,
        endDate: LocalDate,
        activeEntities: ActiveEntitiesCollector
    ) {
        val period = resolveOperatingPeriod(operatingPeriodId) ?: return
        val activePeriod = filterPeriodByDaysOfWeek(period, daysOfWeek)
        
        if (isPeriodOverlapping(activePeriod, dayOffset, startDate, endDate)) {
            activeEntities.addServiceJourney(serviceJourneyId)
            activeEntities.addDayType(dayTypeId)
            activeEntities.addOperatingPeriod(operatingPeriodId)
            
            // Add referenced operating days
            repository.operatingPeriods[operatingPeriodId]?.let { opPeriodData ->
                opPeriodData.fromDateId?.let { activeEntities.addOperatingDay(it) }
                opPeriodData.toDateId?.let { activeEntities.addOperatingDay(it) }
            }
        }
    }
    
    private fun processOperatingDay(
        serviceJourneyId: String,
        operatingDayId: String,
        dayOffset: Long,
        startDate: LocalDate,
        endDate: LocalDate,
        activeEntities: ActiveEntitiesCollector,
        additionalAction: (() -> Unit)? = null
    ) {
        val calendarDate = repository.operatingDays[operatingDayId] ?: return
        
        if (isDateInPeriod(calendarDate, dayOffset, startDate, endDate)) {
            activeEntities.addServiceJourney(serviceJourneyId)
            activeEntities.addOperatingDay(operatingDayId)
            additionalAction?.invoke()
        }
    }
    
    private fun resolveOperatingPeriod(operatingPeriodId: String): Period? {
        val opPeriodData = repository.operatingPeriods[operatingPeriodId] ?: return null
        
        return opPeriodData.period ?: run {
            val fromDateId = opPeriodData.fromDateId ?: return null
            val toDateId = opPeriodData.toDateId ?: return null
            
            val fromDate = repository.operatingDays[fromDateId] ?: return null
            val toDate = repository.operatingDays[toDateId] ?: return null
            
            Period(fromDate, toDate)
        }
    }
    
    private fun filterPeriodByDaysOfWeek(period: Period, daysOfWeek: Set<DayOfWeek>): Period {
        val fromDate = period.fromDate ?: return period
        val toDate = period.toDate ?: return period
        
        val activeDays = generateSequence(fromDate) { it.plusDays(1) }
            .takeWhile { !it.isAfter(toDate) }
            .filter { it.dayOfWeek in daysOfWeek }
            .toList()
        
        return if (activeDays.isEmpty()) {
            period
        } else {
            Period(activeDays.first(), activeDays.last())
        }
    }
    
    private fun isDateInPeriod(date: LocalDate, dayOffset: Long, startDate: LocalDate, endDate: LocalDate): Boolean {
        val adjustedDate = date.plusDays(dayOffset)
        return !date.isAfter(endDate) && !adjustedDate.isBefore(startDate)
    }
    
    private fun isPeriodOverlapping(period: Period, dayOffset: Long, startDate: LocalDate, endDate: LocalDate): Boolean {
        val fromDate = period.fromDate ?: return false
        val toDate = period.toDate ?: return false
        
        return !fromDate.isAfter(endDate) && !toDate.plusDays(dayOffset).isBefore(startDate)
    }
}
