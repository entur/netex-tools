package org.entur.netex.tools.lib.sax

import java.time.LocalDate

data class ActiveDatesModel(
    val operatingDayToCalendarDateMap: MutableMap<String, LocalDate> = mutableMapOf(),

    // DayTypeAssignment
    val dayTypeRefToDateMap: MutableMap<String, MutableList<LocalDate>> = mutableMapOf(),
    val dayTypeRefToOperatingDayRefMap: MutableMap<String, MutableList<String>> = mutableMapOf(),
    val dayTypeRefToOperatingPeriodRefMap: MutableMap<String, MutableList<String>> = mutableMapOf(),

    val dayTypeToDaysOfWeek: Map<String, String> = mutableMapOf(),

    var currentDayTypeAssignmentDayTypeRef: String? = null,
    var currentDayTypeAssignmentDate: LocalDate? = null,
    var currentDayTypeAssignmentOperatingDay: String? = null,
    var currentDayTypeAssignmentOperatingPeriod: String? = null,
    )