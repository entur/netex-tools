package org.entur.netex.tools.lib.sax.handlers

import org.entur.netex.tools.lib.extensions.putOrAddToExistingList
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.sax.ActiveDatesModel
import org.entur.netex.tools.lib.sax.NetexDataCollector
import org.xml.sax.Attributes

class DayTypeAssignmentHandler(
    val activeDatesModel: ActiveDatesModel
) : NetexDataCollector {

    override fun characters(ch: CharArray?, start: Int, length: Int, currentEntity: Entity) {
    }

    override fun endElement(uri: String?, localName: String?, qName: String?, parentEntity: Entity) {
        if (activeDatesModel.currentDayTypeAssignmentDayTypeRef == null) {
            return
        }
        activeDatesModel.currentDayTypeAssignmentOperatingDay?.let {
            activeDatesModel.dayTypeRefToOperatingDayRefMap.putOrAddToExistingList(
                activeDatesModel.currentDayTypeAssignmentDayTypeRef!!, it
            )
        }

        activeDatesModel.currentDayTypeAssignmentOperatingPeriod?.let {
            activeDatesModel.dayTypeRefToOperatingPeriodRefMap.putOrAddToExistingList(
                activeDatesModel.currentDayTypeAssignmentDayTypeRef!!, it
            )
        }

        activeDatesModel.currentDayTypeAssignmentDate?.let {
            activeDatesModel.dayTypeRefToDateMap.putOrAddToExistingList(
                activeDatesModel.currentDayTypeAssignmentDayTypeRef!!, it
            )
        }
        activeDatesModel.currentDayTypeAssignmentDayTypeRef = null
        activeDatesModel.currentDayTypeAssignmentOperatingDay = null
        activeDatesModel.currentDayTypeAssignmentOperatingPeriod = null
        activeDatesModel.currentDayTypeAssignmentDate = null
    }

    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?,
        currentEntity: Entity
    ) {
    }
}