package org.entur.netex.tools.lib.plugin.activedates.data

import org.entur.netex.tools.lib.model.EntityId
import java.time.LocalTime

data class VehicleJourneyData(
    val dayTypes: MutableList<EntityId.Simple> = mutableListOf(),
    val operatingDays: MutableList<EntityId.Simple> = mutableListOf(),
    var finalArrivalTime: LocalTime? = null,
    var finalArrivalDayOffset: Long = 0
)
