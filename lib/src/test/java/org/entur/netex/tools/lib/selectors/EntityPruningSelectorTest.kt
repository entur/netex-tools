package org.entur.netex.tools.lib.selectors

import org.entur.netex.tools.lib.data.TestDataFactory
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class EntityPruningSelectorTest {
    @Test
    fun testEntityPruningSelector() {
        val entityModel = TestDataFactory.entityModelWithReferences()
        val entitySelection = TestDataFactory.entitySelectionWithUnreferredEntities()
        val selector = EntityPruningSelector(
            entitySelection,
            unreferencedTypesToRemove = setOf("unreferencedType")
        )
        val selection = selector.selectEntities(entityModel)

        assertNotNull(selection)

        val keptEntities = selection.selection.get("unreferencedType")?.keys ?: setOf()
        assertEquals(3, keptEntities.size)
        assert(keptEntities.containsAll(listOf("entity1", "entity2", "entity3")))

    }
}