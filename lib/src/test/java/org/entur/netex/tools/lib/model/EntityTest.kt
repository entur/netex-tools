package org.entur.netex.tools.lib.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class EntityTest {

    val gpID = "gp"
    val pID = "p"
    val cID = "c"

    val grandparent = Entity(gpID, "GrandParent")
    val parent = Entity(pID, "Parent", grandparent)
    val child = Entity(cID, "Child", parent)

    @Test
    fun testToString() {
        assertEquals("(gp GrandParent)", grandparent.toString())
        assertEquals("(p GrandParent/Parent)", parent.toString())
        assertEquals("(c GrandParent/Parent/Child)", child.toString())
    }

    @Test
    fun path() {
        assertEquals("", grandparent.path())
        assertEquals("GrandParent/", parent.path())
        assertEquals("Parent/GrandParent/", child.path())
    }

    @Test
    fun fullPath() {
        assertEquals("GrandParent", grandparent.fullPath())
        assertEquals("GrandParent/Parent", parent.fullPath())
        assertEquals("GrandParent/Parent/Child", child.fullPath())
    }

    @Test
    fun getId() {
        assertEquals(gpID, grandparent.id)
        assertEquals(pID, parent.id)
        assertEquals(cID, child.id)
    }

    @Test
    fun getType() {
        assertEquals("GrandParent", grandparent.type)
        assertEquals("Parent", parent.type)
        assertEquals("Child", child.type)
    }

    @Test
    fun getParent() {
        assertNull(grandparent.parent)
        assertEquals(grandparent, parent.parent)
        assertEquals(parent, child.parent)
    }
}