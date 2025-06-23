//package org.entur.netex.tools.lib.sax
//
//import org.entur.netex.tools.lib.model.Entity
//import org.entur.netex.tools.lib.sax.handlers.CalendarDateHandler
//import org.entur.netex.tools.lib.sax.handlers.OperatingDayHandler
//import org.entur.netex.tools.lib.utils.Log
//import org.xml.sax.Attributes
//import java.time.LocalDate
//import java.util.Stack
//
//class BuildActiveDatesCollectionHandler(val activeDatesModel: ActiveDatesModel) : NetexToolsSaxHandler() {
//    val stringBuilder = StringBuilder()
//    var isParsingOperatingDays = false
//    var isParsingDayTypeAssignments = false
//    var isParsingDayTypes = false
//
//    var currentOperatingDayId: String = ""
//
//    var currentDayTypeRef: String = ""
//    var currentDateInDayTypeAssignment: LocalDate? = null
//    var currentOperatingDayRefInDayTypeAssignment: String = ""
//    var currentOperatingPeriodRefInDayTypeAssignment: String = ""
//
//    var currentDayTypeId: String = ""
//
//    val elementStack = Stack<NetexToolsSaxHandler>()
//
//    fun getHandler(qName: String?): NetexToolsSaxHandler? {
//        return when (qName) {
//            "OperatingDay" -> {
//                return OperatingDayHandler(activeDatesModel)
//            }
//            "CalendarDate" -> {
//                return CalendarDateHandler(activeDatesModel)
//            }
//            else -> null
//        }
//    }
//
//    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
//        // Needs to be reset to avoid concatenating of values from different elements
////        stringBuilder.setLength(0)
//
//        val handler = getHandler(qName)
//        handler?.let { handlerForElement ->
//            elementStack.add(handlerForElement)
//            handlerForElement.startElement(uri, localName, qName, attributes)
//        }
//
////        if (qName == "operatingDays") {
////            OperatingDayHandler(activeDatesModel).startElement(uri, localName, qName, attributes)
////            // Do we need to do this? Depends on whether OperatingDay elements may exist outside of <operatingDays>
////            // and whether they need to be handled differently
////            isParsingOperatingDays = true
////        } else if (qName == "OperatingDay" && isParsingOperatingDays) {
////            val operatingDayId = attributes?.getValue("id")
////            if (operatingDayId == null) {
////                Log.warn("Detected OperatingDay without id")
////            } else {
////                currentOperatingDayId = operatingDayId
////            }
////        }
////
////        // We don't need to keep the id of the DayTypeAssignment itself because nothing refers to it, but we need
////        // to get the mandatory DayTypeRef.ref and map it to date/operatingDayRef/operatingPeriodRef
////        if (qName == "dayTypeAssignments") {
////            isParsingDayTypeAssignments = true
////        } else if (qName == "DayTypeRef" && isParsingDayTypeAssignments) {
////            val dayTypeRef = attributes?.getValue("ref")
////            if (dayTypeRef == null) {
////                Log.warn("Detected Day type assignment without id")
////            } else {
////                currentDayTypeRef = dayTypeRef
////            }
////        } else if (qName == "OperatingPeriodRef" && isParsingDayTypeAssignments) {
////            val operatingPeriodRef = attributes?.getValue("ref")
////            if (operatingPeriodRef == null) {
////                Log.warn("Detected OperatingPeriodRef without ref")
////            } else {
////                currentOperatingPeriodRefInDayTypeAssignment = operatingPeriodRef
////            }
////        } else if (qName == "OperatingDayRef" && isParsingDayTypeAssignments) {
////            val operatingDayRef = attributes?.getValue("ref")
////            if (operatingDayRef == null) {
////                Log.warn("Detected OperatingPeriod without ref")
////            } else {
////                currentOperatingDayRefInDayTypeAssignment = operatingDayRef
////            }
////        }
////
////        if (qName == "dayTypes") {
////            isParsingDayTypes = true
////        } else if (qName == "DayType" && isParsingDayTypes) {
////            val dayTypeId = attributes?.getValue("id")
////            if (dayTypeId == null) {
////                Log.warn("Detected DayType without id")
////            } else {
////                currentDayTypeId = dayTypeId
////            }
////        }
//    }
//
//    override fun characters(ch: CharArray?, start: Int, length: Int) {
//        if (elementStack.isEmpty()) {
//            return
//        }
////        stringBuilder.append(ch, start, length)
//        elementStack.peek()?.characters(ch, start, length)
//    }
//
//    override fun endElement(uri: String?, localName: String?, qName: String?) {
//        val handler = getHandler(qName)
//        handler?.let { handlerForElement ->
//            handlerForElement.endElement(uri, localName, qName)
//            elementStack.pop()
//        }
////        val valueOfXmlElement = stringBuilder.toString()
//
////        // Parsing of <operatingDays>
////        if (isParsingOperatingDays) {
////            if (qName == "CalendarDate" && currentOperatingDayId != "") {
////                val calendarDate = LocalDate.parse(valueOfXmlElement)
////                addOperatingDayToCalendarDateEntry(currentOperatingDayId, calendarDate)
////            }
////
////            if (qName == "operatingDays") {
////                currentOperatingDayId = ""
////                isParsingOperatingDays = false
////            }
////
////            return
////        }
////
////        // Parsing of <dayTypeAssignments>
////        // A <DayTypeAssignment> encapsulates Date, OperatingDayRef and/or OperatingPeriodRef
////        // Just one of them, or possible to use e.g. both Date and OperatingDayRef in a single DayTypeAssignment?
////        if (isParsingDayTypeAssignments) {
////            if (qName == "Date") {
////                currentDateInDayTypeAssignment = LocalDate.parse(valueOfXmlElement)
////            }
////
////            if (qName == "DayTypeAssignment" && currentDayTypeRef != "") {
////                // Adding entries need to be done last for DayTypeAssignment, because DayTypeRef and its "values"
////                // are siblings in the DayTypeAssignment XML
////                if (currentDateInDayTypeAssignment != null) {
////                    addDayTypeRefToDateEntry(currentDayTypeRef, currentDateInDayTypeAssignment!!)
////                }
////                if (currentOperatingDayRefInDayTypeAssignment != "") {
////                    addDayTypeRefToOperatingDayRefEntry(currentDayTypeRef, currentOperatingDayRefInDayTypeAssignment)
////                }
////                if (currentOperatingPeriodRefInDayTypeAssignment != "") {
////                    addDayTypeRefToOperatingPeriodRefEntry(currentDayTypeRef, currentOperatingPeriodRefInDayTypeAssignment)
////                }
////
////                currentDayTypeRef = ""
////                currentDateInDayTypeAssignment = null
////                currentOperatingDayRefInDayTypeAssignment = ""
////                currentOperatingPeriodRefInDayTypeAssignment = ""
////            }
////
////            if (qName == "dayTypeAssignments") {
////                isParsingDayTypeAssignments = false
////            }
////
////            return
////        }
////
////        if (isParsingDayTypes) {
////            // Do we need to support WeeksOfMonth etc?
////            if (qName == "DaysOfWeek") {
////                addDayTypeToDaysOfWeekEntry(currentDayTypeId, valueOfXmlElement)
////            }
////
////            if (qName == "DayType") {
////                currentDayTypeId = ""
////            }
////
////            if (qName == "dayTypes") {
////                isParsingDayTypes = false
////            }
////        }
//    }
//}
