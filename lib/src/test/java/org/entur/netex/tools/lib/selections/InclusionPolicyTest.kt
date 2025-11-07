package org.entur.netex.tools.lib.selections

import org.entur.netex.tools.lib.data.TestDataFactory
import org.entur.netex.tools.lib.model.Element
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Stack
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InclusionPolicyTest {

    private val skipElements = listOf("/some/path/to/skip", "/another/path/to/skip", "/TestElement")
    private lateinit var inclusionPolicy: InclusionPolicy

    @BeforeEach
    fun setUp() {
        inclusionPolicy = InclusionPolicy(
            entityModel = TestDataFactory.defaultEntityModel(),
            entitySelection = null,
            refSelection = null,
            skipElements = skipElements
        )
    }

    @Test
    fun testShouldIncludeRefOnlyWhenRefIsInRefSelection() {
        val existingRef = TestDataFactory.defaultRef("existing-entity")
        val missingRef = TestDataFactory.defaultRef("missing-entity")
        val refSelection = RefSelection(setOf(existingRef))
        assertTrue { inclusionPolicy.shouldInclude(existingRef, refSelection) }
        assertFalse { inclusionPolicy.shouldInclude(missingRef, refSelection) }
    }

    @Test
    fun testShouldIncludeEntityOnlyWhenEntityIsInEntitySelection() {
        val existingEntity = TestDataFactory.defaultEntity("existing-entity")
        val missingEntity = TestDataFactory.defaultEntity("missing-entity")
        val entitySelection = TestDataFactory.entitySelection(setOf(existingEntity))
        assertTrue { inclusionPolicy.shouldInclude(existingEntity, entitySelection) }
        assertFalse { inclusionPolicy.shouldInclude(missingEntity, entitySelection) }
    }

    @Test
    fun testShouldIncludeElementIfNoEntitySelectionIsProvided() {
        val elementStack = Stack<Element>()
        elementStack.push(TestDataFactory.defaultElement(name = "NonSkipTestElement", id = "element-id"))
        assertTrue { inclusionPolicy.shouldInclude(elementStack) }
    }


    @Test
    fun testShouldNotIncludeElementIfCurrentEntityIsNotInEntitySelection() {
        val elementStack = Stack<Element>()
        val entity = TestDataFactory.defaultElement(name = "TestEntity", id = "nonExistingId")
        val element = TestDataFactory.elementWithParentEntity(name = "TestElement", currentEntityId = "nonExistingId")
        elementStack.push(entity)
        elementStack.push(element)
        assertFalse {
            inclusionPolicy.shouldInclude(
                elementStack,
                TestDataFactory.entitySelection(
                    setOf(TestDataFactory.defaultEntity("existingId"))
                )
            )
        }
    }

    @Test
    fun testShouldIncludeElementIfStackIsEmpty() {
        val elementStack = Stack<Element>()
        assertTrue { inclusionPolicy.shouldInclude(elementStack) }
    }

    @Test
    fun testShouldNotIncludeElementIfCurrentPathStartsWithSkipElement() {
        val elementStack = Stack<Element>()
        val element = TestDataFactory.defaultElement(name = "TestElement")
        elementStack.push(element)
        assertFalse { inclusionPolicy.shouldInclude(elementStack) }
    }

    @Test
    fun testShouldNotIncludeElementIfCurrentPathShouldBeSkipped() {
        val element = TestDataFactory.defaultElement(name = "TestElement")
        val stack1 = Stack<Element>()
        stack1.push(element)
        assertFalse { inclusionPolicy.shouldInclude(stack1) }

        val entityElement = TestDataFactory.defaultElement(name = "TestElement", id = "entityId")
        val stack2 = Stack<Element>()
        stack2.push(entityElement)
        assertFalse { inclusionPolicy.shouldInclude(stack2) }

        val refElement = TestDataFactory.defaultElement(name = "TestElement", ref = "referredId")
        val stack3 = Stack<Element>()
        stack3.push(refElement)
        assertFalse { inclusionPolicy.shouldInclude(stack3) }
    }
}