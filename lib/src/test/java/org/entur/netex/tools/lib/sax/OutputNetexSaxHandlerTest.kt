package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.data.TestDataFactory
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.entur.netex.tools.lib.output.NetexFileWriter
import org.entur.netex.tools.lib.report.FileIndex
import org.entur.netex.tools.lib.selections.InclusionPolicy
import org.entur.netex.tools.lib.selections.RefSelection
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.kotlin.verify
import org.xml.sax.helpers.AttributesImpl
import java.io.File

class OutputNetexSaxHandlerTest {
    private val entityModel = TestDataFactory.defaultEntityModel()
    private val entitySelection = TestDataFactory.entitySelection(entityModel.listAllEntities())
    private val refSelection = RefSelection(setOf())

    private val fileIndex = FileIndex()
    private val inclusionPolicy = InclusionPolicy(
        entitySelection = entitySelection,
        refSelection = refSelection,
        skipElements = listOf(
            "/PublicationDelivery/dataObjects/CompositeFrame/frames/VehicleScheduleFrame",
            "/PublicationDelivery/dataObjects/CompositeFrame/frames/ServiceFrame/lines/Line/routes",
            "/PublicationDelivery/dataObjects/CompositeFrame/frames/TimetableFrame/vehicleJourneys/DeadRun",
            "/Parent/Child"
        )
    )

    private val testFile = File("test.xml")

    private val fileWriter = mock<NetexFileWriter>()
    private val delegatingXmlElementWriter = mock<DelegatingXMLElementWriter>()

    private lateinit var outputNetexSaxHandler: OutputNetexSaxHandler

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

    private val serviceJourneyId = "service_journey_id"
    private val serviceJourneyEntity = TestDataFactory.defaultEntity(id = serviceJourneyId, type = "ServiceJourney")
    private val blockId = "block_id"
    private val blockEntity = TestDataFactory.defaultEntity(id = blockId, type = "Block")

    init {
        entityModel.addEntity(serviceJourneyEntity)
        entityModel.addEntity(blockEntity)

        entitySelection.selection["ServiceJourney"] = mutableMapOf(serviceJourneyId to serviceJourneyEntity)
        entitySelection.allIds.add(serviceJourneyId)
    }

    private fun getAttributesForEntity(entity: Entity): AttributesImpl {
        val attrs = AttributesImpl()
        attrs.addAttribute("", "id", "id", "CDATA", entity.id)
        return attrs
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
    fun startElementDoesNotWriteTagIfElementShouldBeSkipped() {
        val blockAttrs = getAttributesForEntity(blockEntity)
        outputNetexSaxHandler.startElement("", "", "Block", blockAttrs)
        verify(delegatingXmlElementWriter, never()).handleStartElement(
            uri = "",
            localName = "",
            qName = "Block",
            attributes = blockAttrs,
            currentPath = "/Block"
        )

        // Verifies that children of skipped elements are also skipped, regardless of selection
        val serviceJourneyAttrs = AttributesImpl()
        serviceJourneyAttrs.addAttribute("", "id", "id", "CDATA", serviceJourneyId)
        outputNetexSaxHandler.startElement("", "", "ServiceJourney", serviceJourneyAttrs)
        verify(delegatingXmlElementWriter, never()).handleStartElement(
            uri = "",
            localName = "",
            qName = "ServiceJourney",
            attributes = serviceJourneyAttrs,
            currentPath = "/Block/ServiceJourney"
        )
    }

    @Test
    fun startElementWritesTagIfElementShouldBeIncluded() {
        val serviceJourneyAttrs = getAttributesForEntity(serviceJourneyEntity)
        outputNetexSaxHandler.startElement("", "", "ServiceJourney", serviceJourneyAttrs)
        verify(delegatingXmlElementWriter).handleStartElement(
            uri = "",
            localName = "",
            qName = "ServiceJourney",
            attributes = serviceJourneyAttrs,
            currentPath = "/ServiceJourney"
        )
    }

    @Test
    fun charactersDoesNotWriteIfElementShouldBeSkipped() {
        val blockAttrs = getAttributesForEntity(blockEntity)
        outputNetexSaxHandler.startElement("", "", "Block", blockAttrs)

        val chars = "some characters".toCharArray()
        outputNetexSaxHandler.characters(chars, 0, chars.size)
        verify(delegatingXmlElementWriter, never()).handleCharacters(
            chars,
            0,
            chars.size,
            currentPath = "/Block"
        )
    }

    @Test
    fun charactersWritesIfElementShouldBeIncluded() {
        val serviceJourneyAttrs = AttributesImpl()
        serviceJourneyAttrs.addAttribute("", "id", "id", "CDATA", serviceJourneyId)
        outputNetexSaxHandler.startElement("", "", "ServiceJourney", serviceJourneyAttrs)
        val chars = "some characters".toCharArray()
        outputNetexSaxHandler.characters(chars, 0, chars.size)
        verify(delegatingXmlElementWriter).handleCharacters(
            chars,
            0,
            chars.size,
            currentPath = "/ServiceJourney"
        )
    }

    @Test
    fun endElementDoesNotWriteIfElementShouldBeSkipped() {
        val blockAttrs = getAttributesForEntity(blockEntity)
        outputNetexSaxHandler.startElement("", "", "Block", blockAttrs)

        outputNetexSaxHandler.endElement("", "", "Block")
        verify(delegatingXmlElementWriter, never())
            .handleEndElement(
                uri = "",
                localName = "",
                qName = "Block",
                currentPath = "/Block"
            )
    }

    @Test
    fun endElementWritesIfElementShouldBeIncluded() {
        val serviceJourneyAttrs = AttributesImpl()
        serviceJourneyAttrs.addAttribute("", "id", "id", "CDATA", serviceJourneyId)
        outputNetexSaxHandler.startElement("", "", "ServiceJourney", serviceJourneyAttrs)
        outputNetexSaxHandler.endElement("", "", "ServiceJourney")
        verify(delegatingXmlElementWriter).handleEndElement(
            uri = "",
            localName = "",
            qName = "ServiceJourney",
            currentPath = "/ServiceJourney"
        )
    }

    @Test
    fun endElementStopsSkippingWhenSkippedElementEnds() {
        val blockAttrs = getAttributesForEntity(blockEntity)
        val serviceJourneyAttrs = getAttributesForEntity(serviceJourneyEntity)
        outputNetexSaxHandler.startElement("", "", "Block", blockAttrs)
        outputNetexSaxHandler.startElement("", "", "ServiceJourney", serviceJourneyAttrs)
        outputNetexSaxHandler.endElement("", "", "ServiceJourney")
        outputNetexSaxHandler.endElement("", "", "Block")
        verify(delegatingXmlElementWriter, never()).handleStartElement(
            uri = "",
            localName = "",
            qName = "Block",
            attributes = blockAttrs,
            currentPath = "/Block"
        )
        verify(delegatingXmlElementWriter, never()).handleStartElement(
            uri = "",
            localName = "",
            qName = "ServiceJourney",
            attributes = serviceJourneyAttrs,
            currentPath = "/Block/ServiceJourney"
        )
        verify(delegatingXmlElementWriter, never()).handleEndElement(
            uri = "",
            localName = "",
            qName = "ServiceJourney",
            currentPath = "/Block/ServiceJourney"
        )
        verify(delegatingXmlElementWriter, never()).handleEndElement(
            uri = "",
            localName = "",
            qName = "Block",
            currentPath = "/Block"
        )

        // After the skipped Block ends, we should be out of skip mode, and writing should resume
        outputNetexSaxHandler.startElement("", "", "ServiceJourney", serviceJourneyAttrs)
        verify(delegatingXmlElementWriter).handleStartElement(
            uri = "",
            localName = "",
            qName = "ServiceJourney",
            attributes = serviceJourneyAttrs,
            currentPath = "/ServiceJourney"
        )
    }

    @Test
    fun commentDoesNotWriteIfElementShouldBeSkipped() {
        val blockAttrs = getAttributesForEntity(blockEntity)
        outputNetexSaxHandler.startElement("", "", "Block", blockAttrs)

        outputNetexSaxHandler.comment("some comment".toCharArray(), 0, 0)
        verify(fileWriter, never()).writeComments("some comment".toCharArray(), 0, 0)
    }

    @Test
    fun endElementDoesNotWriteIfElementIsInSkippedElements() {
        val attrs = AttributesImpl()

        outputNetexSaxHandler.startElement("", "", "Parent", attrs)
        outputNetexSaxHandler.startElement("", "", "Child", attrs)
        outputNetexSaxHandler.startElement("", "", "Child", attrs)
        outputNetexSaxHandler.startElement("", "", "GrandChild", attrs)

        outputNetexSaxHandler.endElement("", "", "GrandChild")
        outputNetexSaxHandler.endElement("", "", "Child")
        outputNetexSaxHandler.endElement("", "", "Child")
        outputNetexSaxHandler.endElement("", "", "Parent")

        verify(delegatingXmlElementWriter).handleStartElement("", "", "Parent", attrs, "/Parent")
        verify(delegatingXmlElementWriter, never()).handleStartElement("", "", "Child", attrs, "/Parent/Child")
        verify(delegatingXmlElementWriter, never()).handleStartElement("", "", "GrandChild", attrs, "/Parent/Child/GrandChild")
        verify(delegatingXmlElementWriter, never()).handleEndElement("", "", "GrandChild", "/Parent/Child/GrandChild")
        verify(delegatingXmlElementWriter, never()).handleEndElement("", "", "Child", "/Parent/Child")
        verify(delegatingXmlElementWriter).handleEndElement("", "", "Parent", "/Parent")
    }
}