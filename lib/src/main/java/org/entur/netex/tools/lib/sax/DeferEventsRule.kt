package org.entur.netex.tools.lib.sax

interface DeferEventsRule {
    fun shouldDefer(eventRecord: EventRecord, buffer: EventBuffer): Boolean
    fun shouldHandleDeferredEvents(buffer: EventBuffer): Boolean
}