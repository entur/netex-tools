package org.entur.netex.tools.lib.selectors.entities

import org.entur.netex.tools.lib.data.TestDataFactory
import org.entur.netex.tools.lib.model.Entity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SkipElementsSelectorTest {
    @Test
    fun testSelectEntities() {
        val entityModel = TestDataFactory.defaultEntityModel()

        entityModel.addEntity(Entity("1", "Route", "PUBLIC"))
        entityModel.addEntity(Entity("2", "Line", "PUBLIC"))
        entityModel.addEntity(Entity("3", "StopPlace", "PUBLIC"))
        entityModel.addEntity(Entity("4", "RestrictedElement", "RESTRICTED"))

        val elementsToSkip = setOf("RestrictedElement")
        val selection = SkipElementsSelector(elementsToSkip).selectEntities(entityModel)

        assertEquals(3, selection.selection.size)
        assertTrue(selection.selection.containsKey("Route"))
        assertTrue(selection.selection.containsKey("Line"))
        assertTrue(selection.selection.containsKey("StopPlace"))
        assertFalse(selection.selection.containsKey("RestrictedElement"))
    }
}