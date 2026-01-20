package org.entur.netex.tools.lib.report

import org.entur.netex.tools.lib.data.TestDataFactory
import org.entur.netex.tools.lib.model.Entity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class FilterReportTest {

    private fun createFilterReport(
        elementTypesByFile: Map<File, Map<String, Int>>,
        entitiesByFile: Map<File, Set<Entity>>,
    ) = FilterReport(
        elementTypesByFile = elementTypesByFile,
        entitiesByFile = entitiesByFile,
    )

    @Test
    fun getNumberOfElementsByFile() {
        val filterReport = createFilterReport(
            elementTypesByFile = mapOf(
                File("file1") to mapOf("type1" to 3, "type2" to 4),
                File("file2") to mapOf("type1" to 2),
            ),
            entitiesByFile = emptyMap(),
        )
        assertEquals(3, filterReport.getNumberOfElementsByFile(File("file1"), "type1"))
        assertEquals(4, filterReport.getNumberOfElementsByFile(File("file1"), "type2"))
        assertEquals(2, filterReport.getNumberOfElementsByFile(File("file2"), "type1"))
    }

    @Test
    fun getAllEntityIdsByFiles() {
        val filterReport = createFilterReport(
            elementTypesByFile = emptyMap(),
            entitiesByFile = mapOf(
                File("file1") to setOf(
                    TestDataFactory.defaultEntity(id="id1"),
                    TestDataFactory.defaultEntity(id="id2"),
                ),
                File("file2") to setOf(
                    TestDataFactory.defaultEntity(id="id3"),
                ),
            ),
        )

        filterReport.getAllEntityIdsByFiles(setOf(File("file1")))
    }

    @Test
    fun getNumberOfElementsOfType() {
        val filterReport = createFilterReport(
            elementTypesByFile = mapOf(
                File("file1") to mapOf("type1" to 3, "type2" to 4),
                File("file2") to mapOf("type1" to 2),
            ),
            entitiesByFile = emptyMap(),
        )
        assertEquals(5, filterReport.getNumberOfElementsOfType("type1"))
        assertEquals(4, filterReport.getNumberOfElementsOfType("type2"))
    }
}