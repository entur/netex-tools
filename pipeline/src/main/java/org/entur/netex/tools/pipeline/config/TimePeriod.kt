package org.entur.netex.tools.pipeline.config

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class TimePeriod(
    @Serializable(with = LocalDateSerializer::class)
    val start : LocalDate,
    @Serializable(with = LocalDateSerializer::class)
    val end : LocalDate
)