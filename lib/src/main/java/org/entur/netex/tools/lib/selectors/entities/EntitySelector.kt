package org.entur.netex.tools.lib.selectors.entities

import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.selections.EntitySelection

interface EntitySelector {
    fun selectEntities(model: EntityModel, currentEntitySelection: EntitySelection? = null): EntitySelection
}