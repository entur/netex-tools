package org.entur.netex.tools.lib.selections

import org.entur.netex.tools.lib.model.Element

class RefSelection(val selection: Set<String>): Selection() {
    override fun includes(element: Element): Boolean {
        if (!element.isRef()) {
            return false
        }
        val ref = element.attributes?.getValue("ref") ?: return false
        return selection.contains(ref)
    }
}