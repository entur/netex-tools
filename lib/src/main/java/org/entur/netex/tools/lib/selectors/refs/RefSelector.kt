package org.entur.netex.tools.lib.selectors.refs

import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.selections.RefSelection

interface RefSelector {
    fun selectRefs(model: EntityModel): RefSelection
}