package org.entur.netex.tools.lib.sax.model

import java.time.LocalDate

data class Period(
    val fromDate: LocalDate?,
    val toDate: LocalDate?,
)