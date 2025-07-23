package org.entur.netex.tools.lib.model

import org.entur.netex.tools.lib.selections.Selection

class RefSelection(val selection: Set<Ref>): Selection() {
    override fun includes(element: Element): Boolean {
        if (!element.isRef()) {
            return false
        }
        val ref = element.attributes?.getValue("ref") ?: return false
        return selection.any { it.ref == ref }
    }
}