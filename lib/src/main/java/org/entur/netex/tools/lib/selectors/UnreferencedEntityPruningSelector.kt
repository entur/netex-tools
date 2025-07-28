package org.entur.netex.tools.lib.selectors

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.selections.EntitySelection
import org.entur.netex.tools.lib.utils.Log

class UnreferencedEntityPruningSelector(
    private val entityTypes: List<String> = listOf(
        "Route",           // Routes that aren't used by any JourneyPattern
        "Line",            // Lines that aren't used by any ServiceJourney or Route
        "JourneyPattern",  // JourneyPatterns that aren't used by any ServiceJourney
        "DestinationDisplay", // DestinationDisplays not used by any journey pattern or service journey
        "RoutePoint",      // RoutePoints not used by any Route
        "PointOnRoute",    // PointOnRoute not used by any Route
        "OperatingPeriod"  // OperatingPeriods not used by any DayTypeAssignment
    )
): EntitySelector() {

    override fun selectEntities(model: EntityModel): EntitySelection {
        Log.info("Pruning unreferenced entities...")
        val entitiesByTypeAndEntity = model.getEntitesByTypeAndId()
        val prunedSelection = removeUnreferencedEntities(entitiesByTypeAndEntity, model)
        return EntitySelection(prunedSelection)
    }

    /**
     * Remove entities of specified types that are not referenced by any selected entities.
     * This is useful for cleaning up orphaned entities after filtering operations.
     * Runs multiple passes until no more entities are removed to handle cascading deletions.
     *
     * @param entityTypes List of entity types to check for unreferenced entities
     */
    fun removeUnreferencedEntities(selection: MutableMap<String, MutableMap<String, Entity>>, model: EntityModel): MutableMap<String, MutableMap<String, Entity>> {
        var entitiesRemoved: Boolean
        var pass = 1

        do {
            entitiesRemoved = false
            Log.info("Pruning pass $pass...")

            entityTypes.forEach { entityType ->
                if (removeUnreferencedEntitiesOfType(entityType, selection, model)) {
                    entitiesRemoved = true
                }
            }
            pass++
        } while (entitiesRemoved)

        Log.info("Pruning completed after ${pass - 1} passes")
        return selection
    }

    /**
     * Remove entities of a specific type that are not referenced by any selected entities.
     * @return true if any entities were removed, false otherwise
     */
    private fun removeUnreferencedEntitiesOfType(entityType: String, selection: MutableMap<String, MutableMap<String, Entity>>, model: EntityModel): Boolean {
        val entitiesOfType = selection[entityType] ?: return false
        val unreferencedIds = mutableSetOf<String>()

        // Check each entity of the specified type
        entitiesOfType.keys.forEach { entityId ->
            if (!isEntityReferenced(entityId, model, selection)) {
                unreferencedIds.add(entityId)
                Log.info("Removing unreferenced $entityType: $entityId")
            }
        }

        // Remove unreferenced entities
        unreferencedIds.forEach { entityId ->
            entitiesOfType.remove(entityId)
        }

        // Clean up empty type maps
        if (entitiesOfType.isEmpty()) {
            selection.remove(entityType)
        }

        return unreferencedIds.isNotEmpty()
    }

    /**
     * Check if an entity is referenced by any other selected entities.
     */
    private fun isEntityReferenced(entityId: String, model: EntityModel, selection: MutableMap<String, MutableMap<String, Entity>>): Boolean {
        return model.listAllRefs().any { ref ->
            ref.ref == entityId && isSelected(ref.source, selection)
        }
    }

    fun isSelected(e : Entity?, selection: MutableMap<String, MutableMap<String, Entity>>) : Boolean {
        if (e == null) { return false }
        return isSelected(e.type, e.id, selection)
    }

    fun isSelected(type : String, id : String, selection: MutableMap<String, MutableMap<String, Entity>>) : Boolean {
        val m = selection[type]
        return m != null && m.containsKey(id)
    }
}