package org.entur.netex.tools.lib.sax.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.sax.ActiveDatesModel
import org.entur.netex.tools.lib.sax.NetexDataCollector
import org.xml.sax.Attributes

class FromDateRefHandler(
    val activeDatesModel: ActiveDatesModel,
): NetexDataCollector(
) {
    override fun startElement(
        attributes: Attributes?,
        currentEntity: Entity
    ) {
        if (currentEntity.type == NetexTypes.OPERATING_PERIOD) {
            val operatingPeriodId = currentEntity.id
            val fromDateRef = attributes?.getValue("ref")
            activeDatesModel.operatingPeriodIdToFromDateRefMap[operatingPeriodId] = fromDateRef.toString()
        }
    }
}