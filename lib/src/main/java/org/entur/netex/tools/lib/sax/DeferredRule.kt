package org.entur.netex.tools.lib.sax

interface DeferredRule {
    fun shouldDefer(eventRecord: EventRecord, buffer: DeferredEventsBuffer): Boolean
    fun shouldHandleDeferredEvents(buffer: DeferredEventsBuffer): Boolean
}