package org.entur.netex.tools.lib.report

import org.entur.netex.tools.lib.model.Entity
import java.io.File

class FileIndex {
    val elementTypesByFile = mutableMapOf<File, MutableMap<String, Int>>()
    val entitiesByFile = mutableMapOf<File, MutableSet<Entity>>()

    val elementTypesByDocument = mutableMapOf<String, MutableMap<String, Int>>()
    val entitiesByDocument = mutableMapOf<String, MutableSet<Entity>>()

    private fun <K> incrementTypeCount(
        map: MutableMap<K, MutableMap<String, Int>>,
        key: K,
        type: String
    ) {
        map.compute(key) { _, mapOfTypes ->
            if (mapOfTypes == null || mapOfTypes.isEmpty()) {
                mutableMapOf(type to 1)
            } else if (mapOfTypes[type] == null) {
                mapOfTypes[type] = 1
                mapOfTypes
            } else {
                mapOfTypes[type] = (mapOfTypes[type] ?: 0) + 1
                mapOfTypes
            }
        }
    }

    fun add(entity: Entity, file: File) {
        incrementTypeCount(elementTypesByFile, file, entity.type)
        entitiesByFile.computeIfAbsent(file) { mutableSetOf() }.add(entity)
    }

    fun add(entity: Entity, documentName: String) {
        incrementTypeCount(elementTypesByDocument, documentName, entity.type)
        entitiesByDocument.computeIfAbsent(documentName) { mutableSetOf() }.add(entity)
    }
}