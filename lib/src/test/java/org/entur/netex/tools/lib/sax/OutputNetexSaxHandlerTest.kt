package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.data.TestDataFactory
import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.entur.netex.tools.lib.output.NetexFileWriter
import org.entur.netex.tools.lib.report.FileIndex
import org.entur.netex.tools.lib.selections.InclusionPolicy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.kotlin.verify
import org.xml.sax.helpers.AttributesImpl
import java.io.File

class OutputNetexSaxHandlerTest {

    private val inclusionPolicy = InclusionPolicy(
        entitySelection = null,
        refSelection = null,
        skipElements = listOf(
            "/PublicationDelivery/dataObjects/VehicleScheduleFrame"
        )
    )

    private lateinit var outputNetexSaxHandler: OutputNetexSaxHandler

    private val fileIndex = FileIndex()
    private val fileWriter = mock<NetexFileWriter>()
    private val delegatingXmlElementWriter = mock<DelegatingXMLElementWriter>()
    private val testFile = File("test.xml")
    private val defaultAttributes = AttributesImpl()

    @BeforeEach
    fun setUp() {
        outputNetexSaxHandler = OutputNetexSaxHandler(
            entityModel = TestDataFactory.defaultEntityModel(),
            fileIndex = fileIndex,
            inclusionPolicy = inclusionPolicy,
            fileWriter = fileWriter,
            outputFile = testFile,
            elementWriter = delegatingXmlElementWriter,
        )
    }


    @Test
    fun startDocument() {
        outputNetexSaxHandler.startDocument()
        verify(fileWriter).startDocument()
    }

    @Test
    fun endDocument() {
        outputNetexSaxHandler.endDocument()
        verify(fileWriter).endDocument()
    }

    @Test
    fun handlerWritesElementsThatShouldNotBeSkipped() {
        outputNetexSaxHandler.startElement("", "", "PublicationDelivery", defaultAttributes)
        outputNetexSaxHandler.startElement("", "", "dataObjects", defaultAttributes)
        outputNetexSaxHandler.comment("".toCharArray(), 0, 0)

        verify(delegatingXmlElementWriter).handleStartElement(
            uri = "",
            localName = "",
            qName = "PublicationDelivery",
            attributes = defaultAttributes,
            currentPath = "/PublicationDelivery"
        )
        verify(delegatingXmlElementWriter).handleStartElement(
            uri = "",
            localName = "",
            qName = "dataObjects",
            attributes = defaultAttributes,
            currentPath = "/PublicationDelivery/dataObjects"
        )
        verify(fileWriter).writeComments("".toCharArray(), 0, 0)

        outputNetexSaxHandler.characters("".toCharArray(), 0, 0)
        verify(delegatingXmlElementWriter).handleCharacters(
            ch = "".toCharArray(),
            start = 0,
            length = 0,
            currentPath = "/PublicationDelivery/dataObjects"
        )

        outputNetexSaxHandler.endElement("", "", "dataObjects")
        outputNetexSaxHandler.endElement("", "", "PublicationDelivery")
        verify(delegatingXmlElementWriter).handleEndElement(
            "",
            "",
            "dataObjects",
            currentPath = "/PublicationDelivery/dataObjects"
        )
        verify(delegatingXmlElementWriter).handleEndElement(
            "",
            "",
            "PublicationDelivery",
            currentPath = "/PublicationDelivery"
        )
    }

    @Test
    fun handlerDoesNotWriteElementsThatShouldBeSkipped() {
        outputNetexSaxHandler.startElement("", "", "PublicationDelivery", defaultAttributes)
        outputNetexSaxHandler.startElement("", "", "dataObjects", defaultAttributes)

        // Verifies that VehicleScheduleFrame starting tag and characters are not written
        outputNetexSaxHandler.startElement("", "", "VehicleScheduleFrame", defaultAttributes)
        outputNetexSaxHandler.characters("".toCharArray(), 0, 0)
        verify(delegatingXmlElementWriter, never()).handleStartElement(
            uri = "",
            localName = "",
            qName = "VehicleScheduleFrame",
            attributes = defaultAttributes,
            currentPath = "/PublicationDelivery/dataObjects/VehicleScheduleFrame"
        )
        verify(delegatingXmlElementWriter, never()).characters("".toCharArray(), 0, 0)

        // Verifies that Block starting tag and characters are not written
        outputNetexSaxHandler.startElement("", "", "Block", defaultAttributes)
        outputNetexSaxHandler.characters("".toCharArray(), 0, 0)
        verify(delegatingXmlElementWriter, never()).handleStartElement(
            uri = "",
            localName = "",
            qName = "Block",
            attributes = defaultAttributes,
            currentPath = "/PublicationDelivery/dataObjects/VehicleScheduleFrame/Block"
        )
        verify(delegatingXmlElementWriter, never()).handleCharacters("".toCharArray(), 0, 0, "/PublicationDelivery/dataObjects/VehicleScheduleFrame/Block")

        // Verifies that comments in a Block tag are not written
        outputNetexSaxHandler.comment("".toCharArray(), 0, 0)
        verify(fileWriter, never()).writeComments("".toCharArray(), 0, 0)

        // Verifies that Block end tag is not written
        outputNetexSaxHandler.endElement("", "", "Block")
        verify(delegatingXmlElementWriter, never()).handleEndElement(
            uri = "",
            localName = "",
            qName = "Block",
            currentPath = "/PublicationDelivery/dataObjects/VehicleScheduleFrame/Block"
        )

        // Verifies that Blocks are not written if there are Block siblings
        outputNetexSaxHandler.startElement("", "", "Block", defaultAttributes)
        outputNetexSaxHandler.characters("".toCharArray(), 0, 0)
        outputNetexSaxHandler.endElement("", "", "Block")
        verify(delegatingXmlElementWriter, never()).handleStartElement(
            uri = "",
            localName = "",
            qName = "Block",
            attributes = defaultAttributes,
            currentPath = "/PublicationDelivery/dataObjects/VehicleScheduleFrame/Block"
        )
        verify(delegatingXmlElementWriter, never()).handleCharacters("".toCharArray(), 0, 0, "/PublicationDelivery/dataObjects/VehicleScheduleFrame/Block")
        verify(delegatingXmlElementWriter, never()).handleEndElement(
            uri = "",
            localName = "",
            qName = "Block",
            currentPath = "/PublicationDelivery/dataObjects/VehicleScheduleFrame/Block"
        )

        // Verifies that VehicleScheduleFrame end tag is not written
        outputNetexSaxHandler.endElement("", "", "VehicleScheduleFrame")
        verify(delegatingXmlElementWriter, never()).handleEndElement(
            uri = "",
            localName = "",
            qName = "VehicleScheduleFrame",
            currentPath = "/PublicationDelivery/dataObjects/VehicleScheduleFrame"
        )
    }
}