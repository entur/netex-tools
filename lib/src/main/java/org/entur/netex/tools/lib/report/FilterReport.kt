package org.entur.netex.tools.lib.report

import org.entur.netex.tools.lib.model.Entity
import java.io.File

data class FilterReport(
    val elementTypesByFile: Map<File, Map<String, Int>>,
    val entitiesByFile: Map<File, Set<Entity>>,
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
}