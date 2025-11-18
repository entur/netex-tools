package org.entur.netex.tools.lib.selectors.entities

import org.entur.netex.tools.lib.config.FilterConfig
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.selections.EntitySelection
import org.entur.netex.tools.lib.utils.timedMs
import org.slf4j.LoggerFactory

class CompositeEntitySelector(
    private val filterConfig: FilterConfig,
): EntitySelector {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun runSelector(
        selector: EntitySelector,
        model: EntityModel,
        currentEntitySelection: EntitySelection
    ): EntitySelection {
        val (entitySelection, ms) = timedMs {
            logger.info("Running entity selector: ${selector::class.simpleName}")
            selector.selectEntities(model, currentEntitySelection)
        }
        val accumulatedEntitySelection = entitySelection.intersectWith(currentEntitySelection)
        logger.info("Entity selector ${selector::class.simpleName} finished in ${ms}ms")
        return accumulatedEntitySelection
    }

    fun removeRestrictedEntities(
        model: EntityModel,
        currentEntitySelection: EntitySelection
    ): EntitySelection =
        runSelector(
            selector = PublicEntitiesSelector(),
            model = model,
            currentEntitySelection = currentEntitySelection
        )

    fun pruneUnreferencedEntities(
        model: EntityModel,
        currentEntitySelection: EntitySelection,
        unreferencedEntitiesToPrune: Set<String>,
    ): EntitySelection {
        val selector = EntityPruningSelector(
            entitySelection = currentEntitySelection,
            typesToRemove = unreferencedEntitiesToPrune
        )
        return runSelector(
            selector = selector,
            model = model,
            currentEntitySelection = currentEntitySelection
        )
    }

    override fun selectEntities(
        model: EntityModel,
        currentEntitySelection: EntitySelection?
    ): EntitySelection {
        var entitySelection = AllEntitiesSelector().selectEntities(model)
        if (filterConfig.removePrivateData) {
            entitySelection = removeRestrictedEntities(
                model = model,
                currentEntitySelection = entitySelection
            )
        }

        if (filterConfig.hasSpecifiedEntitiesToPrune()) {
            entitySelection = pruneUnreferencedEntities(
                model = model,
                currentEntitySelection = entitySelection,
                unreferencedEntitiesToPrune = filterConfig.unreferencedEntitiesToPrune
            )
        }
        for (entitySelector in filterConfig.entitySelectors) {
            entitySelection = runSelector(
                model = model,
                currentEntitySelection = entitySelection,
                selector = entitySelector
            )
        }
        if (filterConfig.entitySelectors.isNotEmpty() && filterConfig.hasSpecifiedEntitiesToPrune()) {
            entitySelection = pruneUnreferencedEntities(
                model = model,
                currentEntitySelection = entitySelection,
                unreferencedEntitiesToPrune = filterConfig.unreferencedEntitiesToPrune
            )
        }

        return entitySelection
    }
}