package org.entur.netex.tools.lib.selections

import org.entur.netex.tools.lib.data.TestDataFactory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InclusionPolicyTest {

    private val skipElements = listOf("/some/path/to/skip", "/another/path/to/skip")
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
        val element = TestDataFactory.defaultElement(name = "TestElement", id = "element-id")
        assertTrue { inclusionPolicy.shouldInclude(element, null) }
    }

    @Test
    fun testShouldIncludeElementIfElementDoesNotHaveACurrentEntity() {
        val element = TestDataFactory.defaultElement(name = "TestElement")
        assertTrue { inclusionPolicy.shouldInclude(element, TestDataFactory.entitySelection(setOf())) }
    }

    @Test
    fun testShouldNotIncludeElementIfCurrentEntityIsNotInEntitySelection() {
        val element = TestDataFactory.elementWithParentEntity(name = "TestElement", currentEntityId = "nonExistingId")
        assertFalse {
            inclusionPolicy.shouldInclude(
                element,
                TestDataFactory.entitySelection(
                    setOf(TestDataFactory.defaultEntity("existingId"))
                )
            )
        }
    }

    @Test
    fun testShouldIncludeElementIfElementIsNull() {
        assertTrue { inclusionPolicy.shouldInclude(null, "/some/path") }
    }

    @Test
    fun testShouldNotIncludeElementIfCurrentPathStartsWithSkipElement() {
        val element = TestDataFactory.defaultElement(name = "TestElement")
        assertFalse { inclusionPolicy.shouldInclude(element, "/some/path/to/skip/A") }
    }

    @Test
    fun testShouldNotIncludeElementIfCurrentPathShouldBeSkipped() {
        val element = TestDataFactory.defaultElement(name = "TestElement")
        val entityElement = TestDataFactory.defaultElement(name = "TestElement", id = "entityId")
        val refElement = TestDataFactory.defaultElement(name = "TestElement", ref = "referredId")
        for (path in skipElements) {
            assertFalse { inclusionPolicy.shouldInclude(element, path) }
            assertFalse { inclusionPolicy.shouldInclude(entityElement, path) }
            assertFalse { inclusionPolicy.shouldInclude(refElement, path) }
        }
    }
}