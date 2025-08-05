package org.entur.netex.tools.lib.selections

import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.Ref

class RefSelection(val selection: Set<Ref>): Selection() {
    override fun includes(element: Element): Boolean {
        if (!element.isRef()) {
            return false
        }
        val ref = element.attributes?.getValue("ref") ?: return false
        return selection.any { it.ref == ref }
    }

    fun isSelected(ref: String): Boolean {
        return selection.any { it.ref == ref }
    }
}