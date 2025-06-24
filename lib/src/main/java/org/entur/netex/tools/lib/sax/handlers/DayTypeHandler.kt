package org.entur.netex.tools.lib.sax.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.sax.ActiveDatesModel
import org.entur.netex.tools.lib.sax.NetexDataCollector
import org.xml.sax.Attributes

class DayTypeHandler(
    val activeDatesModel: ActiveDatesModel
): NetexDataCollector() {
    override fun endElement(
        uri: String?,
        localName: String?,
        qName: String?,
        currentEntity: Entity
    ) {
        activeDatesModel.currentDayTypeId = null
    }

    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?,
        currentEntity: Entity
    ) {
        activeDatesModel.currentDayTypeId = attributes?.getValue("id")
    }
}