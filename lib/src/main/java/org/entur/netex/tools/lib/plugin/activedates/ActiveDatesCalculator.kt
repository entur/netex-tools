package org.entur.netex.tools.lib.plugin.activedates

import org.entur.netex.tools.lib.config.TimePeriod
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.plugin.activedates.data.VehicleJourneyData
import org.entur.netex.tools.lib.plugin.activedates.helper.ActiveEntitiesCollector
import org.entur.netex.tools.lib.plugin.activedates.model.Period
import java.time.DayOfWeek
import java.time.LocalDate

class ActiveDatesCalculator(private val repository: ActiveDatesRepository) {
    
    fun activeDateEntitiesInPeriod(timePeriod: TimePeriod, entityModel: EntityModel): Map<String, Set<String>> {
        val activeEntities = ActiveEntitiesCollector()

        repository.serviceJourneys.forEach { (serviceJourneyId, serviceJourneyData) ->
            processServiceJourney(
                serviceJourneyId, 
                serviceJourneyData, 
                timePeriod,
                activeEntities
            )
        }

        repository.deadRuns.forEach { (deadRunId, vehicleJourneyData) ->
            processDeadRun(
                deadRunId,
                vehicleJourneyData,
                timePeriod,
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
            if (shouldIncludeDatedServiceJourney(
                serviceJourneyId,
                operatingDayId,
                activeEntities,
                timePeriod
            )) {
                activeEntities.addDatedServiceJourney(datedServiceJourneyId)
            }
        }

        val dayTypeAssignments = entityModel.getEntitiesOfType(NetexTypes.DAY_TYPE_ASSIGNMENT)
        for (dayTypeAssignment in dayTypeAssignments) {
            if (shouldIncludeDayTypeAssignment(dayTypeAssignment, activeEntities, entityModel)) {
                activeEntities.addDayTypeAssignment(dayTypeAssignment.id)
            }
        }

        return activeEntities.toMap()
    }

    fun shouldIncludeDayTypeAssignment(dayTypeAssignmentEntity: Entity, activeEntities: ActiveEntitiesCollector, entityModel: EntityModel): Boolean {
        val dayTypeRefs = entityModel.getRefsOfTypeFrom(dayTypeAssignmentEntity.id, NetexTypes.DAY_TYPE_REF)
        val dayTypeRefValues = dayTypeRefs.map { it.ref }
        return dayTypeRefValues.any( activeEntities.dayTypes()::contains )
    }

    fun shouldIncludeDatedServiceJourney(
        serviceJourneyId: String,
        operatingDayId: String,
        activeEntities: ActiveEntitiesCollector,
        timePeriod: TimePeriod
    ): Boolean {
        if (serviceJourneyId in activeEntities.serviceJourneys() && operatingDayId in activeEntities.operatingDays()) {
            val operatingDayDate = repository.operatingDays.get(operatingDayId)
            val finalArrivalDayOffsetForServiceJourney = repository.serviceJourneys.get(serviceJourneyId)?.finalArrivalDayOffset
            if (operatingDayDate == null || finalArrivalDayOffsetForServiceJourney == null) {
                return false
            }
            val isDateInPeriod = isDateInPeriod(
                date = operatingDayDate,
                dayOffset = finalArrivalDayOffsetForServiceJourney,
                timePeriod = timePeriod
            )
            return isDateInPeriod
        }
        return false
    }

    private fun processServiceJourney(
        serviceJourneyId: String,
        vehicleJourneyData: VehicleJourneyData,
        timePeriod: TimePeriod,
        activeEntities: ActiveEntitiesCollector
    ) {
        val dayOffset = vehicleJourneyData.finalArrivalDayOffset
        
        // Process day types
        vehicleJourneyData.dayTypes.forEach { dayTypeId ->
            processDayType(serviceJourneyId, dayTypeId, dayOffset, timePeriod, activeEntities)
        }
        
        // Process direct operating days
        vehicleJourneyData.operatingDays.forEach { operatingDayId ->
            processOperatingDay(serviceJourneyId, operatingDayId, dayOffset, timePeriod, activeEntities)
        }
    }

    private fun processDeadRun(
        deadRunId: String,
        vehicleJourneyData: VehicleJourneyData,
        timePeriod: TimePeriod,
        activeEntities: ActiveEntitiesCollector
    ) {
        val dayOffset = vehicleJourneyData.finalArrivalDayOffset

        vehicleJourneyData.dayTypes.forEach { dayTypeId ->
            processDayType(deadRunId, dayTypeId, dayOffset, timePeriod, activeEntities, isDeadRun = true)
        }

        // Process direct operating days
        vehicleJourneyData.operatingDays.forEach { operatingDayId ->
            processOperatingDay(deadRunId, operatingDayId, dayOffset, timePeriod, activeEntities, isDeadRun = true)
        }
    }
    
    private fun processDayType(
        serviceJourneyId: String,
        dayTypeId: String,
        dayOffset: Long,
        timePeriod: TimePeriod,
        activeEntities: ActiveEntitiesCollector,
        isDeadRun: Boolean = false,
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
                timePeriod,
                activeEntities,
                isDeadRun = isDeadRun
            )
        }
        
        // Process operating days
        dayTypeData.operatingDays.forEach { operatingDayId ->
            processOperatingDay(serviceJourneyId, operatingDayId, dayOffset, timePeriod, activeEntities, isDeadRun) {
                activeEntities.addDayType(dayTypeId)
            }
        }
        
        // Process dates
        dayTypeData.dates.forEach { date ->
            if (isDateInPeriod(date, dayOffset, timePeriod)) {
                if (isDeadRun) {
                    activeEntities.addDeadRun(serviceJourneyId)
                } else {
                    activeEntities.addServiceJourney(serviceJourneyId)
                }
                activeEntities.addDayType(dayTypeId)
            }
        }
    }
    
    private fun processOperatingPeriod(
        vehicleJourneyId: String,
        dayTypeId: String,
        operatingPeriodId: String,
        daysOfWeek: Set<DayOfWeek>,
        dayOffset: Long,
        timePeriod: TimePeriod,
        activeEntities: ActiveEntitiesCollector,
        isDeadRun: Boolean = false,
    ) {
        val period = resolveOperatingPeriod(operatingPeriodId) ?: return
        val activePeriod = filterPeriodByDaysOfWeek(period, daysOfWeek)
        
        if (isPeriodOverlapping(activePeriod, dayOffset, timePeriod)) {
            if (isDeadRun) {
                activeEntities.addDeadRun(vehicleJourneyId)
            } else {
                activeEntities.addServiceJourney(vehicleJourneyId)
            }
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
        vehicleJourneyId: String,
        operatingDayId: String,
        dayOffset: Long,
        timePeriod: TimePeriod,
        activeEntities: ActiveEntitiesCollector,
        isDeadRun: Boolean = false,
        additionalAction: (() -> Unit)? = null,
    ) {
        val calendarDate = repository.operatingDays[operatingDayId] ?: return
        
        if (isDateInPeriod(calendarDate, dayOffset, timePeriod)) {
            if (isDeadRun) {
                activeEntities.addDeadRun(vehicleJourneyId)
            } else {
                activeEntities.addServiceJourney(vehicleJourneyId)
            }
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
    
    private fun isDateInPeriod(date: LocalDate, dayOffset: Long, timePeriod: TimePeriod): Boolean {
        val adjustedDate = date.plusDays(dayOffset)

        if (timePeriod.start == null && timePeriod.end != null) {
            return !date.isAfter(timePeriod.end)
        }

        if (timePeriod.end == null && timePeriod.start != null) {
            return !adjustedDate.isBefore(timePeriod.start)
        }

        return !date.isAfter(timePeriod.end) && !adjustedDate.isBefore(timePeriod.start)
    }
    
    private fun isPeriodOverlapping(period: Period, dayOffset: Long, timePeriod: TimePeriod): Boolean {
        val fromDate = period.fromDate ?: return false
        val toDate = period.toDate ?: return false

        val adjustedToDate = toDate.plusDays(dayOffset)

        if (timePeriod.start == null && timePeriod.end != null) {
            return !fromDate.isAfter(timePeriod.end)
        }

        if (timePeriod.end == null && timePeriod.start != null) {
            return !adjustedToDate.isBefore(timePeriod.start)
        }

        return !fromDate.isAfter(timePeriod.end) && !adjustedToDate.isBefore(timePeriod.start)
    }
}
