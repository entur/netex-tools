package org.entur.netex.tools.lib.selectors

import org.entur.netex.tools.lib.data.TestDataFactory
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.PublicationEnumeration
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PublicEntitiesSelectorTest {
    @Test
    fun testSelectEntities() {
        val entityModel = TestDataFactory.defaultEntityModel()
        val publicEntity = Entity("1", "Route", PublicationEnumeration.PUBLIC.value)
        val restrictedEntity = Entity("2", "Line", PublicationEnumeration.RESTRICTED.value)
        entityModel.addEntity(publicEntity)
        entityModel.addEntity(restrictedEntity)
        val selection = PublicEntitiesSelector().selectEntities(entityModel)

        assertEquals(1, selection.selection.size)
        assertTrue(selection.selection.containsKey(publicEntity.type))
        assertFalse(selection.selection.containsKey(restrictedEntity.type))
    }
}