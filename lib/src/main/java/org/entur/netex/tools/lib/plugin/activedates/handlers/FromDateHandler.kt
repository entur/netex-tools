package org.entur.netex.tools.lib.plugin.activedates.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.plugin.activedates.NetexDataCollector
import org.entur.netex.tools.lib.plugin.activedates.model.Period
import java.time.LocalDateTime

class FromDateHandler(
    val activeDatesRepository: ActiveDatesRepository
) : NetexDataCollector() {
    private val stringBuilder = StringBuilder()

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        stringBuilder.append(ch, start, length)
    }

    override fun endElement(currentEntity: Entity) {
        if (currentEntity.type == NetexTypes.OPERATING_PERIOD) {
            val operatingPeriodId = currentEntity.id
            val existingToDate = activeDatesRepository.operatingPeriodIdToPeriodMap[operatingPeriodId]?.toDate
            activeDatesRepository.operatingPeriodIdToPeriodMap[operatingPeriodId] = Period(
                fromDate = LocalDateTime.parse(stringBuilder.toString()).toLocalDate(),
                toDate = existingToDate,
            )
        }
        stringBuilder.setLength(0)
    }
}