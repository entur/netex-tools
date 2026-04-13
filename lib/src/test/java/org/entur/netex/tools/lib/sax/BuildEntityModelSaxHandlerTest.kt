package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.data.TestDataFactory
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.plugin.AbstractNetexPlugin
import org.entur.netex.tools.lib.plugin.TestNetexPlugin
import org.entur.netex.tools.lib.selections.InclusionPolicy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.xml.sax.Attributes
import java.io.File

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
            entitySelection = null,
            refSelection = null,
            skipElements = skipElements
        )

        buildEntityModelHandler = BuildEntityModelSaxHandler(
            entityModel = entityModel,
            plugins = listOf(testNetexPlugin),
            inclusionPolicy = inclusionPolicy,
            file = File("test.xml")
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

    // --- Scoped element registration tests ---

    @Test
    fun testUnscopedPluginDoesNotReceiveNestedCallbacks() {
        val attrs = TestDataFactory.defaultAttributes(id = "id")

        buildEntityModelHandler.startElement("", "", testNetexPlugin.supportedElementType, attrs)
        testNetexPlugin.reset()

        // Nested child — unscoped plugin should NOT be notified
        buildEntityModelHandler.startElement("", "", "NestedChild", TestDataFactory.defaultAttributes())
        Assertions.assertFalse(testNetexPlugin.hasCalledStartElement)

        val chars = "text".toCharArray()
        buildEntityModelHandler.characters(chars, 0, chars.size)
        Assertions.assertFalse(testNetexPlugin.hasCalledCharacters)

        buildEntityModelHandler.endElement("", "", "NestedChild")
        Assertions.assertFalse(testNetexPlugin.hasCalledEndElement)
    }

    @Test
    fun testScopedPluginReceivesEventsOnlyInsideAncestor() {
        val plugin = TrackingPlugin("ParentElement/ChildElement")

        val handler = BuildEntityModelSaxHandler(
            entityModel = TestDataFactory.defaultEntityModel(),
            inclusionPolicy = InclusionPolicy(entitySelection = null, refSelection = null, skipElements = emptyList()),
            file = File("test.xml"),
            plugins = listOf(plugin),
        )

        // ChildElement outside ParentElement — should NOT match
        handler.startElement("", "", "Other", TestDataFactory.defaultAttributes(id = "o1"))
        handler.startElement("", "", "ChildElement", TestDataFactory.defaultAttributes())
        handler.endElement("", "", "ChildElement")
        handler.endElement("", "", "Other")

        Assertions.assertTrue(plugin.events.isEmpty(), "Scoped plugin should not receive events outside ancestor")

        // ChildElement inside ParentElement — should match
        handler.startElement("", "", "ParentElement", TestDataFactory.defaultAttributes(id = "p1"))
        handler.startElement("", "", "ChildElement", TestDataFactory.defaultAttributes())
        val chars = "text".toCharArray()
        handler.characters(chars, 0, chars.size)
        handler.endElement("", "", "ChildElement")
        handler.endElement("", "", "ParentElement")

        Assertions.assertEquals(
            listOf("startElement:ChildElement", "characters:ChildElement", "endElement:ChildElement"),
            plugin.events
        )
    }

    @Test
    fun testScopedAndUnscopedPluginsCoexist() {
        val scopedPlugin = TrackingPlugin("ParentElement/SharedName")
        val unscopedPlugin = TrackingPlugin("SharedName")

        val handler = BuildEntityModelSaxHandler(
            entityModel = TestDataFactory.defaultEntityModel(),
            inclusionPolicy = InclusionPolicy(entitySelection = null, refSelection = null, skipElements = emptyList()),
            file = File("test.xml"),
            plugins = listOf(scopedPlugin, unscopedPlugin),
        )

        // SharedName outside ParentElement — only unscoped should match
        handler.startElement("", "", "Other", TestDataFactory.defaultAttributes(id = "o1"))
        handler.startElement("", "", "SharedName", TestDataFactory.defaultAttributes())
        handler.endElement("", "", "SharedName")
        handler.endElement("", "", "Other")

        Assertions.assertTrue(scopedPlugin.events.isEmpty())
        Assertions.assertEquals(
            listOf("startElement:SharedName", "endElement:SharedName"),
            unscopedPlugin.events
        )

        unscopedPlugin.events.clear()

        // SharedName inside ParentElement — both should match
        handler.startElement("", "", "ParentElement", TestDataFactory.defaultAttributes(id = "p1"))
        handler.startElement("", "", "SharedName", TestDataFactory.defaultAttributes())
        handler.endElement("", "", "SharedName")
        handler.endElement("", "", "ParentElement")

        Assertions.assertEquals(
            listOf("startElement:SharedName", "endElement:SharedName"),
            scopedPlugin.events
        )
        Assertions.assertEquals(
            listOf("startElement:SharedName", "endElement:SharedName"),
            unscopedPlugin.events
        )
    }

    @AfterEach
    fun tearDown() {
        testNetexPlugin.reset()
    }

    /** Test plugin that tracks all received events. */
    private class TrackingPlugin(vararg elementTypes: String) : AbstractNetexPlugin() {
        private val types = elementTypes.toSet()
        val events = mutableListOf<String>()

        override fun getName() = "Tracker"
        override fun getDescription() = "Tracks events for testing"
        override fun getSupportedElementTypes() = types

        override fun startElement(elementName: String, attributes: Attributes?, currentEntity: Entity?) {
            events.add("startElement:$elementName")
        }

        override fun characters(elementName: String, ch: CharArray?, start: Int, length: Int) {
            events.add("characters:$elementName")
        }

        override fun endElement(elementName: String, currentEntity: Entity?) {
            events.add("endElement:$elementName")
        }
    }

}