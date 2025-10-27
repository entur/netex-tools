package org.entur.netex.tools.lib.selectors.entities

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.model.PublicationEnumeration
import org.entur.netex.tools.lib.selections.EntitySelection

class PassengerStopAssignmentSelector(
    private val entitySelection: EntitySelection
): EntitySelector() {
    override fun selectEntities(model: EntityModel): EntitySelection {
        val passengerStopAssignments = model.getEntitiesOfType(NetexTypes.PASSENGER_STOP_ASSIGNMENT)
        val psasToKeep = passengerStopAssignments.filter { passengerStopAssignment ->
            val psaId = passengerStopAssignment.id
            val scheduledStopPointRef = model.getRefsOfTypeFrom(psaId, "ScheduledStopPointRef").firstOrNull()?.ref
            if (scheduledStopPointRef != null) {
                val entitiesReferringToSsp = model.getEntitiesReferringTo(
                    Entity(
                        id = scheduledStopPointRef,
                        type = NetexTypes.SCHEDULED_STOP_POINT,
                        publication = PublicationEnumeration.RESTRICTED.value
                    )
                )

                val nonPSAsReferringToSsp = entitiesReferringToSsp.filter { it.type != NetexTypes.PASSENGER_STOP_ASSIGNMENT }
                val selectedEntitiesReferringToSsp = nonPSAsReferringToSsp.filter { entitySelection.includes(it) }
                selectedEntitiesReferringToSsp.isNotEmpty()
            }
            else {
                false
            }
        }

        entitySelection.selection[NetexTypes.PASSENGER_STOP_ASSIGNMENT] = mutableMapOf()
        psasToKeep.forEach { psaToKeep ->
            entitySelection.selection[NetexTypes.PASSENGER_STOP_ASSIGNMENT]!![psaToKeep.id] = psaToKeep
        }

        val scheduledStopPoints = model.getEntitiesOfType(NetexTypes.SCHEDULED_STOP_POINT)
        val sspsToKeep = scheduledStopPoints.filter { scheduledStopPoint ->
            val entitiesReferringToSsp = model.getEntitiesReferringTo(
                Entity(
                    id = scheduledStopPoint.id,
                    type = NetexTypes.SCHEDULED_STOP_POINT,
                    publication = PublicationEnumeration.RESTRICTED.value
                )
            )
            val nonPSAsReferringToSsp = entitiesReferringToSsp.filter { it.type != NetexTypes.PASSENGER_STOP_ASSIGNMENT }
            val selectedEntitiesReferringToSsp = nonPSAsReferringToSsp.filter { entitySelection.includes(it) }
            selectedEntitiesReferringToSsp.isNotEmpty()
        }

        entitySelection.selection[NetexTypes.SCHEDULED_STOP_POINT] = mutableMapOf()
        sspsToKeep.forEach { sspToKeep ->
            entitySelection.selection[NetexTypes.SCHEDULED_STOP_POINT]!![sspToKeep.id] = sspToKeep
        }

        return entitySelection
    }
}