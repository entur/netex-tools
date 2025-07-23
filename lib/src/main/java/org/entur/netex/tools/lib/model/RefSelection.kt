package org.entur.netex.tools.lib.model

class RefSelection(val selection: Set<Ref>) {
    fun includes(element: Element): Boolean {
        if (!element.isRef()) {
            return false
        }
        val ref = element.attributes?.getValue("ref") ?: return false
        return selection.any { it.ref == ref }
    }
}