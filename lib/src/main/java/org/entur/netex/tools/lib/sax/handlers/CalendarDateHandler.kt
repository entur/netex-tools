package org.entur.netex.tools.lib.sax.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.sax.ActiveDatesModel
import org.entur.netex.tools.lib.sax.NetexDataCollector
import org.xml.sax.Attributes
import java.time.LocalDate

class CalendarDateHandler(val activeDatesModel: ActiveDatesModel): NetexDataCollector {
    val stringBuilder = StringBuilder()

    override fun characters(ch: CharArray?, start: Int, length: Int, currentEntity: Entity) {
        stringBuilder.append(ch, start, length)
    }

    override fun endElement(uri: String?, localName: String?, qName: String?, parentEntity: Entity) {
        val calendarDate = LocalDate.parse(stringBuilder.trim().toString())
        activeDatesModel.operatingDayToCalendarDateMap.put(parentEntity.id, calendarDate)
        stringBuilder.setLength(0)
    }

    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?
    ) {
        TODO("Not yet implemented")
    }
}