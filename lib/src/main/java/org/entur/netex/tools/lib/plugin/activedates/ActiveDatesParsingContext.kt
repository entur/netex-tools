package org.entur.netex.tools.lib.plugin.activedates

import org.entur.netex.tools.lib.model.EntityId
import java.time.LocalDate

data class ActiveDatesParsingContext (
    var currentServiceJourneyId: EntityId.Simple? = null,
    var currentDeadRunId: EntityId.Simple? = null,

    var currentOperatingDayRef: EntityId.Simple? = null,
    var currentServiceJourneyRef: EntityId.Simple? = null,

    var currentDayTypeAssignmentDayTypeRef: EntityId.Simple? = null,

    var currentDayTypeAssignmentDate: LocalDate? = null,
    var currentDayTypeAssignmentOperatingDay: EntityId.Simple? = null,
    var currentDayTypeAssignmentOperatingPeriod: EntityId.Simple? = null,
    var currentDayTypeId: EntityId.Simple? = null,
)