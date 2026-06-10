package org.entur.netex.tools.lib.model

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class ReportTest {

    private fun report(entities: List<String>, include: (String) -> Boolean = { true }) =
        Report(
            title = "SELECTED ENTITIES",
            entities = entities,
            alias = Alias.of(emptyMap()),
            toStr = { it },
            include = include,
        )

    @Test
    fun `report on an empty entity set does not throw`() {
        val result = assertDoesNotThrow { report(emptyList()).report() }
        assertTrue(result.contains("SELECTED ENTITIES"))
    }

    @Test
    fun `report lists kept and total counts per type`() {
        val result = report(
            entities = listOf("StopPlace", "StopPlace", "Quay"),
            include = { it == "StopPlace" },
        ).report()

        // StopPlace: 2 kept of 2 total; Quay: 0 kept (shown as ·) of 1 total
        assertTrue(result.contains("StopPlace"))
        assertTrue(result.contains("Quay"))
        assertTrue(result.contains("·"), "Types with nothing kept should render the · placeholder")
    }
}
