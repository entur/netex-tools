package org.entur.netex.tools.lib.plugin.activedates.model

import java.time.LocalDate

data class Period(
    val fromDate: LocalDate?,
    val toDate: LocalDate?,
)