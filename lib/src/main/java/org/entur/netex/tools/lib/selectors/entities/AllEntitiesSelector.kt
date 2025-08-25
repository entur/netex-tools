package org.entur.netex.tools.lib.selectors.entities

import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.selections.EntitySelection

class AllEntitiesSelector: EntitySelector() {
    override fun selectEntities(model: EntityModel): EntitySelection =
        EntitySelection(model.getEntitesByTypeAndId())
}