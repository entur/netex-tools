package org.entur.netex.tools.lib.plugin.activedates.helper

import org.entur.netex.tools.lib.extensions.putOrAddToSet
import org.entur.netex.tools.lib.model.NetexTypes

class ActiveEntitiesCollector {
    private val entities = mutableMapOf<String, MutableSet<String>>()

    init {
        // Initialize all entity types with empty sets
        entities[NetexTypes.DATED_SERVICE_JOURNEY] = mutableSetOf()
        entities[NetexTypes.SERVICE_JOURNEY] = mutableSetOf()
        entities[NetexTypes.DAY_TYPE] = mutableSetOf()
        entities[NetexTypes.DAY_TYPE_ASSIGNMENT] = mutableSetOf()
        entities[NetexTypes.OPERATING_PERIOD] = mutableSetOf()
        entities[NetexTypes.OPERATING_DAY] = mutableSetOf()
    }

    fun addDatedServiceJourney(id: String) = add(NetexTypes.DATED_SERVICE_JOURNEY, id)
    fun addServiceJourney(id: String) = add(NetexTypes.SERVICE_JOURNEY, id)
    fun addDayType(id: String) = add(NetexTypes.DAY_TYPE, id)
    fun addDayTypeAssignment(id: String) = add(NetexTypes.DAY_TYPE_ASSIGNMENT, id)
    fun addOperatingPeriod(id: String) = add(NetexTypes.OPERATING_PERIOD, id)
    fun addOperatingDay(id: String) = add(NetexTypes.OPERATING_DAY, id)

    private fun add(type: String, id: String) = entities.putOrAddToSet(type, id)

    fun serviceJourneys(): Set<String> = entities[NetexTypes.SERVICE_JOURNEY] ?: mutableSetOf()
    fun dayTypes(): Set<String> = entities[NetexTypes.DAY_TYPE] ?: mutableSetOf()
    fun operatingDays(): Set<String> = entities[NetexTypes.OPERATING_DAY] ?: mutableSetOf()

    // Additional getters for consistency
    fun datedServiceJourneys(): Set<String> = entities[NetexTypes.DATED_SERVICE_JOURNEY] ?: mutableSetOf()
    fun dayTypeAssignments(): Set<String> = entities[NetexTypes.DAY_TYPE_ASSIGNMENT] ?: mutableSetOf()
    fun operatingPeriods(): Set<String> = entities[NetexTypes.OPERATING_PERIOD] ?: mutableSetOf()

    fun toMap(): Map<String, Set<String>> = entities
}