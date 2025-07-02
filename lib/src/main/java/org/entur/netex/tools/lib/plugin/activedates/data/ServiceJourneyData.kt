package org.entur.netex.tools.lib.plugin.activedates.data

import java.time.LocalTime

data class ServiceJourneyData(
    val dayTypes: MutableList<String> = mutableListOf(),
    val operatingDays: MutableList<String> = mutableListOf(),
    var finalArrivalTime: LocalTime? = null,
    var finalArrivalDayOffset: Long = 0
)