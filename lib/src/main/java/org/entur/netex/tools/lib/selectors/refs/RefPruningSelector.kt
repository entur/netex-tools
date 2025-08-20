package org.entur.netex.tools.lib.selectors.refs

import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.selections.EntitySelection
import org.entur.netex.tools.lib.selections.RefSelection

class RefPruningSelector(
    private val entitySelection: EntitySelection,
    private val referencesToExcludeFromPruning: Set<String>,
): RefSelector() {
    override fun selectRefs(model: EntityModel): RefSelection {
        val allRefs = model.listAllRefs()
        val allEntityIds = entitySelection.allIds()
        val refsToKeep = allRefs
            .filter { allEntityIds.contains(it.ref) || it.type in referencesToExcludeFromPruning }
            .toHashSet()
        return RefSelection(refsToKeep)
    }
}