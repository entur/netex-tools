package org.entur.netex.tools.lib.plugin.activedates.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesParsingContext
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.plugin.activedates.NetexDataCollector
import java.time.LocalDate

class CalendarDateHandler(val activeDatesRepository: ActiveDatesRepository): NetexDataCollector() {
    private val stringBuilder = StringBuilder()

    override fun characters(context: ActiveDatesParsingContext, ch: CharArray?, start: Int, length: Int) {
        stringBuilder.append(ch, start, length)
    }

    override fun endElement(context: ActiveDatesParsingContext, currentEntity: Entity) {
        val calendarDate = LocalDate.parse(stringBuilder.trim().toString())
        activeDatesRepository.operatingDayToCalendarDateMap[currentEntity.id] = calendarDate
        stringBuilder.setLength(0)
    }
}