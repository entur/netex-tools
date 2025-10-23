package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.data.TestDataFactory
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.plugin.DefaultNetexFileWriter
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
        entityModel = entityModel,
        entitySelection = entitySelection,
        refSelection = refSelection,
        skipElements = listOf(
            "/PublicationDelivery/dataObjects/CompositeFrame/frames/VehicleScheduleFrame",
            "/PublicationDelivery/dataObjects/CompositeFrame/frames/ServiceFrame/lines/Line/routes",
            "/PublicationDelivery/dataObjects/CompositeFrame/frames/TimetableFrame/vehicleJourneys/DeadRun"
        )
    )

    private val testFile = File("test.xml")
    private val writer = mock<DefaultNetexFileWriter>()

    private lateinit var outputNetexSaxHandler: OutputNetexSaxHandler

    @BeforeEach
    fun setUp() {
        outputNetexSaxHandler = OutputNetexSaxHandler(
            entityModel = TestDataFactory.defaultEntityModel(),
            fileIndex = fileIndex,
            inclusionPolicy = inclusionPolicy,
            netexFileWriter = writer,
            outputFile = testFile,
        )
    }

    private val serviceJourneyId = "service_journey_id"
    private val serviceJourneyEntity = TestDataFactory.defaultEntity(id = serviceJourneyId, type = "ServiceJourney")
    private val blockId = "block_id"
    private val blockEntity = TestDataFactory.defaultEntity(id = blockId, type = "Block")

    init {
        entityModel.addEntity(serviceJourneyEntity)
        entitySelection.selection["ServiceJourney"] = mutableMapOf(serviceJourneyId to serviceJourneyEntity)

        entityModel.addEntity(blockEntity)
    }

    private fun getAttributesForEntity(entity: Entity): AttributesImpl {
        val attrs = AttributesImpl()
        attrs.addAttribute("", "id", "id", "CDATA", entity.id)
        return attrs
    }

    @Test
    fun startDocument() {
        outputNetexSaxHandler.startDocument()
        verify(writer).startDocument()
    }

    @Test
    fun endDocument() {
        outputNetexSaxHandler.endDocument()
        verify(writer).endDocument()
    }

    @Test
    fun startElementDoesNotWriteTagIfElementShouldBeSkipped() {
        val blockAttrs = getAttributesForEntity(blockEntity)
        outputNetexSaxHandler.startElement("", "", "Block", blockAttrs)
        verify(writer, never()).writeStartElement(qName = "Block", attributes = blockAttrs)

        // Verifies that children of skipped elements are also skipped, regardless of selection
        val serviceJourneyAttrs = AttributesImpl()
        serviceJourneyAttrs.addAttribute("", "id", "id", "CDATA", serviceJourneyId)
        outputNetexSaxHandler.startElement("", "", "ServiceJourney", serviceJourneyAttrs)
        verify(writer, never()).writeStartElement(qName = "ServiceJourney", attributes = serviceJourneyAttrs)
    }

    @Test
    fun startElementWritesTagIfElementShouldBeIncluded() {
        val serviceJourneyAttrs = getAttributesForEntity(serviceJourneyEntity)
        outputNetexSaxHandler.startElement("", "", "ServiceJourney", serviceJourneyAttrs)
        verify(writer).writeStartElement(qName = "ServiceJourney", attributes = serviceJourneyAttrs)
    }

    @Test
    fun charactersDoesNotWriteIfElementShouldBeSkipped() {
        val blockAttrs = getAttributesForEntity(blockEntity)
        outputNetexSaxHandler.startElement("", "", "Block", blockAttrs)

        val chars = "some characters".toCharArray()
        outputNetexSaxHandler.characters(chars, 0, chars.size)
        verify(writer, never()).writeCharacters(chars, 0, chars.size)
    }

    @Test
    fun charactersWritesIfElementShouldBeIncluded() {
        val chars = "some characters".toCharArray()
        outputNetexSaxHandler.characters(chars, 0, chars.size)
        verify(writer).writeCharacters(chars, 0, chars.size)
    }

    @Test
    fun endElementDoesNotWriteIfElementShouldBeSkipped() {
        val blockAttrs = getAttributesForEntity(blockEntity)
        outputNetexSaxHandler.startElement("", "", "Block", blockAttrs)

        outputNetexSaxHandler.endElement("", "", "Block")
        verify(writer, never()).writeEndElement("Block")
    }

    @Test
    fun endElementWritesIfElementShouldBeIncluded() {
        outputNetexSaxHandler.endElement("", "", "ServiceJourney")
        verify(writer).writeEndElement("ServiceJourney")
    }

    @Test
    fun endElementStopsSkippingWhenSkippedElementEnds() {
        val blockAttrs = getAttributesForEntity(blockEntity)
        val serviceJourneyAttrs = getAttributesForEntity(serviceJourneyEntity)
        outputNetexSaxHandler.startElement("", "", "Block", blockAttrs)
        outputNetexSaxHandler.startElement("", "", "ServiceJourney", serviceJourneyAttrs)
        outputNetexSaxHandler.endElement("", "", "ServiceJourney")
        outputNetexSaxHandler.endElement("", "", "Block")
        verify(writer, never()).writeStartElement(qName = "Block", attributes = blockAttrs)
        verify(writer, never()).writeStartElement(qName = "ServiceJourney", attributes = serviceJourneyAttrs)
        verify(writer, never()).writeEndElement("ServiceJourney")
        verify(writer, never()).writeEndElement("Block")

        // After the skipped Block ends, we should be out of skip mode, and writing should resume
        outputNetexSaxHandler.startElement("", "", "ServiceJourney", serviceJourneyAttrs)
        verify(writer).writeStartElement(qName = "ServiceJourney", attributes = serviceJourneyAttrs)
    }

    @Test
    fun commentDoesNotWriteIfElementShouldBeSkipped() {
        val blockAttrs = getAttributesForEntity(blockEntity)
        outputNetexSaxHandler.startElement("", "", "Block", blockAttrs)

        outputNetexSaxHandler.comment("some comment".toCharArray(), 0, 0)
        verify(writer, never()).writeComments("some comment".toCharArray(), 0, 0)
    }
}