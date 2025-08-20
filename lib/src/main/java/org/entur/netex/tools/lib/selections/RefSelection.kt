package org.entur.netex.tools.lib.selections

import org.entur.netex.tools.lib.model.Ref

class RefSelection(val selection: Set<Ref>) {
    fun includes(ref: Ref): Boolean {
        return selection.contains(ref)
    }

    fun intersectWith(otherRefSelection: RefSelection): RefSelection {
        return RefSelection(selection.intersect(otherRefSelection.selection))
    }
}