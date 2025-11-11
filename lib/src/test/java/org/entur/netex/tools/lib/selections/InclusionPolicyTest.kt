package org.entur.netex.tools.lib.selections

import org.entur.netex.tools.lib.data.TestDataFactory
import org.entur.netex.tools.lib.model.Element
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Stack
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InclusionPolicyTest {

    private val skipElements = listOf("/Skip")

    private lateinit var inclusionPolicyWithSelections: InclusionPolicy
    private lateinit var inclusionPolicyWithoutSelections: InclusionPolicy

    private val selectedEntity = TestDataFactory.defaultEntity(id = "SelectedEntityId")
    private val nonSelectedEntity = TestDataFactory.defaultEntity(id = "NonSelectedEntityId")

    private val selectedRef = TestDataFactory.defaultRef(id = "SelectedRef")
    private val nonSelectedRef = TestDataFactory.defaultRef(id = "NonSelectedRef")

    @BeforeEach
    fun setUp() {
        inclusionPolicyWithoutSelections = InclusionPolicy(
            entitySelection = null,
            refSelection = null,
            skipElements = skipElements
        )

        val entitySelection = TestDataFactory.entitySelection(listOf(selectedEntity))
        val refSelection = RefSelection(selection = setOf(selectedRef))

        inclusionPolicyWithSelections = InclusionPolicy(
            entitySelection = entitySelection,
            refSelection = refSelection,
            skipElements = skipElements
        )
    }

    private fun createEntityElement(id: String) = TestDataFactory.defaultElement(name = "Entity", id = id, currentEntityId = id)
    private fun createRefElement(ref: String) = TestDataFactory.defaultElement(name = "Ref", ref = ref)

    private fun createSkippedElementStack(): Stack<Pair<Element, Boolean>> {
        val stack = Stack<Pair<Element, Boolean>>()
        stack.push(Pair(TestDataFactory.defaultElement(name = "Skip"), true))
        return stack
    }

    private fun createNonSkippedElementStack(): Stack<Pair<Element, Boolean>> {
        val stack = Stack<Pair<Element, Boolean>>()
        stack.push(Pair(TestDataFactory.defaultElement(name = "IncludedElement"), true))
        return stack
    }

    private fun createNonIncludedElementStack(): Stack<Pair<Element, Boolean>> {
        val stack = Stack<Pair<Element, Boolean>>()
        stack.push(Pair(TestDataFactory.defaultElement(name = "ExcludedElement"), false))
        return stack
    }

    @Test
    fun testShouldIncludeRefOnlyWhenRefIsInRefSelection() {
        val elementStack = createNonSkippedElementStack()

        val existingRef = createRefElement(ref = selectedRef.ref)
        assertTrue(inclusionPolicyWithSelections.shouldInclude(existingRef, elementStack))

        val missingRef = createRefElement(ref = nonSelectedRef.ref)
        assertFalse(inclusionPolicyWithSelections.shouldInclude(missingRef, elementStack))
    }

    @Test
    fun testShouldIncludeEntityOnlyWhenEntityIsInEntitySelection() {
        val elementStack = createNonSkippedElementStack()

        val existingEntity = createEntityElement(id = selectedEntity.id)
        assertTrue(inclusionPolicyWithSelections.shouldInclude(existingEntity, elementStack))

        val missingEntity = createEntityElement(id = nonSelectedEntity.id)
        assertFalse(inclusionPolicyWithSelections.shouldInclude(missingEntity, elementStack))
    }

    @Test
    fun testShouldIncludeElementWhenItsNotOnSkippedElementsPath() {
        val elementStack = createNonSkippedElementStack()
        val simpleElement = TestDataFactory.defaultElement(name = "TestElement")
        assertTrue(inclusionPolicyWithSelections.shouldInclude(simpleElement, elementStack))
        assertTrue(inclusionPolicyWithoutSelections.shouldInclude(simpleElement, elementStack))
    }

    @Test
    fun testShouldExcludeElementsOnSkippedElementsPaths() {
        val elementStack = createSkippedElementStack()
        val simpleElement = TestDataFactory.defaultElement(name = "Element")
        assertFalse(inclusionPolicyWithSelections.shouldInclude(simpleElement, elementStack))
        assertFalse(inclusionPolicyWithoutSelections.shouldInclude(simpleElement, elementStack))

        elementStack.push(simpleElement to true)
        val nestedElement = TestDataFactory.defaultElement(name = "NestedElement")
        assertFalse(inclusionPolicyWithSelections.shouldInclude(nestedElement, elementStack))
        assertFalse(inclusionPolicyWithoutSelections.shouldInclude(nestedElement, elementStack))
    }

    @Test
    fun testAlwaysIncludeRootElement() {
        val element = TestDataFactory.defaultElement(name = "TestElement")
        val stack = Stack<Pair<Element, Boolean>>()
        assertTrue(inclusionPolicyWithSelections.shouldInclude(element, stack))
        assertTrue(inclusionPolicyWithoutSelections.shouldInclude(element, stack))
    }

    @Test
    fun shouldExcludeElementsWithExcludedAncestor() {
        val element = TestDataFactory.defaultElement(name = "TestElement")
        val stack = createNonIncludedElementStack()
        assertFalse(inclusionPolicyWithSelections.shouldInclude(element, stack))
        assertFalse(inclusionPolicyWithoutSelections.shouldInclude(element, stack))
    }
}