package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.extensions.toAttributes
import org.entur.netex.tools.lib.output.Characters
import org.entur.netex.tools.lib.output.Comments
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.xml.sax.helpers.AttributesImpl

class NetexToolsSaxHandlerTest {

    lateinit var handler: NetexToolsSaxHandler

    @BeforeEach
    fun init() {
        handler = object : NetexToolsSaxHandler() {}

    }

    @Test
    fun testHandlerStateIsUpdatedCorrectlyByStartElement() {
        handler.startElement("", "", "Test1", AttributesImpl())
        handler.startElement("", "", "Test2", mapOf<String, String>(
            "id" to "testId",
        ).toAttributes())

        val elementStack = handler.elementStack
        assertEquals(2, elementStack.size)
        assertTrue(elementStack.any({ it.name == "Test1" }))
        assertTrue(elementStack.any({ it.name == "Test2" }))

        val entityStack = handler.entityStack
        assertEquals(1, entityStack.size)
        assertTrue(entityStack.any({ it.type == "Test2" }))

        assertEquals("Test2", handler.currentEventRecord?.element?.name)
    }

    @Test
    fun testHandlerStateIsUpdatedCorrectlyByCharacters() {
        handler.startElement("", "", "Test1", AttributesImpl())
        val ch = "Content".toCharArray()
        handler.characters(ch, 0, ch.size)
        assertTrue(handler.currentEventRecord?.event is Characters)
    }

    @Test
    fun testHandlerStateIsUpdatedCorrectlyByComments() {
        handler.startElement("", "", "Test1", AttributesImpl())
        val ch = "Comment".toCharArray()
        handler.comments(ch, 0, ch.size)
        assertTrue(handler.currentEventRecord?.event is Comments)
    }

    @Test
    fun testHandlerStateIsUpdatedCorrectlyByEndElement() {
        handler.startElement("", "", "Test1", AttributesImpl())
        handler.startElement("", "", "Test2", mapOf<String, String>(
            "id" to "testId",
        ).toAttributes())

        assertEquals(1, handler.entityStack.size)
        assertEquals(2, handler.elementStack.size)

        handler.endElement("", "", "Test2")
        assertEquals(0, handler.entityStack.size)
        assertEquals(1, handler.elementStack.size)

        handler.endElement("", "", "Test1")
        assertEquals(0, handler.elementStack.size)
    }
}