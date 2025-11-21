package org.entur.netex.tools.lib.selectors.entities

import org.entur.netex.tools.lib.selections.EntitySelection

/**
 * An implementation of EntitySelector makes a selection of entities, and returns
 * an EntitySelection that contain the selected entities.
 *
 * @param context Context object containing the entity model, and optionally, a current entity selection state (e.g. the results of an entity selector run at an earlier stage)
 * */
interface EntitySelector {
    fun selectEntities(context: EntitySelectorContext): EntitySelection
}