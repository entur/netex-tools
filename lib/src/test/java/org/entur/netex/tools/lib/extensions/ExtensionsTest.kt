package org.entur.netex.tools.lib.extensions

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ExtensionsTest {
    @Test
    fun putOrAddToSet() {
        val map = mapOf(
            "a" to mutableSetOf(1, 2),
            "b" to mutableSetOf(3)
        ).toMutableMap()
        map.putOrAddToSet("c", 4)
        map.putOrAddToSet("a", 3)
        assertEquals(setOf(1, 2, 3), map["a"])
        assertEquals(setOf(3), map["b"])
        assertEquals(setOf(4), map["c"])
    }

    @Test
    fun middle() {
        val list1 = listOf(1, 2, 3, 4, 5)
        val list2 = listOf(1, 2)
        val list3 = listOf(1, 2, 3)

        assertEquals(listOf(2, 3, 4), list1.middle())
        assertEquals(emptyList<Int>(), list2.middle())
        assertEquals(listOf(2), list3.middle())
    }

    @Test
    fun putOrAddToList() {
        val map = mapOf(
            "a" to mutableListOf(1, 2),
            "b" to mutableListOf(3)
        ).toMutableMap()
        map.putOrAddToList("c", 4)
        map.putOrAddToList("a", 3)
        assertEquals(listOf(1, 2, 3), map["a"])
        assertEquals(listOf(3), map["b"])
        assertEquals(listOf(4), map["c"])
    }

    @Test
    fun hasAttribute() {
        val attributes = org.xml.sax.helpers.AttributesImpl()
        attributes.addAttribute("", "attr1", "attr1", "CDATA", "value1")
        assertTrue(attributes.hasAttribute("attr1"))
        assertFalse(attributes.hasAttribute("attr2"))
    }

    @Test
    fun toMap() {
        val attributes = org.xml.sax.helpers.AttributesImpl()
        attributes.addAttribute("", "attr1", "attr1", "CDATA", "value1")
        assertTrue(attributes.toMap().containsKey("attr1"))
    }

    @Test
    fun toAttributes() {
        val map = mapOf("attr1" to "value1", "attr2" to "value2")
        val attributes = map.toAttributes()
        assertEquals("value1", attributes.getValue("attr1"))
        assertEquals("value2", attributes.getValue("attr2"))
    }

    @Test
    fun addNewAttribute() {
        val attributes = org.xml.sax.helpers.AttributesImpl()
        attributes.addNewAttribute("attr1", "value1")
        assertEquals("value1", attributes.getValue("attr1"))
    }

    @Test
    fun toISO8601() {
        val date = java.time.LocalDate.of(2023, 3, 15)
        assertEquals("2023-03-15T00:00:00", date.toISO8601())
    }

}