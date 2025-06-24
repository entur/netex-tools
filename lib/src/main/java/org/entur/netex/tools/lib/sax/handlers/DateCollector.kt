package org.entur.netex.tools.lib.sax.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.sax.ActiveDatesModel
import org.entur.netex.tools.lib.sax.NetexDataCollector
import java.time.LocalDate

class DateCollector(val activeDatesModel: ActiveDatesModel) : NetexDataCollector() {

    private val stringBuilder = StringBuilder()

    override fun characters(ch: CharArray?, start: Int, length: Int, currentEntity: Entity) {
        stringBuilder.append(ch, start, length)
    }

    override fun endElement(uri: String?, localName: String?, qName: String?, parentEntity: Entity) {
        val calendarDate = LocalDate.parse(stringBuilder.trim().toString())

        if (parentEntity.type == "DayTypeAssignment") {
            activeDatesModel.currentDayTypeAssignmentDate = calendarDate
        }
    }
}