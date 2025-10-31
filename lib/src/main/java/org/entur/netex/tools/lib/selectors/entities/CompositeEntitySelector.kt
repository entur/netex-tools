package org.entur.netex.tools.lib.selectors.entities

import org.entur.netex.tools.lib.config.FilterConfig
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesPlugin
import org.entur.netex.tools.lib.selections.EntitySelection
import org.entur.netex.tools.lib.utils.timedMs
import org.slf4j.LoggerFactory

class CompositeEntitySelector(
    private val filterConfig: FilterConfig,
    private val activeDatesPlugin: ActiveDatesPlugin,
): EntitySelector() {
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun getInitialEntitySelectors(): List<EntitySelector> {
        val selectors = mutableListOf<EntitySelector>(AllEntitiesSelector())
        if (filterConfig.removePrivateData) {
            selectors.add(PublicEntitiesSelector())
        }
        if (filterConfig.period.start != null || filterConfig.period.end != null) {
            selectors.add(
                ActiveDatesSelector(
                    period = filterConfig.period,
                    activeDatesPlugin = activeDatesPlugin,
                )
            )
        }

        return selectors
    }

    private fun runSelector(selector: EntitySelector, entityModel: EntityModel): EntitySelection {
        logger.info("Running entity selector: ${selector::class.simpleName}")
        return selector.selectEntities(entityModel)
    }

    private fun selectEntitiesToKeep(selectors: List<EntitySelector>, entityModel: EntityModel): EntitySelection =
        selectors
            .map { runSelector(it, entityModel) }
            .reduce { acc, selection -> selection.intersectWith(acc) }


    private fun runInitialEntitySelection(entityModel: EntityModel): EntitySelection {
        logger.info("Starting initial entity selection...")
        val (entitySelection, ms) = timedMs {
            val selectors = getInitialEntitySelectors()
            selectEntitiesToKeep(
                entityModel = entityModel,
                selectors = selectors
            )
        }
        logger.info("Initial entity collection done in ${ms}ms. Collected a total of ${entitySelection.allIds().size} entities")
        return entitySelection
    }

    private fun pruneUnreferencedEntities(
        entityModel: EntityModel,
        entitySelection: EntitySelection
    ): EntitySelection {
        logger.info("Pruning unreferenced entities...")
        val (prunedEntitySelection, ms) = timedMs {
            val selector = EntityPruningSelector(
                entitySelection = entitySelection,
                typesToRemove = filterConfig.unreferencedEntitiesToPrune
            )
            runSelector(selector, entityModel).intersectWith(entitySelection)
        }
        logger.info("Pruned unreferenced entities in ${ms}ms")
        return prunedEntitySelection
    }

    private fun removeInterchangesWithoutServiceJourneys(
        entityModel: EntityModel,
        entitySelection: EntitySelection
    ): EntitySelection {
        logger.info("Removing interchanges without service journeys...")
        val (entitySelectionWithInterchangesRemoved, ms) = timedMs {
            val selector = ServiceJourneyInterchangeSelector(entitySelection)
            runSelector(selector, entityModel).intersectWith(entitySelection)
        }
        logger.info("Removed interchanges without service journeys in ${ms}ms")
        return entitySelectionWithInterchangesRemoved
    }

    private fun removePassengerStopAssignmentsWithUnreferredScheduledStopPoint(
        entityModel: EntityModel,
        entitySelection: EntitySelection
    ): EntitySelection {
        logger.info("Removing passenger stop assignments with unreferred ScheduledStopPoint...")
        val (entitySelectionWithAssignmentsRemoved, ms) = timedMs {
            PassengerStopAssignmentSelector(entitySelection)
                .selectEntities(entityModel)
                .intersectWith(entitySelection)
        }
        logger.info("Removed passenger stop assignments with unreferred ScheduledStopPoint in ${ms}ms")
        return entitySelectionWithAssignmentsRemoved
    }

    override fun selectEntities(model: EntityModel): EntitySelection {
        var entitySelection = runInitialEntitySelection(model)
        if (filterConfig.hasSpecifiedEntitiesToPrune()) {
            entitySelection = pruneUnreferencedEntities(model, entitySelection)
        }
        if (filterConfig.removeInterchangesWithoutServiceJourneys) {
            entitySelection = removeInterchangesWithoutServiceJourneys(model, entitySelection)
        }
        if (filterConfig.removePassengerStopAssignmentsWithUnreferredScheduledStopPoint) {
            entitySelection = removePassengerStopAssignmentsWithUnreferredScheduledStopPoint(model, entitySelection)
        }
        return entitySelection
    }
}