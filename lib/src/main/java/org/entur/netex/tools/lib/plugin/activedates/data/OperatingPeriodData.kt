package org.entur.netex.tools.lib.plugin.activedates.data

import org.entur.netex.tools.lib.plugin.activedates.model.Period

data class OperatingPeriodData(
    var period: Period? = null,
    var fromDateId: String? = null,
    var toDateId: String? = null
)
