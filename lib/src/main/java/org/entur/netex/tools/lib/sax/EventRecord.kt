package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.output.Event

data class EventRecord(
    val event: Event,
    val element: Element
)