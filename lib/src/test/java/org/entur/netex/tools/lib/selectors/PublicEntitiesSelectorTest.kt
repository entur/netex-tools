package org.entur.netex.tools.lib.selectors

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.PublicationEnumeration
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PublicEntitiesSelectorTest {
    @Test
    fun testSelector() {
        val publicEntity = Entity("1", "Route", PublicationEnumeration.PUBLIC.value)
        val restrictedEntity = Entity("2", "Line", PublicationEnumeration.RESTRICTED.value)
        val entities = listOf(publicEntity, restrictedEntity)
        val selection = PublicEntitiesSelector().selector(entities)

        assertEquals(1, selection.selection.size)
        assertTrue(selection.selection.containsKey(publicEntity.type))
        assertFalse(selection.selection.containsKey(restrictedEntity.type))
    }
}