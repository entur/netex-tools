package org.entur.netex.tools.lib.selectors.entities

import org.entur.netex.tools.lib.data.TestDataFactory
import org.entur.netex.tools.lib.model.Alias
import org.entur.netex.tools.lib.model.EntityModel
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class EntityPruningSelectorTest {
    @Test
    fun testEntityPruningSelector() {
        val entitySelection = TestDataFactory.entitySelectionWithUnreferredEntities()
        val selector = EntityPruningSelector(
            typesToRemove = setOf("unreferencedType"),
            entitySelection,
        )
        val selection = selector.selectEntities(EntityModel(Alias.of(emptyMap())))

        assertNotNull(selection)

        val keptEntities = selection.selection.get("unreferencedType")?.keys ?: setOf()
        assertEquals(0, keptEntities.size)
    }

    @Test
    fun testEntityPruningSelectorKeepsEntitiesByDefault() {
        val entitySelection = TestDataFactory.entitySelectionWithUnreferredEntities()
        val selector = EntityPruningSelector(
            typesToRemove = setOf(),
            entitySelection,
        )
        val selection = selector.selectEntities(EntityModel(Alias.of(emptyMap())))

        assertNotNull(selection)

        val keptEntities = selection.selection.get("unreferencedType")?.keys ?: setOf()
        assertEquals(4, keptEntities.size)
    }

    @Test
    fun testEntityPruningSelectorKeepsEntityWhenSomethingRefersToIt() {
        val entitySelection = TestDataFactory.entitySelectionWithReferredEntities()
        val selector = EntityPruningSelector(
            typesToRemove = setOf("unreferencedType"),
            entitySelection,
        )
        val selection = selector.selectEntities(EntityModel(Alias.of(emptyMap())))

        assertNotNull(selection)

        val keptEntities = selection.selection.get("unreferencedType")?.keys ?: setOf()
        assertEquals(2, keptEntities.size)
    }
}