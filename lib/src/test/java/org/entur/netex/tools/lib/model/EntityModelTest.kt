package org.entur.netex.tools.lib.model

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class EntityModelTest {

    val eID = "e1"
    val fID = "f1"
    private val e = Entity(eID, "MyType", PublicationEnumeration.PUBLIC.value)
    private val f = Entity(fID, "Other", PublicationEnumeration.PUBLIC.value, e)

    private val subject = EntityModel(Alias(mutableMapOf(Pair("MyType", "MT"))))

    @BeforeEach
    fun addEntity() {
        subject.addEntity(f)
        subject.addEntity(e)
        subject.addRef("MyTypeRef", f, eID)
    }

    @Test
    fun getEntity() {
        assertEquals(e, subject.getEntity(eID))
        assertEquals(f, subject.getEntity(fID))
    }


    @Test
    fun forAllEntities() {
        subject.forAllEntities("MyType") {
            assertEquals(e, it)
        }
        subject.forAllEntities("Other") {
            assertEquals(f, it)
        }
    }

    @Test
    fun forAllReferences() {
        subject.forAllReferences("MyType") {
            fail("No refs of type MyType expected")
        }

        var hit = false
        subject.forAllReferences("Other") {
            hit = true
            assertEquals("MyTypeRef", it.type)
            assertEquals(eID, it.ref)
            assertEquals(f, it.source)
        }
        assertTrue(hit)
    }

    @Test
    fun listAllRefs() {
        val ref = subject.listAllRefs().get(0)
        assertEquals("MyTypeRef", ref.type)
        assertEquals(eID, ref.ref)
        assertEquals(f, ref.source)

        assertEquals(1, subject.listAllRefs().size)
    }

    @Test
    fun printReports() {
        // This is just making sure the report does not fail, it does not test anything
        val selection = EntitySelection(mutableMapOf())
        subject.printEntities(selection)
        subject.printReferences(selection)
    }
}