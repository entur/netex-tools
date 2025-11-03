package org.entur.netex.tools.lib.config

import kotlinx.serialization.Serializable
import java.time.LocalDate

// TODO: For efficiency, consider supporting nullable values for start and end dates.
//       This would allow for ActiveDatesCalculator not to run at all if both are null.
@Serializable
data class TimePeriod(
    @Serializable(with = LocalDateSerializer::class)
    val start : LocalDate?,
    @Serializable(with = LocalDateSerializer::class)
    val end : LocalDate?,
) {
    fun hasStartOrEnd() : Boolean {
        return start != null || end != null
    }
}