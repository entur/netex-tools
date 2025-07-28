package org.entur.netex.tools.lib.selectors

import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.selections.EntitySelection

abstract class EntitySelector {
    abstract fun selectEntities(model: EntityModel): EntitySelection
}