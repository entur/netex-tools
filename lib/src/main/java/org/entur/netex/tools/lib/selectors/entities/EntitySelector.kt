package org.entur.netex.tools.lib.selectors.entities

import org.entur.netex.tools.lib.selections.EntitySelection

interface EntitySelector {
    fun selectEntities(context: EntitySelectorContext): EntitySelection
}