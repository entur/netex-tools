package org.entur.netex.tools.lib.selection

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.SimpleEntitySelection

abstract class EntitySelector {
    open fun selector(entities: Collection<Entity>): SimpleEntitySelection {
        throw NotImplementedError("Selector method not implemented in ${this::class.simpleName}")
    }

    open fun selector(entitySelection: SimpleEntitySelection): SimpleEntitySelection {
        throw NotImplementedError("Selector method not implemented in ${this::class.simpleName}")
    }
}