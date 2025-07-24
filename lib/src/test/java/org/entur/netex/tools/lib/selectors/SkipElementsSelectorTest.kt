package org.entur.netex.tools.lib.selectors

import org.entur.netex.tools.lib.model.Entity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SkipElementsSelectorTest {
    @Test
    fun testSelector() {
        val entity1 = Entity("1", "Route", "PUBLIC")
        val entity2 = Entity("2", "Line", "PUBLIC")
        val entity3 = Entity("3", "StopPlace", "PUBLIC")
        val entity4 = Entity("4", "RestrictedElement", "RESTRICTED")

        val entities = listOf(entity1, entity2, entity3, entity4)
        val elementsToSkip = setOf("RestrictedElement")

        val selection = SkipElementsSelector(elementsToSkip).selector(entities)

        assertEquals(3, selection.selection.size)
        assertTrue(selection.selection.containsKey(entity1.type))
        assertTrue(selection.selection.containsKey(entity2.type))
        assertTrue(selection.selection.containsKey(entity3.type))
        assertFalse(selection.selection.containsKey(entity4.type))
    }
}