package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.data.TestDataFactory
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.plugin.TestNetexPlugin
import org.entur.netex.tools.lib.selections.InclusionPolicy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BuildEntityModelSaxHandlerTest {

    private lateinit var buildEntityModelHandler: BuildEntityModelSaxHandler
    private lateinit var entityModel: EntityModel

    private val testNetexPlugin = TestNetexPlugin()

    private val skipElements = listOf("/element1/element2")

    private fun setUpSkippingState() {
        val skippedElement = TestDataFactory.defaultElement(name = "element2")
        val attrs = TestDataFactory.defaultAttributes(id = "id2")

        buildEntityModelHandler.startElement(
            uri = "",
            localName = "element1",
            qName = "element1",
            attributes = TestDataFactory.defaultAttributes()
        )
        buildEntityModelHandler.startElement(
            uri = "",
            localName = skippedElement.name,
            qName = skippedElement.name,
            attributes = attrs
        )
    }

    @BeforeEach
    fun setUp() {
        entityModel = TestDataFactory.defaultEntityModel()

        val inclusionPolicy = InclusionPolicy(
            entityModel = entityModel,
            entitySelection = null,
            refSelection = null,
            skipElements = skipElements
        )

        buildEntityModelHandler = BuildEntityModelSaxHandler(
            entityModel = entityModel,
            inclusionPolicy = inclusionPolicy,
            plugins = listOf(testNetexPlugin)
        )
    }

    @Test
    fun testStartElementShouldRegisterEntityWhenTypeShouldNotBeSkipped() {
        val entityElement = TestDataFactory.defaultElement(name = "element3")
        val attrs = TestDataFactory.defaultAttributes(id = "id3")

        buildEntityModelHandler.startElement(
            uri = "",
            localName = entityElement.name,
            qName = entityElement.name,
            attributes = attrs
        )

        Assertions.assertNotNull(entityModel.getEntity("id3"))
    }

    @Test
    fun testStartElementShouldRegisterRefWhenTypeShouldNotBeSkipped() {
        val entityElement = TestDataFactory.defaultElement(name = "entityElement", id = "entityId")
        val entityAttrs = TestDataFactory.defaultAttributes(id = "entityId")
        buildEntityModelHandler.startElement(
            uri = "",
            localName = entityElement.name,
            qName = entityElement.name,
            attributes = entityAttrs
        )

        val refElement = TestDataFactory.defaultElement(name = "refElement", ref = "referredEntity", currentEntityId = "entityId")
        val refAttrs = TestDataFactory.defaultAttributes(ref = "referredEntity")
        buildEntityModelHandler.startElement(
            uri = "",
            localName = refElement.name,
            qName = refElement.name,
            attributes = refAttrs
        )

        Assertions.assertNotNull(entityModel.getRef(refElement))
    }

    @Test
    fun testStartElementShouldSkipElementWhenPathIsInSkipElements() {
        setUpSkippingState()
        Assertions.assertNull(entityModel.getEntity("someId"))
        Assertions.assertEquals(skipElements[0], buildEntityModelHandler.elementBeingSkipped)
    }

    @Test
    fun testEndElementShouldStopSkippingWhenEndOfSkippedElementIsReached() {
        setUpSkippingState()
        Assertions.assertEquals(skipElements[0], buildEntityModelHandler.elementBeingSkipped)

        buildEntityModelHandler.endElement(
            uri = "",
            localName = "element2",
            qName = "element2"
        )
        Assertions.assertNull(buildEntityModelHandler.elementBeingSkipped)
    }

    @Test
    fun testStartElementShouldCallPluginsForNonSkippedElements() {
        val entityElement = TestDataFactory.defaultElement(name = testNetexPlugin.supportedElementType)
        val attrs = TestDataFactory.defaultAttributes(id = "id")

        buildEntityModelHandler.startElement(
            uri = "",
            localName = entityElement.name,
            qName = entityElement.name,
            attributes = attrs
        )

        Assertions.assertTrue(testNetexPlugin.hasCalledStartElement)
    }

    @Test
    fun testStartElementShouldNotCallPluginsForSkippedElements() {
        setUpSkippingState()
        val attrs = TestDataFactory.defaultAttributes(id = "id")
        val entityElement = TestDataFactory.defaultElement(name = testNetexPlugin.supportedElementType)
        buildEntityModelHandler.startElement(
            uri = "",
            localName = entityElement.name,
            qName = entityElement.name,
            attributes = attrs
        )
        Assertions.assertFalse(testNetexPlugin.hasCalledStartElement)
    }

    @Test
    fun testCharactersShouldCallPluginsForNonSkippedElements() {
        val entityElement = TestDataFactory.defaultElement(name = testNetexPlugin.supportedElementType)
        val attrs = TestDataFactory.defaultAttributes(id = "id")

        buildEntityModelHandler.startElement(
            uri = "",
            localName = entityElement.name,
            qName = entityElement.name,
            attributes = attrs
        )

        val chars = "some characters".toCharArray()
        buildEntityModelHandler.characters(
            ch = chars,
            start = 0,
            length = chars.size
        )

        Assertions.assertTrue(testNetexPlugin.hasCalledCharacters)
    }

    @Test
    fun testCharactersShouldNotCallPluginsForSkippedElements() {
        setUpSkippingState()
        val entityElement = TestDataFactory.defaultElement(name = testNetexPlugin.supportedElementType)
        val attrs = TestDataFactory.defaultAttributes(id = "id")
        buildEntityModelHandler.startElement(
            uri = "",
            localName = entityElement.name,
            qName = entityElement.name,
            attributes = attrs
        )

        val chars = "some characters".toCharArray()
        buildEntityModelHandler.characters(
            ch = chars,
            start = 0,
            length = chars.size
        )

        Assertions.assertFalse(testNetexPlugin.hasCalledCharacters)
    }

    @Test
    fun testEndElementShouldCallPluginsForNonSkippedElements() {
        val entityElement = TestDataFactory.defaultElement(name = testNetexPlugin.supportedElementType)
        val attrs = TestDataFactory.defaultAttributes(id = "id")

        buildEntityModelHandler.startElement(
            uri = "",
            localName = entityElement.name,
            qName = entityElement.name,
            attributes = attrs
        )

        buildEntityModelHandler.endElement(
            uri = "",
            localName = entityElement.name,
            qName = entityElement.name
        )

        Assertions.assertTrue(testNetexPlugin.hasCalledEndElement)
    }

    @Test
    fun testEndElementShouldNotCallPluginsForSkippedElements() {
        setUpSkippingState()
        val entityElement = TestDataFactory.defaultElement(name = testNetexPlugin.supportedElementType)
        val attrs = TestDataFactory.defaultAttributes(id = "id")
        buildEntityModelHandler.startElement(
            uri = "",
            localName = entityElement.name,
            qName = entityElement.name,
            attributes = attrs
        )

        buildEntityModelHandler.endElement(
            uri = "",
            localName = entityElement.name,
            qName = entityElement.name
        )

        Assertions.assertFalse(testNetexPlugin.hasCalledEndElement)
    }

    @AfterEach
    fun tearDown() {
        testNetexPlugin.reset()
    }

}