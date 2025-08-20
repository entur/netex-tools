package org.entur.netex.tools.lib.selectors.refs

import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.selections.RefSelection

abstract class RefSelector {
    abstract fun selectRefs(model: EntityModel): RefSelection
}