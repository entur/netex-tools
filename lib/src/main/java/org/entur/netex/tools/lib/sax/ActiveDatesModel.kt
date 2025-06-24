package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.sax.model.Period
import java.time.LocalDate
import java.time.LocalTime

data class ActiveDatesModel(
    val operatingDayToCalendarDateMap: MutableMap<String, LocalDate> = mutableMapOf(),

    // operatingPeriods
    val operatingPeriodIdToPeriodMap: MutableMap<String, Period> = mutableMapOf(),
    val operatingPeriodIdToFromDateRefMap: MutableMap<String, String> = mutableMapOf(),
    val operatingPeriodIdToToDateRefMap: MutableMap<String, String> = mutableMapOf(),

    // DayTypeAssignment
    val dayTypeRefToDateMap: MutableMap<String, MutableList<LocalDate>> = mutableMapOf(),
    val dayTypeRefToOperatingDayRefMap: MutableMap<String, MutableList<String>> = mutableMapOf(),
    val dayTypeRefToOperatingPeriodRefMap: MutableMap<String, MutableList<String>> = mutableMapOf(),

    val dayTypeToDaysOfWeek: MutableMap<String, String> = mutableMapOf(),

    val serviceJourneyToDayTypeRefMap: MutableMap<String, MutableList<String>> = mutableMapOf(),

    // ServiceJourneyId->OperatingDay[] via DatedServiceJourney
    val serviceJourneyToOperatingDayRefMap: MutableMap<String, MutableList<String>> = mutableMapOf(),
    var currentOperatingDayRef: String? = null,
    var currentServiceJourneyRef: String? = null,

    val serviceJourneyToFinalArrivalTimeMap: MutableMap<String, LocalTime> = mutableMapOf(),
    val serviceJourneyToFinalArrivalDayOffsetMap: MutableMap<String, Int> = mutableMapOf(),
    var currentServiceJourneyId: String? = null,

    var currentDayTypeAssignmentDayTypeRef: String? = null,
    var currentDayTypeAssignmentDate: LocalDate? = null,
    var currentDayTypeAssignmentOperatingDay: String? = null,
    var currentDayTypeAssignmentOperatingPeriod: String? = null,

    var currentDayTypeId: String? = null,
)