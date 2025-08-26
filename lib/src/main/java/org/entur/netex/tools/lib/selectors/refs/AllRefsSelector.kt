package org.entur.netex.tools.lib.selectors.refs

import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.selections.RefSelection

class AllRefsSelector: RefSelector() {
    override fun selectRefs(model: EntityModel): RefSelection =
        RefSelection(model.listAllRefs().toSet())
}