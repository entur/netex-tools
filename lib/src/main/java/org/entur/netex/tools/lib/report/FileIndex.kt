package org.entur.netex.tools.lib.report

import org.entur.netex.tools.lib.model.Entity
import java.io.File

class FileIndex {
    val elementTypesByFile = mutableMapOf<File, MutableMap<String, Int>>()
    val entitiesByFile = mutableMapOf<File, MutableSet<Entity>>()

    private fun incrementTypeCount(
        map: MutableMap<File, MutableMap<String, Int>>,
        file: File,
        type: String
    ) {
        map.compute(file) { _, mapOfTypes ->
            if (mapOfTypes == null || mapOfTypes.isEmpty()) {
                mutableMapOf(type to 1)
            } else if (mapOfTypes[type] == null) {
                mapOfTypes[type] = 1
                mapOfTypes
            } else {
                val entry = mapOfTypes[type]
                mapOfTypes[type] = entry!! + 1
                mapOfTypes
            }
        }
    }

    fun add(entity: Entity, file: File) {
        incrementTypeCount(elementTypesByFile, file, entity.type)
        entitiesByFile.computeIfAbsent(file) { mutableSetOf() }.add(entity)
    }
}