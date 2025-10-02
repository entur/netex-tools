package org.entur.netex.tools.lib.plugin.activedates

import org.entur.netex.tools.lib.model.EntityId
import org.entur.netex.tools.lib.plugin.activedates.data.DayTypeData
import org.entur.netex.tools.lib.plugin.activedates.data.OperatingPeriodData
import org.entur.netex.tools.lib.plugin.activedates.data.VehicleJourneyData
import java.time.LocalDate

class ActiveDatesRepository(
    val dayTypes: MutableMap<EntityId, DayTypeData> = mutableMapOf(),
    val operatingPeriods: MutableMap<EntityId.Simple, OperatingPeriodData> = mutableMapOf(),
    val operatingDays: MutableMap<EntityId.Simple, LocalDate> = mutableMapOf(),
    val serviceJourneys: MutableMap<EntityId.Simple, VehicleJourneyData> = mutableMapOf(),
    val deadRuns: MutableMap<EntityId.Simple, VehicleJourneyData> = mutableMapOf(),
    val datedServiceJourneyToOperatingDays: MutableMap<EntityId.Simple, EntityId.Simple> = mutableMapOf(),
) {
    fun getDayTypeData(dayTypeId: EntityId): DayTypeData =
        dayTypes.getOrPut(dayTypeId) { DayTypeData() }
    
    fun getServiceJourneyData(serviceJourneyId: EntityId.Simple): VehicleJourneyData =
        serviceJourneys.getOrPut(serviceJourneyId) { VehicleJourneyData() }
    
    fun getOperatingPeriodData(operatingPeriodId: EntityId.Simple): OperatingPeriodData =
        operatingPeriods.getOrPut(operatingPeriodId) { OperatingPeriodData() }

    fun getDeadRunData(deadRunId: EntityId.Simple): VehicleJourneyData =
        deadRuns.getOrPut(deadRunId) { VehicleJourneyData() }
}