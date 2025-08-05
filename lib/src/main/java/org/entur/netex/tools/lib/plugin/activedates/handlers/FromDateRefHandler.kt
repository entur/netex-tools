package org.entur.netex.tools.lib.plugin.activedates.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesParsingContext
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.plugin.activedates.NetexDataCollector
import org.xml.sax.Attributes

class FromDateRefHandler(
    val activeDatesRepository: ActiveDatesRepository,
): NetexDataCollector(
) {
    override fun startElement(
        context: ActiveDatesParsingContext,
        attributes: Attributes?,
        currentEntity: Entity
    ) {
        if (currentEntity.type == NetexTypes.OPERATING_PERIOD) {
            val operatingPeriodId = currentEntity.id
            val fromDateRef = attributes?.getValue("ref")
            val opPeriodData = activeDatesRepository.getOperatingPeriodData(operatingPeriodId)
            opPeriodData.fromDateId = fromDateRef.toString()
        }
    }
}