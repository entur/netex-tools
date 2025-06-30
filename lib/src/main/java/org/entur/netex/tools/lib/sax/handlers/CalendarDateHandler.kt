package org.entur.netex.tools.lib.sax.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.sax.ActiveDatesModel
import org.entur.netex.tools.lib.sax.NetexDataCollector
import java.time.LocalDate

class CalendarDateHandler(val activeDatesModel: ActiveDatesModel): NetexDataCollector() {
    val stringBuilder = StringBuilder()

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        stringBuilder.append(ch, start, length)
    }

    override fun endElement(currentEntity: Entity) {
        val calendarDate = LocalDate.parse(stringBuilder.trim().toString())
        activeDatesModel.operatingDayToCalendarDateMap.put(currentEntity.id, calendarDate)
        stringBuilder.setLength(0)
    }
}