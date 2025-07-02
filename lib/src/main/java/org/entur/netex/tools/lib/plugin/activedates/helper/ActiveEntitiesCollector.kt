package org.entur.netex.tools.lib.plugin.activedates.helper

import org.entur.netex.tools.lib.extensions.putOrAddToSet
import org.entur.netex.tools.lib.model.NetexTypes

class ActiveEntitiesCollector {
    private val entities = mutableMapOf<String, MutableSet<String>>()
    
    fun addServiceJourney(id: String) = add(NetexTypes.SERVICE_JOURNEY, id)
    fun addDayType(id: String) = add(NetexTypes.DAY_TYPE, id)
    fun addOperatingPeriod(id: String) = add(NetexTypes.OPERATING_PERIOD, id)
    fun addOperatingDay(id: String) = add(NetexTypes.OPERATING_DAY, id)
    
    private fun add(type: String, id: String) {
        entities.putOrAddToSet(type, id)
    }
    
    fun toMap(): Map<String, MutableSet<String>> = entities
}