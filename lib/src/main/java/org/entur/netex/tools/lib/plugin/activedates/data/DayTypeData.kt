package org.entur.netex.tools.lib.plugin.activedates.data

import java.time.DayOfWeek
import java.time.LocalDate

data class DayTypeData(
    val operatingPeriods: MutableList<String> = mutableListOf(),
    val daysOfWeek: MutableSet<DayOfWeek> = mutableSetOf(),
    val dates: MutableList<LocalDate> = mutableListOf(),
    val operatingDays: MutableList<String> = mutableListOf()
)