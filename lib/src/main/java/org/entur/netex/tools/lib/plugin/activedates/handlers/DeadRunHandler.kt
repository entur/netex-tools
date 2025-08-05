package org.entur.netex.tools.lib.plugin.activedates.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesParsingContext
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.plugin.activedates.NetexDataCollector
import org.xml.sax.Attributes

class DeadRunHandler(val activeDatesRepository: ActiveDatesRepository): NetexDataCollector() {
    override fun startElement(
        context: ActiveDatesParsingContext,
        attributes: Attributes?,
        currentEntity: Entity
    ) {
        context.currentDeadRunId = currentEntity.id
    }

    override fun endElement(context: ActiveDatesParsingContext, currentEntity: Entity) {
        context.currentDeadRunId = null
    }
}