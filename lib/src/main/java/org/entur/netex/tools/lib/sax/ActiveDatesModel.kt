package org.entur.netex.tools.lib.sax

import java.time.LocalDate

data class ActiveDatesModel(
    val operatingDayToCalendarDateMap: MutableMap<String, LocalDate> = mutableMapOf(),
    val dayTypeRefToDateMap: Map<String, LocalDate> = mutableMapOf(),
    val dayTypeRefToOperatingDayRefMap: Map<String, String> = mutableMapOf(),
    val dayTypeRefToOperatingPeriodRefMap: Map<String, String> = mutableMapOf(),
    val dayTypeToDaysOfWeek: Map<String, String> = mutableMapOf(),
)