package org.entur.netex.tools.lib.plugin.activedates.helper

import org.entur.netex.tools.lib.extensions.putOrAddToSet
import org.entur.netex.tools.lib.model.EntityId
import org.entur.netex.tools.lib.model.NetexTypes

class ActiveEntitiesCollector {
    private val entities = mutableMapOf<String, MutableSet<EntityId>>()

    init {
        // Initialize all entity types with empty sets
        entities[NetexTypes.DATED_SERVICE_JOURNEY] = mutableSetOf()
        entities[NetexTypes.SERVICE_JOURNEY] = mutableSetOf()
        entities[NetexTypes.DAY_TYPE] = mutableSetOf()
        entities[NetexTypes.DAY_TYPE_ASSIGNMENT] = mutableSetOf()
        entities[NetexTypes.OPERATING_PERIOD] = mutableSetOf()
        entities[NetexTypes.OPERATING_DAY] = mutableSetOf()
        entities[NetexTypes.DEAD_RUN] = mutableSetOf()
    }

    fun addDatedServiceJourney(id: EntityId) = add(NetexTypes.DATED_SERVICE_JOURNEY, id)
    fun addServiceJourney(id: EntityId) = add(NetexTypes.SERVICE_JOURNEY, id)
    fun addDayType(id: EntityId) = add(NetexTypes.DAY_TYPE, id)
    fun addDayTypeAssignment(id: EntityId) = add(NetexTypes.DAY_TYPE_ASSIGNMENT, id)
    fun addOperatingPeriod(id: EntityId) = add(NetexTypes.OPERATING_PERIOD, id)
    fun addOperatingDay(id: EntityId) = add(NetexTypes.OPERATING_DAY, id)
    fun addDeadRun(id: EntityId) = add(NetexTypes.DEAD_RUN, id)

    private fun add(type: String, id: EntityId) = entities.putOrAddToSet(type, id)

    fun serviceJourneys(): Set<EntityId> = entities[NetexTypes.SERVICE_JOURNEY] ?: mutableSetOf()
    fun dayTypes(): Set<EntityId> = entities[NetexTypes.DAY_TYPE] ?: mutableSetOf()
    fun operatingDays(): Set<EntityId> = entities[NetexTypes.OPERATING_DAY] ?: mutableSetOf()
    fun deadRuns(): Set<EntityId> = entities[NetexTypes.DEAD_RUN] ?: mutableSetOf()

    // Additional getters for consistency
    fun datedServiceJourneys(): Set<EntityId> = entities[NetexTypes.DATED_SERVICE_JOURNEY] ?: mutableSetOf()
    fun dayTypeAssignments(): Set<EntityId> = entities[NetexTypes.DAY_TYPE_ASSIGNMENT] ?: mutableSetOf()
    fun operatingPeriods(): Set<EntityId> = entities[NetexTypes.OPERATING_PERIOD] ?: mutableSetOf()

    fun toMap(): Map<String, Set<EntityId>> = entities
}