package org.entur.netex.tools.lib.report

import org.entur.netex.tools.lib.model.Entity
import java.io.File

data class FilterReport(
    val elementTypesByFile: Map<File, Map<String, Int>>,
    val entitiesByFile: Map<File, Set<Entity>>,
    val elementTypesByDocument: Map<String, Map<String, Int>> = emptyMap(),
    val entitiesByDocument: Map<String, Set<Entity>> = emptyMap(),
) {
    fun getNumberOfElementsByFile(file: File, type: String): Int {
        return elementTypesByFile[file]?.get(type) ?: 0
    }

    fun getAllEntityIdsByFiles(files: Set<File>): Set<String> {
        return entitiesByFile.filter { (file, _) -> files.contains(file) }
            .flatMap { (_, entities) -> entities.map { it.id } }
            .sorted()
            .toSet()
    }

    fun getNumberOfElementsByDocument(documentName: String, type: String): Int {
        return elementTypesByDocument[documentName]?.get(type) ?: 0
    }

    fun getAllEntityIdsByDocuments(documentNames: Set<String>): Set<String> {
        return entitiesByDocument.filter { (name, _) -> documentNames.contains(name) }
            .flatMap { (_, entities) -> entities.map { it.id } }
            .sorted()
            .toSet()
    }

    fun getNumberOfElementsOfType(type: String): Int {
        val fromFiles = elementTypesByFile.values.sumOf { it[type] ?: 0 }
        val fromDocuments = elementTypesByDocument.values.sumOf { it[type] ?: 0 }
        return fromFiles + fromDocuments
    }
}