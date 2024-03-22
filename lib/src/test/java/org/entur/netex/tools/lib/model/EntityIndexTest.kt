package org.entur.netex.tools.lib.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EntityIndexTest {

    private val eV0 = Entity("e1", "Type")
    private val eV1 = Entity("e1", "TypeV2")
    private val f = Entity("f1", "Other")

    private var subject = EntityIndex()

    @BeforeEach
    fun setup() {
        subject.add(eV0)
        subject.add(eV1)
        subject.add(f)
    }

    @Test
    fun addAndGet() {
        // The first element is added to the collection,
        // adding the same element twice does nothing
        assertEquals(f, subject.get(f.id))
        assertEquals(eV0, subject.get(eV1.id))
    }

    @Test
    fun list() {
        assertEquals(listOf(eV0), subject.list("Type"))
        assertEquals(listOf(f), subject.list("Other"))
        assertEquals(listOf<Entity>(), subject.list("TypeV2"))
    }

    @Test
    fun listAll() {
        assertEquals(setOf(f, eV0), subject.listAll().toSet())
    }
}