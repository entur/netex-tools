package org.entur.netex.tools.lib.selections

import org.entur.netex.tools.lib.data.TestDataFactory
import org.entur.netex.tools.lib.model.EntityId
import org.junit.jupiter.api.Test

class EntitySelectionTest {
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
    fun testIncludes() {
        val selectedEntity = TestDataFactory.defaultEntity("selectedEntity")
        val selectedEntityId = (selectedEntity.id as EntityId.Simple).id
        val includedElement = TestDataFactory.defaultElement(selectedEntity.type, selectedEntityId)
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
        val entity1Id = (entity1.id as EntityId.Simple).id
        val ref = TestDataFactory.defaultRef(entity1Id)

        val entitySelection = TestDataFactory.entitySelection(listOf(entity1, entity2), setOf(ref))
        assert(entitySelection.hasEntitiesReferringTo(entity1))
        assert(!entitySelection.hasEntitiesReferringTo(entity2))
    }
}