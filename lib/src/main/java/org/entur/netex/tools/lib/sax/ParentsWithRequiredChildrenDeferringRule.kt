package org.entur.netex.tools.lib.sax

class ParentsWithRequiredChildrenDeferringRule(
    val parentsWithRequiredChildren: Map<String, List<String>>,
): DeferredRule {
    val parents = parentsWithRequiredChildren.keys.toList()

    override fun shouldDefer(eventRecord: EventRecord, buffer: DeferredEventsBuffer): Boolean {
        val element = eventRecord.element
        if (parents.contains(element.name)) return true
        return buffer.events.isNotEmpty()
    }

    override fun shouldHandleDeferredEvents(buffer: DeferredEventsBuffer): Boolean {
        val first = buffer.first().element.name
        val childrenInXml = buffer.middle().map { it.element.name }
        val requiredChildren = parentsWithRequiredChildren[first] ?: emptyList()
        return childrenInXml.containsAll(requiredChildren)
    }
}
