package org.entur.netex.tools.lib.sax.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.sax.ActiveDatesModel
import org.entur.netex.tools.lib.sax.NetexDataCollector

class DaysOfWeekHandler(val activeDatesModel: ActiveDatesModel): NetexDataCollector() {
    val stringBuilder = StringBuilder()

    override fun characters(ch: CharArray?, start: Int, length: Int, currentEntity: Entity) {
        stringBuilder.append(ch, start, length)
    }

    override fun endElement(uri: String?, localName: String?, qName: String?, currentEntity: Entity) {
        activeDatesModel.dayTypeToDaysOfWeek.put(currentEntity.id, stringBuilder.toString())
    }
}