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
        val context = EntitySelectorContext(
            entityModel = model,
            currentEntitySelection = currentEntitySelection
        )
        val (entitySelection, ms) = timedMs {
            logger.info("Running entity selector: ${selector::class.simpleName}")
            selector.selectEntities(context)
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

    fun filterAndPruneSelectionUntilTheyAreEqual(
        entitySelection: EntitySelection,
        entitySelectors: List<EntitySelector>,
        model: EntityModel
    ): EntitySelection {
        val maxIterations = 5
        var iterations = 0

        var prunedEntitySelection: EntitySelection? = null
        var filteredEntitySelection = entitySelection.copy()
        do {
            if (prunedEntitySelection != null) {
                filteredEntitySelection = prunedEntitySelection.copy()
            }

            for (entitySelector in entitySelectors) {
                filteredEntitySelection = runSelector(
                    model = model,
                    currentEntitySelection = filteredEntitySelection,
                    selector = entitySelector
                )
            }

            prunedEntitySelection = pruneUnreferencedEntities(
                model = model,
                currentEntitySelection = filteredEntitySelection,
                unreferencedEntitiesToPrune = filterConfig.unreferencedEntitiesToPrune
            )

            iterations++

            if (iterations >= maxIterations) {
                logger.warn("EntitySelection did not converge after $maxIterations iterations")
                break
            }
        } while (!filteredEntitySelection.isEqualTo(prunedEntitySelection))

        return prunedEntitySelection
    }

    override fun selectEntities(context: EntitySelectorContext): EntitySelection {
        var allEntitiesSelection = AllEntitiesSelector().selectEntities(context)
        var publicEntitySelection: EntitySelection? = null

        if (filterConfig.removePrivateData) {
            publicEntitySelection = removeRestrictedEntities(
                model = context.entityModel,
                currentEntitySelection = allEntitiesSelection
            )
        }

        val prunedEntitySelection = pruneUnreferencedEntities(
            model = context.entityModel,
            currentEntitySelection = publicEntitySelection?.copy() ?: allEntitiesSelection.copy(),
            unreferencedEntitiesToPrune = filterConfig.unreferencedEntitiesToPrune
        )

        val filteredEntitySelection = if (filterConfig.entitySelectors.isNotEmpty() && filterConfig.hasSpecifiedEntitiesToPrune()) {
            filterAndPruneSelectionUntilTheyAreEqual(
                entitySelection = prunedEntitySelection.copy(),
                filterConfig.entitySelectors,
                model = context.entityModel
            )
        } else {
            prunedEntitySelection
        }

        return filteredEntitySelection
    }
}