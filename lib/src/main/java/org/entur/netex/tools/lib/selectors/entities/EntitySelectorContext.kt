package org.entur.netex.tools.lib.selectors.entities

import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.selections.EntitySelection

data class EntitySelectorContext(
    val entityModel: EntityModel,
    val currentEntitySelection: EntitySelection? = null
)