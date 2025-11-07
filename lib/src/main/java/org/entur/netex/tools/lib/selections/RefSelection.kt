package org.entur.netex.tools.lib.selections

import org.entur.netex.tools.lib.model.Ref

class RefSelection(val selection: Set<Ref>) {
    val allRefs = selection.map { it.ref }.toSet()

    fun includes(ref: String) = allRefs.contains(ref)

    fun includes(ref: Ref): Boolean {
        return selection.contains(ref)
    }

    fun intersectWith(otherRefSelection: RefSelection): RefSelection {
        return RefSelection(selection.intersect(otherRefSelection.selection))
    }
}