package org.entur.netex.tools.lib.selectors.entities

import org.entur.netex.tools.lib.selections.EntitySelection

class AllEntitiesSelector: EntitySelector {
    override fun selectEntities(context: EntitySelectorContext): EntitySelection {
        val model = context.entityModel
        return EntitySelection(model.getEntitesByTypeAndId(), model)
    }
}