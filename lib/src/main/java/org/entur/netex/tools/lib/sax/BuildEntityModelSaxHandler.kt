package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.Entity.Companion.EMPTY
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.NetexTypes
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
        NetexTypes.CALENDAR_DATE to CalendarDateHandler(activeDatesModel),
        NetexTypes.DAY_TYPE_ASSIGNMENT to DayTypeAssignmentHandler(activeDatesModel),
        NetexTypes.OPERATING_DAY_REF to OperatingDayRefHandler(activeDatesModel),
        NetexTypes.OPERATING_PERIOD_REF to OperatingPeriodRefHandler(activeDatesModel),
        NetexTypes.DATE to DateCollector(activeDatesModel),
        NetexTypes.DAY_TYPE_REF to DayTypeRefHandler(activeDatesModel),
        NetexTypes.DAYS_OF_WEEK to DaysOfWeekHandler(activeDatesModel),
        NetexTypes.FROM_DATE to FromDateHandler(activeDatesModel),
        NetexTypes.TO_DATE to ToDateHandler(activeDatesModel),
        NetexTypes.FROM_DATE_REF to FromDateRefHandler(activeDatesModel),
        NetexTypes.TO_DATE_REF to ToDateRefHandler(activeDatesModel),
        NetexTypes.DATED_SERVICE_JOURNEY to DatedServiceJourneyHandler(activeDatesModel),
        NetexTypes.SERVICE_JOURNEY_REF to ServiceJourneyRefHandler(activeDatesModel),
        NetexTypes.ARRIVAL_TIME to ArrivalTimeHandler(activeDatesModel),
        NetexTypes.ARRIVAL_DAY_OFFSET to ArrivalDayOffsetHandler(activeDatesModel),
        NetexTypes.SERVICE_JOURNEY to ServiceJourneyHandler(activeDatesModel),
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
            currentHandler.startElement(attributes, currentEntity!!)
        }
    }

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        val currentHandler = handlerMap.get(currentElement?.name)

        if (currentHandler != null && currentEntity != null) {
            currentHandler.characters(ch, start, length)
        }
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        val currentHandler = handlerMap.get(currentElement?.name)
        if (currentHandler != null && currentEntity != null) {
            currentHandler.endElement(currentEntity!!)
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
