package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.extensions.middle
import org.entur.netex.tools.lib.output.DeferredWriter
import org.entur.netex.tools.lib.output.NetexFileWriter
import org.entur.netex.tools.lib.output.StartElement

class RequiredChildrenWriter(
    val elementsRequiredChildren: Map<String, List<String>>,
    fileWriter: NetexFileWriter,
): DeferredWriter(
    fileWriter,
) {
    override fun shouldDeferWritingEvent(currentPath: String): Boolean {
        val keysToDefer = elementsRequiredChildren.keys
        return keysToDefer.any(currentPath::startsWith)
    }

    override fun shouldWriteDeferredEvents(): Boolean {
        val requiredChildren = elementsRequiredChildren["/PublicationDelivery/dataObjects/CompositeFrame/frames/ServiceFrame/noticeAssignments/NoticeAssignment"] ?: emptyList()
        val deferredChildEventElements = deferredEvents.middle()
            .filter { it is StartElement }
            .map { (it as StartElement).qName }
        return deferredChildEventElements.containsAll(requiredChildren)
    }
}
