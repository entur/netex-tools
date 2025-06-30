package org.entur.netex.tools.lib.sax.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.sax.ActiveDatesModel
import org.entur.netex.tools.lib.sax.NetexDataCollector
import org.entur.netex.tools.lib.sax.model.Period
import java.time.LocalDateTime

class ToDateHandler(
    val activeDatesModel: ActiveDatesModel
) : NetexDataCollector() {
    val stringBuilder = StringBuilder()

    override fun characters(ch: CharArray?, start: Int, length: Int, currentEntity: Entity) {
        stringBuilder.append(ch, start, length)
    }

    override fun endElement(uri: String?, localName: String?, qName: String?, currentEntity: Entity) {
        if (currentEntity.type == "OperatingPeriod") {
            val operatingPeriodId = currentEntity.id
            val existingFromDate = activeDatesModel.operatingPeriodIdToPeriodMap[operatingPeriodId]?.fromDate
            activeDatesModel.operatingPeriodIdToPeriodMap[operatingPeriodId] = Period(
                fromDate = existingFromDate,
                toDate = LocalDateTime.parse(stringBuilder.toString()).toLocalDate(),
            )
        }
        stringBuilder.setLength(0)
    }
}