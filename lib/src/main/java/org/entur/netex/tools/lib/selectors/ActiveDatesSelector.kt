package org.entur.netex.tools.lib.selectors

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.selections.EntitySelection
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesCalculator
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesPlugin
import java.time.LocalDate
import kotlin.collections.forEach

class ActiveDatesSelector(val activeDatesPlugin: ActiveDatesPlugin, val model: EntityModel, val fromDate: LocalDate, val toDate: LocalDate): EntitySelector() {

    override fun selector(entitySelection: EntitySelection): EntitySelection {
        val calculator = ActiveDatesCalculator(activeDatesPlugin.getCollectedData())
        val activeEntities = calculator.activeDateEntitiesInPeriod(fromDate, toDate, model)

        val activeEntitiesMap = mutableMapOf<String, MutableMap<String, Entity>>()
        entitySelection.selection.forEach { (type, entities) ->
            if (activeEntities.containsKey(type)) {
                val idsOfActiveEntitiesWithType = activeEntities[type]
                val entitiesToKeep = entities.filter { idsOfActiveEntitiesWithType?.contains(it.key) == true  }
                if (entitiesToKeep.isNotEmpty()) {
                    activeEntitiesMap.put(type, entitiesToKeep.toMutableMap())
                }
            } else {
                // If no active entities for this type, keep all entities of this type
                activeEntitiesMap.put(type, entities)
            }
        }

        return EntitySelection(activeEntitiesMap)
    }
}