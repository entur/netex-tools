package org.entur.netex.tools.lib.plugin.activedates

import java.time.LocalDate

data class ActiveDatesParsingContext (
    var currentServiceJourneyId: String? = null,

    var currentOperatingDayRef: String? = null,
    var currentServiceJourneyRef: String? = null,

    var currentDayTypeAssignmentDayTypeRef: String? = null,

    var currentDayTypeAssignmentDate: LocalDate? = null,
    var currentDayTypeAssignmentOperatingDay: String? = null,
    var currentDayTypeAssignmentOperatingPeriod: String? = null,
    var currentDayTypeId: String? = null,
) {
}