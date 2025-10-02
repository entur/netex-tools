package org.entur.netex.tools.lib.plugin.activedates.data

import org.entur.netex.tools.lib.model.EntityId
import java.time.DayOfWeek
import java.time.LocalDate

data class DayTypeData(
    val operatingPeriods: MutableList<EntityId.Simple> = mutableListOf(),
    val dates: MutableList<LocalDate> = mutableListOf(),
    val operatingDays: MutableList<EntityId.Simple> = mutableListOf(),
    val daysOfWeek: MutableSet<DayOfWeek> = mutableSetOf(),
)
