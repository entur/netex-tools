package org.entur.netex.tools.cli.config

import kotlinx.serialization.Serializable

@Serializable
data class TimePeriod(
    val start : String,
    val end : String
)