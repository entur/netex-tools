package org.entur.netex.tools.lib.model

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class EntitySelectionTest {
    companion object {
        const val PARENT_TYPE = "ParentType"
        const val CHILD_TYPE = "ChildType"
    }

    val eID = "e"
    val eaID = "e:a"
    val ebID = "e:b"
    val eaaID = "e:a:a"

    private val e = Entity(eID, PARENT_TYPE)
    private val ea = Entity(eaID, CHILD_TYPE, e)
    private val eb = Entity(ebID, CHILD_TYPE, e)
    private val eaa = Entity(eaaID, CHILD_TYPE, ea)

    private val alias = Alias(mutableMapOf())
    private val model = EntityModel(alias)


    @BeforeEach
    fun setup() {
        listOf(e, ea, eb, eaa).forEach { model.addEntity(it) }
        model.addRef("Ref", ea, eb.id)
        model.addRef("Ref", eb, e.id)
    }

    @Test
    fun testIsSelect() {
        with(EntitySelection(model)) {
            // Initially nothing is selected
            assertSelected()

            // Select one
            select(ea)
            assertSelected(ea)

            // Select another
            select(e)
            assertSelected(e, ea)
        }
    }
    @Test
    fun testSelectIds() {
        with(EntitySelection(model)) {
            // Initially nothing is selected
            assertSelected()

            // Select e, but not ea[wrong type] -> An error is printed, but not aborted
            select(PARENT_TYPE,  arrayOf(e.id, ea.id))
            assertSelected(e)

            // Select multiple
            select(CHILD_TYPE,  arrayOf(eb.id, eaa.id))
            assertSelected(e, eb, eaa)
        }
    }



    @Test
    fun testSelectTypeWithParentRelationShip() {
        listOf(
            SelectTypeTC(ea, CHILD_TYPE, "UnknownType", listOf()),
            SelectTypeTC(ea, CHILD_TYPE, PARENT_TYPE, listOf()),
            SelectTypeTC(ea, CHILD_TYPE, CHILD_TYPE, listOf(eaa)),
            SelectTypeTC(e, CHILD_TYPE, PARENT_TYPE, listOf(ea, eb))
        ).forEach {
            with(EntitySelection(model)) {
                select(it.e)
                selectType(it.fromType).ifParentSelected(it.toType)
                assertSelected(it.expected())
            }
        }
    }

    @Test
    fun testSelectTypeWithChildRelationShip() {
        listOf(
            SelectTypeTC(ea, CHILD_TYPE, "UnknownType", listOf()),
            SelectTypeTC(ea, PARENT_TYPE, CHILD_TYPE, listOf(e)),
            SelectTypeTC(ea, CHILD_TYPE, CHILD_TYPE, listOf()),
            SelectTypeTC(eaa, CHILD_TYPE, CHILD_TYPE, listOf(ea))
        ).forEach {
            with(EntitySelection(model)) {
                select(it.e)
                selectType(it.fromType).ifChildSelected(it.toType)
                assertSelected(it.expected())
            }
        }
    }

    @Test
    fun selectAllParents() {
        listOf(
            Pair(e, listOf()),
            Pair(ea, listOf(e)),
            Pair(eaa, listOf(e, ea))
        ).forEach {
            with(EntitySelection(model)) {
                select(it.first)
                selectAllParents()
                assertSelected(it.second + it.first)
            }
        }
    }

    @Test
    fun testSelectTypeWithRefTargetRelationShip() {
        // Refs: ea -> eb -> e
        listOf(
            SelectTypeTC(ea, CHILD_TYPE, "UnknownType", listOf()),
            SelectTypeTC(e, CHILD_TYPE, PARENT_TYPE, listOf(eb)),
            SelectTypeTC(eb, CHILD_TYPE, CHILD_TYPE, listOf(ea)),
            SelectTypeTC(ea, CHILD_TYPE, CHILD_TYPE, listOf())
        ).forEach {
            with(EntitySelection(model)) {
                select(it.e)
                selectType(it.fromType).ifRefTargetSelected(it.toType)
                assertSelected(it.expected())
            }
        }
    }

    @Test
    fun testSelectTypeWithRefSourceRelationShip() {
        // Refs: ea -> eb -> e
        listOf(
            SelectTypeTC(ea, CHILD_TYPE, "UnknownType", listOf()),
            SelectTypeTC(eb, PARENT_TYPE, CHILD_TYPE, listOf(e)),
            SelectTypeTC(ea, CHILD_TYPE, CHILD_TYPE, listOf(eb)),
            SelectTypeTC(eb, CHILD_TYPE, CHILD_TYPE, listOf())
        ).forEach {
            with(EntitySelection(model)) {
                select(it.e)
                selectType(it.fromType).ifRefSourceSelected(it.toType)
                assertSelected(it.expected())
            }
        }
    }

    @Test
    fun selectAllReferencedEntities() {
        // Refs: ea -> eb -> e

        listOf(
            Pair(e, listOf()),
            Pair(ea, listOf(eb, e)),
            Pair(eb, listOf(e)),
            Pair(eaa, listOf())
        ).forEach {
            with(EntitySelection(model)) {
                select(it.first)
                selectAllReferencedEntities()
                assertSelected(it.second + it.first)
            }
        }
    }

    private fun EntitySelection.assertSelected(vararg list : Entity) {
        assertSelected(list.asList())
    }

    private fun EntitySelection.assertSelected(list : List<Entity>) {
        val expected = list.map { it.id }.sorted()
        val selected = setOf(e, ea, eb, eaa).filter { isSelected(it) }.map { it.id }.sorted()
        assertEquals(expected, selected)
    }

    private data class SelectTypeTC(
        val e : Entity,
        val fromType : String,
        val toType :String,
        private val expected : List<Entity>
    ) {
        fun expected() = expected + e
    }
}