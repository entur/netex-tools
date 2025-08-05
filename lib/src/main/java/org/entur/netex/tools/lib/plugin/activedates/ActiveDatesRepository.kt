package org.entur.netex.tools.lib.plugin.activedates

import org.entur.netex.tools.lib.plugin.activedates.data.DayTypeData
import org.entur.netex.tools.lib.plugin.activedates.data.OperatingPeriodData
import org.entur.netex.tools.lib.plugin.activedates.data.VehicleJourneyData
import java.time.LocalDate

class ActiveDatesRepository(
    val dayTypes: MutableMap<String, DayTypeData> = mutableMapOf(),
    val operatingPeriods: MutableMap<String, OperatingPeriodData> = mutableMapOf(),
    val operatingDays: MutableMap<String, LocalDate> = mutableMapOf(),
    val serviceJourneys: MutableMap<String, VehicleJourneyData> = mutableMapOf(),
    val deadRuns: MutableMap<String, VehicleJourneyData> = mutableMapOf(),
    val datedServiceJourneyToOperatingDays: MutableMap<String, String> = mutableMapOf(),
) {
    fun getDayTypeData(dayTypeId: String): DayTypeData =
        dayTypes.getOrPut(dayTypeId) { DayTypeData() }
    
    fun getServiceJourneyData(serviceJourneyId: String): VehicleJourneyData =
        serviceJourneys.getOrPut(serviceJourneyId) { VehicleJourneyData() }
    
    fun getOperatingPeriodData(operatingPeriodId: String): OperatingPeriodData = 
        operatingPeriods.getOrPut(operatingPeriodId) { OperatingPeriodData() }

    fun getDeadRunData(deadRunId: String): VehicleJourneyData =
        deadRuns.getOrPut(deadRunId) { VehicleJourneyData() }
}