package org.entur.netex.tools.lib.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CompositeEntityIdTest {

    @Test
    fun `composite id joins non-null parts with the delimiter`() {
        val id = CompositeEntityId.ByIdVersionAndOrder("PSA:1", "1", "5").id
        assertEquals("PSA:1|1|5", id)
    }

    @Test
    fun `null version is normalised to an empty segment`() {
        val id = CompositeEntityId.ByIdVersionAndOrder("PSA:1", null, "5").id
        assertEquals("PSA:1||5", id)
    }

    @Test
    fun `null order is normalised to an empty segment`() {
        val id = CompositeEntityId.ByIdVersionAndOrder("PSA:1", "1", null).id
        assertEquals("PSA:1|1|", id)
    }

    @Test
    fun `both version and order null produce a stable trailing-delimiter id`() {
        val id = CompositeEntityId.ByIdVersionAndOrder("PSA:1", null, null).id
        assertEquals("PSA:1||", id)
    }

    @Test
    fun `equality is based on the composite id`() {
        val a = CompositeEntityId.ByIdVersionAndOrder("PSA:1", "1", null)
        val b = CompositeEntityId.ByIdVersionAndOrder("PSA:1", "1", null)
        val c = CompositeEntityId.ByIdVersionAndOrder("PSA:1", "2", null)
        assertEquals(a, b)
        assertNotEquals(a, c)
    }
}
