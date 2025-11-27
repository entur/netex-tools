package org.entur.netex.tools.lib.output

import org.entur.netex.tools.lib.data.TestDataFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.xml.sax.ContentHandler
import org.xml.sax.helpers.AttributesImpl
import java.io.File

class DelegatingXMLElementWriterTest {
    val defaultHandler = mock<ContentHandler>()
    val context = XmlContext(
        xmlFile = File("test.xml"),
        xmlWriter = defaultHandler,
    )

    val uri = "uri"
    val localName = "localName"
    val qName = "qName"
    val attrs = AttributesImpl()
    val defaultPath = "/Path"
    val customPath = "/Custom"
    val ch = "Content".toCharArray()
    val length = ch.size

    val customHandler = TestXMLElementHandler()
    val writer = DelegatingXMLElementWriter(
        xmlContext = context,
        handlers = mapOf(
            customPath to customHandler
        )
    )

    @Test
    fun testElementHandler() {
        val handler = writer.elementHandler("/Custom")
        assertEquals(customHandler, handler)
        val unknownHandler = writer.elementHandler("/Unknown")
        assertNull(unknownHandler)
    }

    @Test
    fun testStartElement() {
        writer.startElement(uri, localName, qName, attrs)
        verify(defaultHandler).startElement(uri, localName, qName, attrs)
    }

    @Test
    fun testCharacters() {
        writer.characters(ch, 0, length)
        verify(defaultHandler).characters(ch, 0, length)
    }

    @Test
    fun testEndElement() {
        writer.endElement(uri, localName, qName)
        verify(defaultHandler).endElement(uri, localName, qName)
    }

    @Test
    fun testHandleStartElementCallsDefaultHandlerByDefault() {
        writer.handleStartElement(uri, localName, qName, attrs, defaultPath)
        verify(defaultHandler).startElement(uri, localName, qName, attrs)
    }

    @Test
    fun testHandleStartElementCallsCustomHandlerWhenPathsMatch() {
        writer.handleStartElement(uri, localName, qName, attrs, customPath)
        assertTrue { customHandler.hasCalledStartElement }
        verifyNoInteractions(defaultHandler)
    }

    @Test
    fun testHandleCharactersCallsCustomHandlerWhenPathsMatch() {
        writer.handleCharacters(ch, 0, length, customPath)
        assertTrue { customHandler.hasCalledCharacters }
        verifyNoInteractions(defaultHandler)
    }

    @Test
    fun testHandleEndElementCallsCustomHandlerWhenPathsMatch() {
        writer.handleEndElement(uri, localName, qName, customPath)
        assertTrue { customHandler.hasCalledEndElement }
        verifyNoInteractions(defaultHandler)
    }

    @Test
    fun testWritePicksHandlingByEventType() {
        val startElementEvent = TestDataFactory.startElementEventRecord().event as StartElement
        writer.write(TestDataFactory.startElementEventRecord())
        verify(defaultHandler).startElement(
            eq(startElementEvent.uri),
            eq(startElementEvent.localName),
            eq(startElementEvent.qName),
            check {
                assertEquals(it.length, startElementEvent.attributes?.size)
            }
        )

        val charactersEvent = TestDataFactory.charactersEventRecord().event as Characters
        writer.write(TestDataFactory.charactersEventRecord())
        verify(defaultHandler).characters(
            charactersEvent.ch,
            charactersEvent.start,
            charactersEvent.length,
        )

        val endElementEvent = TestDataFactory.endElementEventRecord().event as EndElement
        writer.write(TestDataFactory.endElementEventRecord())
        verify(defaultHandler).endElement(
            eq(endElementEvent.uri),
            eq(endElementEvent.localName),
            eq(endElementEvent.qName),
        )
    }

    @AfterEach
    fun tearDown() {
        customHandler.reset()
        reset(defaultHandler)
    }
}