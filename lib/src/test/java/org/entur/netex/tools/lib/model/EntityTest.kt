package org.entur.netex.tools.lib.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class EntityTest {

    val gpID = "gp"
    val pID = "p"
    val cID = "c"

    val grandParent = Entity(gpID, "GrandParent", PublicationEnumeration.PUBLIC.value)
    val parent = Entity(pID, "Parent", PublicationEnumeration.PUBLIC.value, grandParent)
    val child = Entity(cID, "Child", PublicationEnumeration.PUBLIC.value, parent)

    @Test
    fun testToString() {
        assertEquals("(gp GrandParent)", grandParent.toString())
        assertEquals("(p GrandParent/Parent)", parent.toString())
        assertEquals("(c GrandParent/Parent/Child)", child.toString())
    }

    @Test
    fun path() {
        assertEquals("", grandParent.path())
        assertEquals("GrandParent/", parent.path())
        assertEquals("Parent/GrandParent/", child.path())
    }

    @Test
    fun fullPath() {
        assertEquals("GrandParent", grandParent.fullPath())
        assertEquals("GrandParent/Parent", parent.fullPath())
        assertEquals("GrandParent/Parent/Child", child.fullPath())
    }

    @Test
    fun getId() {
        assertEquals(gpID, grandParent.id)
        assertEquals(pID, parent.id)
        assertEquals(cID, child.id)
    }

    @Test
    fun getType() {
        assertEquals("GrandParent", grandParent.type)
        assertEquals("Parent", parent.type)
        assertEquals("Child", child.type)
    }

    @Test
    fun getParent() {
        assertNull(grandParent.parent)
        assertEquals(grandParent, parent.parent)
        assertEquals(parent, child.parent)
    }
}