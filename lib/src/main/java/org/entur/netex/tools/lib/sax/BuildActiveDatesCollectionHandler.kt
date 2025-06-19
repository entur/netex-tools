package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.utils.Log
import org.xml.sax.Attributes
import java.time.LocalDate

class BuildActiveDatesCollectionHandler(
    val addOperatingDayToCalendarDateEntry : (String, LocalDate) -> Unit,
    val addDayTypeRefToDateEntry : (String, LocalDate) -> Unit,
    val addDayTypeRefToOperatingDayRefEntry : (String, String) -> Unit,
    val addDayTypeRefToOperatingPeriodRefEntry : (String, String) -> Unit,
) : NetexToolsSaxHandler() {
    val stringBuilder = StringBuilder()
    var isParsingOperatingDays = false
    var isParsingDayTypeAssignments = false

    var currentOperatingDayId: String = ""

    var currentDayTypeRef: String = ""
    var currentDateInDayTypeAssignment: LocalDate? = null
    var currentOperatingDayRefInDayTypeAssignment: String = ""
    var currentOperatingPeriodRefInDayTypeAssignment: String = ""

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        // Needs to be reset to avoid concatenating of values from different elements
        stringBuilder.setLength(0)

        if (qName == "operatingDays") {
            // Do we need to do this? Depends on whether OperatingDay elements may exist outside of <operatingDays>
            // and whether they need to be handled differently
            isParsingOperatingDays = true
        } else if (qName == "OperatingDay" && isParsingOperatingDays) {
            val operatingDayId = attributes?.getValue("id")
            if (operatingDayId == null) {
                Log.warn("Detected OperatingDay without id")
            } else {
                currentOperatingDayId = operatingDayId
            }
        }

        // We don't need to keep the id of the DayTypeAssignment itself because nothing refers to it, but we need
        // to get the mandatory DayTypeRef.ref and map it to date/operatingDayRef/operatingPeriodRef
        if (qName == "dayTypeAssignments") {
            isParsingDayTypeAssignments = true
        } else if (qName == "DayTypeRef" && isParsingDayTypeAssignments) {
            val dayTypeRef = attributes?.getValue("ref")
            if (dayTypeRef == null) {
                Log.warn("Detected Day type assignment without id")
            } else {
                currentDayTypeRef = dayTypeRef
            }
        } else if (qName == "OperatingPeriodRef" && isParsingDayTypeAssignments) {
            val operatingPeriodRef = attributes?.getValue("ref")
            if (operatingPeriodRef == null) {
                Log.warn("Detected OperatingPeriodRef without ref")
            } else {
                currentOperatingPeriodRefInDayTypeAssignment = operatingPeriodRef
            }
        } else if (qName == "OperatingDayRef" && isParsingDayTypeAssignments) {
            val operatingDayRef = attributes?.getValue("ref")
            if (operatingDayRef == null) {
                Log.warn("Detected OperatingPeriod without ref")
            } else {
                currentOperatingDayRefInDayTypeAssignment = operatingDayRef
            }
        }
    }

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        stringBuilder.append(ch, start, length)
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        // Parsing of <operatingDays>
        if (isParsingOperatingDays) {
            if (qName == "CalendarDate" && currentOperatingDayId != "") {
                val dateString = stringBuilder.toString()
                val calendarDate = LocalDate.parse(dateString)
                addOperatingDayToCalendarDateEntry(currentOperatingDayId, calendarDate)
            }

            if (qName == "operatingDays") {
                currentOperatingDayId = ""
                isParsingOperatingDays = false
            }

            return
        }

        // Parsing of <dayTypeAssignments>
        // A <DayTypeAssignment> encapsulates Date, OperatingDayRef and/or OperatingPeriodRef
        // Just one of them, or possible to use e.g. both Date and OperatingDayRef in a single DayTypeAssignment?
        if (isParsingDayTypeAssignments) {
            val valueOfXmlElement = stringBuilder.toString()
            if (qName == "Date") {
                currentDateInDayTypeAssignment = LocalDate.parse(valueOfXmlElement)
            }

            if (qName == "DayTypeAssignment" && currentDayTypeRef != "") {
                // Adding entries need to be done last for DayTypeAssignment, because DayTypeRef and its "values"
                // are siblings in the DayTypeAssignment XML
                if (currentDateInDayTypeAssignment != null) {
                    addDayTypeRefToDateEntry(currentDayTypeRef, currentDateInDayTypeAssignment!!)
                }
                if (currentOperatingDayRefInDayTypeAssignment != "") {
                    addDayTypeRefToOperatingDayRefEntry(currentDayTypeRef, currentOperatingDayRefInDayTypeAssignment)
                }
                if (currentOperatingPeriodRefInDayTypeAssignment != "") {
                    addDayTypeRefToOperatingPeriodRefEntry(currentDayTypeRef, currentOperatingPeriodRefInDayTypeAssignment)
                }

                currentDayTypeRef = ""
                currentDateInDayTypeAssignment = null
                currentOperatingDayRefInDayTypeAssignment = ""
                currentOperatingPeriodRefInDayTypeAssignment = ""
            }

            if (qName == "dayTypeAssignments") {
                isParsingDayTypeAssignments = false
            }

            return
        }
    }
}
