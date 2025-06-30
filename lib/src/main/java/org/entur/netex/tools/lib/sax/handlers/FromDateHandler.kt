package org.entur.netex.tools.lib.sax.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.sax.ActiveDatesModel
import org.entur.netex.tools.lib.sax.NetexDataCollector
import org.entur.netex.tools.lib.sax.model.Period
import java.time.LocalDateTime

class FromDateHandler(
    val activeDatesModel: ActiveDatesModel
) : NetexDataCollector() {
    val stringBuilder = StringBuilder()

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        stringBuilder.append(ch, start, length)
    }

    override fun endElement(currentEntity: Entity) {
        if (currentEntity.type == "OperatingPeriod") {
            val operatingPeriodId = currentEntity.id
            val existingToDate = activeDatesModel.operatingPeriodIdToPeriodMap[operatingPeriodId]?.toDate
            activeDatesModel.operatingPeriodIdToPeriodMap[operatingPeriodId] = Period(
                fromDate = LocalDateTime.parse(stringBuilder.toString()).toLocalDate(),
                toDate = existingToDate,
            )
        }
        stringBuilder.setLength(0)
    }
}