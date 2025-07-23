package org.entur.netex.tools.lib.selection

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntitySelection

abstract class EntitySelector {
    open fun selector(entities: Collection<Entity>): EntitySelection {
        throw NotImplementedError("Selector method not implemented in ${this::class.simpleName}")
    }

    open fun selector(entitySelection: EntitySelection): EntitySelection {
        throw NotImplementedError("Selector method not implemented in ${this::class.simpleName}")
    }
}