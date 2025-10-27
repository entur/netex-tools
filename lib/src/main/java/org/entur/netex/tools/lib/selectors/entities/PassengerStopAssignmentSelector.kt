package org.entur.netex.tools.lib.selectors.entities

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.selections.EntitySelection

/**
 * Filters PassengerStopAssignment and ScheduledStopPoint entities when the only reference made to a
 * ScheduledStopPoint is made from a PassengerStopAssignment in the provided entity selection. In these cases,
 * both the PassengerStopAssignment and ScheduledStopPoint will be removed from the EntitySelection returned from
 * selectEntities.
 * */
class PassengerStopAssignmentSelector(
    private val entitySelection: EntitySelection
): EntitySelector() {
    fun excludePassengerStopAssignments(entities: Collection<Entity>): Collection<Entity> =
        entities.filter { it.type !== NetexTypes.PASSENGER_STOP_ASSIGNMENT }

    fun scheduledStopPointIsReferredToInSelection(scheduledStopPointId: String?, model: EntityModel): Boolean {
        if (scheduledStopPointId == null) {
            return false
        }

        val referringEntities = model.getEntitiesReferringTo(scheduledStopPointId)
        val referringEntitiesExcludingAssignments = excludePassengerStopAssignments(referringEntities)
        val referringEntitiesInSelection = referringEntitiesExcludingAssignments.filter { entitySelection.includes(it) }
        return referringEntitiesInSelection.isNotEmpty()
    }

    fun findPassengerStopAssignmentsToKeep(model: EntityModel): List<Entity> {
        val passengerStopAssignments = model.getEntitiesOfType(NetexTypes.PASSENGER_STOP_ASSIGNMENT)
        return passengerStopAssignments.filter { passengerStopAssignment ->
            val psaId = passengerStopAssignment.id
            val scheduledStopPointRef = model.getRefsOfTypeFrom(psaId, "ScheduledStopPointRef").firstOrNull()?.ref
            scheduledStopPointIsReferredToInSelection(scheduledStopPointRef, model)
        }
    }

    fun findScheduledStopPointsToKeep(model: EntityModel): List<Entity> {
        val scheduledStopPoints = model.getEntitiesOfType(NetexTypes.SCHEDULED_STOP_POINT)
        return scheduledStopPoints.filter { scheduledStopPoint ->
            scheduledStopPointIsReferredToInSelection(scheduledStopPoint.id, model)
        }
    }

    override fun selectEntities(model: EntityModel): EntitySelection {
        val passengerStopAssignmentsToKeep = findPassengerStopAssignmentsToKeep(model)
        entitySelection.selection[NetexTypes.PASSENGER_STOP_ASSIGNMENT] = mutableMapOf()
        passengerStopAssignmentsToKeep.forEach { psaToKeep ->
            entitySelection.selection[NetexTypes.PASSENGER_STOP_ASSIGNMENT]!![psaToKeep.id] = psaToKeep
        }

        val scheduledStopPointsToKeep = findScheduledStopPointsToKeep(model)
        entitySelection.selection[NetexTypes.SCHEDULED_STOP_POINT] = mutableMapOf()
        scheduledStopPointsToKeep.forEach { sspToKeep ->
            entitySelection.selection[NetexTypes.SCHEDULED_STOP_POINT]!![sspToKeep.id] = sspToKeep
        }

        return entitySelection
    }
}