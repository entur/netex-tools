package org.entur.netex.tools.lib.selections

import org.entur.netex.tools.lib.data.TestDataFactory
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class EntitySelectionTest {
    @Test
    fun testIsEqualTo() {
        val entity1 = TestDataFactory.defaultEntity("1")
        val entity2 = TestDataFactory.defaultEntity("2")

        val selection1 = TestDataFactory.entitySelection(listOf(entity1, entity2))
        val selection2 = TestDataFactory.entitySelection(listOf(entity2, entity1))
        assertTrue(selection1.isEqualTo(selection2))

        val selection3 = TestDataFactory.entitySelection(listOf(entity1))
        val selection4 = TestDataFactory.entitySelection(listOf(entity2))
        assertFalse(selection3.isEqualTo(selection4))
        assertFalse(selection3.isEqualTo(selection1))
    }

    @Test
    fun testWithReplaced() {
        val entity1 = TestDataFactory.defaultEntity("1", type = "Type1")
        val entity2 = TestDataFactory.defaultEntity("2", type = "Type2")
        val selection = TestDataFactory.entitySelection(listOf(entity1, entity2))

        val entity3 = TestDataFactory.defaultEntity("3", type = "Type1")
        val newSelection = selection.withReplaced("Type1", mapOf("3" to entity3))
        assertTrue(newSelection.includes(entity3))
        assertTrue(newSelection.includes(entity2))
        assertFalse(newSelection.includes(entity1))
    }

    @Test
    fun testIsSelected() {
        val selectedEntity = TestDataFactory.defaultEntity("selectedEntity")
        val entitySelection = TestDataFactory.entitySelection(listOf(selectedEntity))

        assert(entitySelection.isSelected(selectedEntity))
        assert(entitySelection.isSelected(selectedEntity.type, selectedEntity.id))

        val unselectedEntity = TestDataFactory.defaultEntity("unselectedEntity")
        assert(!entitySelection.isSelected(unselectedEntity))
        assert(!entitySelection.isSelected(unselectedEntity.type, unselectedEntity.id))
    }

    @Test
    fun testCopy() {
        val entitySelectionOriginal = TestDataFactory.entitySelection(emptyList())
        val entitySelectionCopy = entitySelectionOriginal.copy()
        assertFalse(entitySelectionCopy == entitySelectionOriginal)
    }

    @Test
    fun testIncludes() {
        val selectedEntity = TestDataFactory.defaultEntity("selectedEntity")
        val includedElement = TestDataFactory.defaultElement(selectedEntity.type, selectedEntity.id)
        val nonIncludedElement = TestDataFactory.defaultElement(selectedEntity.type)
        val entitySelection = TestDataFactory.entitySelection(listOf(selectedEntity))

        assert(entitySelection.includes(includedElement))
        assert(!entitySelection.includes(nonIncludedElement))
    }

    @Test
    fun testAllIds() {
        val entity1 = TestDataFactory.defaultEntity("entity1")
        val entity2 = TestDataFactory.defaultEntity("entity2")
        val entity3 = TestDataFactory.defaultEntity("entity3")
        val entitySelection = TestDataFactory.entitySelection(listOf(entity1, entity2, entity3))

        val allIds = entitySelection.allIds()
        assert(allIds.contains(entity1.id))
        assert(allIds.contains(entity2.id))
        assert(allIds.contains(entity3.id))
        assert(allIds.size == 3)
    }

    @Test
    fun testIntersectWith() {
        val entity1 = TestDataFactory.defaultEntity("entity1")
        val entity2 = TestDataFactory.defaultEntity("entity2")
        val entity3 = TestDataFactory.defaultEntity("entity3")
        val entitySelection1 = TestDataFactory.entitySelection(listOf(entity1, entity2))
        val entitySelection2 = TestDataFactory.entitySelection(listOf(entity2, entity3))

        val intersection = entitySelection1.intersectWith(entitySelection2)

        assert(intersection.isSelected(entity2))
        assert(!intersection.isSelected(entity1))
        assert(!intersection.isSelected(entity3))
    }

    @Test
    fun testHasEntitiesReferringTo() {
        val entity1 = TestDataFactory.defaultEntity("entity1")
        val entity2 = TestDataFactory.defaultEntity("entity2")
        val ref = TestDataFactory.defaultRef(entity1.id)

        val entitySelection = TestDataFactory.entitySelection(listOf(entity1, entity2), setOf(ref))
        assert(entitySelection.hasEntitiesReferringTo(entity1))
        assert(!entitySelection.hasEntitiesReferringTo(entity2))
    }
}