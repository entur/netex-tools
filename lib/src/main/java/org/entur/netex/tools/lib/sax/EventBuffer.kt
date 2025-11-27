package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.extensions.middle
import org.entur.netex.tools.lib.output.EndElement
import org.entur.netex.tools.lib.output.StartElement

data class EventBuffer(
    val events: MutableList<EventRecord> = mutableListOf(),
    var depth: Int = 0,
) {
    fun first() = events.first()
    fun middle() = events.middle()

    fun hasReachedEndOfBufferedElement(): Boolean {
        return depth == 0 && events.isNotEmpty()
    }

    fun add(eventRecord: EventRecord) {
        events.add(eventRecord)

        if (eventRecord.event is StartElement) {
            depth++
        }

        if (eventRecord.event is EndElement) {
            depth--
        }
    }

    fun flush(eventHandler: (EventRecord) -> Unit) {
        for (event in events) {
            eventHandler(event)
        }

        events.clear()
        depth = 0
    }
}