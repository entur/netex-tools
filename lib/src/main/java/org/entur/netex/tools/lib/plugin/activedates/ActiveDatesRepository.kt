package org.entur.netex.tools.lib.plugin.activedates

import org.entur.netex.tools.lib.plugin.activedates.data.DayTypeData
import org.entur.netex.tools.lib.plugin.activedates.data.OperatingPeriodData
import org.entur.netex.tools.lib.plugin.activedates.data.ServiceJourneyData
import java.time.LocalDate

class ActiveDatesRepository(
    val dayTypes: MutableMap<String, DayTypeData> = mutableMapOf(),
    val operatingPeriods: MutableMap<String, OperatingPeriodData> = mutableMapOf(),
    val operatingDays: MutableMap<String, LocalDate> = mutableMapOf(),
    val serviceJourneys: MutableMap<String, ServiceJourneyData> = mutableMapOf(),
    val datedServiceJourneyToOperatingDays: MutableMap<String, String> = mutableMapOf()
) {
    fun getDayTypeData(dayTypeId: String): DayTypeData =
        dayTypes.getOrPut(dayTypeId) { DayTypeData() }
    
    fun getServiceJourneyData(serviceJourneyId: String): ServiceJourneyData = 
        serviceJourneys.getOrPut(serviceJourneyId) { ServiceJourneyData() }
    
    fun getOperatingPeriodData(operatingPeriodId: String): OperatingPeriodData = 
        operatingPeriods.getOrPut(operatingPeriodId) { OperatingPeriodData() }
}