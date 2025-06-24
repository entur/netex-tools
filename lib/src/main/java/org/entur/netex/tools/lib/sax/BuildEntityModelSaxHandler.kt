package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.Entity.Companion.EMPTY
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.sax.handlers.*
import org.xml.sax.Attributes

class BuildEntityModelSaxHandler(
    val entities : EntityModel,
    val skipHandler : SkipElementHandler,
    val activeDatesModel: ActiveDatesModel

) : NetexToolsSaxHandler() {
    var currentEntity : Entity? = null
    var currentElement : Element? = null

    val handlerMap: Map<String, NetexDataCollector> = mapOf(
        "CalendarDate" to CalendarDateHandler(activeDatesModel),
        "DayTypeAssignment" to DayTypeAssignmentHandler(activeDatesModel),
        "OperatingDayRef" to OperatingDayRefHandler(activeDatesModel),
        "OperatingPeriodRef" to OperatingPeriodRefHandler(activeDatesModel),
        "Date" to DateCollector(activeDatesModel),
        "DayTypeRef" to DayTypeRefHandler(activeDatesModel),
        "DaysOfWeek" to DaysOfWeekHandler(activeDatesModel),
        "FromDate" to FromDateHandler(activeDatesModel),
        "ToDate" to ToDateHandler(activeDatesModel),
        "FromDateRef" to FromDateRefHandler(activeDatesModel),
        "ToDateRef" to ToDateRefHandler(activeDatesModel),
        "DatedServiceJourney" to DatedServiceJourneyHandler(activeDatesModel),
        "ServiceJourneyRef" to ServiceJourneyRefHandler(activeDatesModel),
    )

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        val type = qName!!
        currentElement = Element(type, currentElement)

        if(skipHandler.startSkip(currentElement!!)) {
            return
        }

        // Handle entity
        val id = attributes?.getValue("id")
        val publication = attributes?.getValue("publication") ?: "public"

        if (id != null) {
            val entity = Entity(id, type, publication, currentEntity)
            currentEntity = entity
            entities.addEntity(entity)
        } else {
            val ref = attributes?.getValue("ref")
            if (ref != null) {
                entities.addRef(nn(type), currentEntity!!, ref)
            }
        }

        val currentHandler = handlerMap.get(currentElement?.name)
        if (currentHandler != null && currentEntity != null) {
            currentHandler.startElement(uri, localName, qName, attributes, currentEntity!!)
        }
    }

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        val currentHandler = handlerMap.get(currentElement?.name)

        if (currentHandler != null && currentEntity != null) {
            currentHandler.characters(ch, start, length, currentEntity!!)
        }
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        val currentHandler = handlerMap.get(currentElement?.name)
        if (currentHandler != null && currentEntity != null) {
            currentHandler.endElement(uri, localName, qName, currentEntity!!)
        }

        val c = currentElement
        currentElement = currentElement?.parent
        if(skipHandler.endSkip(c)){
            return
        }

        if (currentEntity?.type == qName) {
            currentEntity = currentEntity?.parent
        }
    }

    private fun nn(value : String?) = value ?: EMPTY
}
