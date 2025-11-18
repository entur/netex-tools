package org.entur.netex.tools.lib.selectors.refs

import org.entur.netex.tools.lib.config.FilterConfig
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.selections.EntitySelection
import org.entur.netex.tools.lib.selections.RefSelection
import org.entur.netex.tools.lib.utils.timedMs
import org.slf4j.LoggerFactory

class CompositeRefSelector(
    val filterConfig: FilterConfig,
    val entitySelection: EntitySelection,
): RefSelector {
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun setupRefSelectors(): List<RefSelector> {
        val selectors = mutableListOf<RefSelector>(AllRefsSelector())
        selectors.addAll(filterConfig.refSelectors)
        if (filterConfig.pruneReferences) {
            selectors.add(RefPruningSelector(entitySelection, filterConfig.referencesToExcludeFromPruning))
        }
        return selectors
    }

    private fun runRefSelector(selector: RefSelector, model: EntityModel): RefSelection {
        logger.info("Running ref selector: ${selector::class.simpleName}")
        return selector.selectRefs(model)
    }

    private fun runRefSelectors(selectors: List<RefSelector>, model: EntityModel): RefSelection {
        logger.info("Starting ref selection...")
        val (refSelection, ms) = timedMs {
            selectors
                .map { runRefSelector(it, model) }
                .reduce { acc, selection -> selection.intersectWith(acc) }
        }
        logger.info("Ref collection done in ${ms}ms. Collected a total of ${refSelection.selection.size} references")
        return refSelection
    }

    override fun selectRefs(model: EntityModel): RefSelection {
        val selectors = setupRefSelectors()
        return runRefSelectors(selectors, model)
    }
}