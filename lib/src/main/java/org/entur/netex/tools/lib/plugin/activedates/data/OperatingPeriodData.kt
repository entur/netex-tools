package org.entur.netex.tools.lib.plugin.activedates.data

import org.entur.netex.tools.lib.model.EntityId
import org.entur.netex.tools.lib.plugin.activedates.model.Period

data class OperatingPeriodData(
    var period: Period? = null,
    var fromDateId: EntityId.Simple? = null,
    var toDateId: EntityId.Simple? = null
)
