package org.entur.netex.tools.lib

import org.entur.netex.tools.lib.config.FilterConfig
import org.entur.netex.tools.lib.plugin.AbstractNetexPlugin
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.xml.sax.Attributes
import java.io.File

class NetexFilterTest {

    private val minimalNetexXml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <PublicationDelivery xmlns="http://www.netex.org.uk/netex" version="1.0">
          <dataObjects>
            <CompositeFrame id="CF:1" version="1">
              <frames>
                <ServiceFrame id="SF:1" version="1">
                  <scheduledStopPoints>
                    <ScheduledStopPoint id="SSP:1" version="1">
                      <Name>Stop One</Name>
                    </ScheduledStopPoint>
                    <ScheduledStopPoint id="SSP:2" version="1">
                      <Name>Stop Two</Name>
                    </ScheduledStopPoint>
                  </scheduledStopPoints>
                </ServiceFrame>
              </frames>
            </CompositeFrame>
          </dataObjects>
        </PublicationDelivery>
    """.trimIndent()

    @Test
    fun `buildEntityModel from byte arrays populates model`() {
        val filter = NetexFilter()
        val documents = mapOf("test.xml" to minimalNetexXml.toByteArray())

        filter.buildEntityModel(documents)

        assertTrue(filter.model.listAllEntities().isNotEmpty())
        assertNotNull(filter.model.getEntity("SSP:1"))
        assertNotNull(filter.model.getEntity("SSP:2"))
    }

    @Test
    fun `buildEntityModel from directory populates model`(@TempDir tempDir: File) {
        File(tempDir, "test.xml").writeText(minimalNetexXml)

        val filter = NetexFilter()
        filter.buildEntityModel(tempDir)

        assertTrue(filter.model.listAllEntities().isNotEmpty())
        assertNotNull(filter.model.getEntity("SSP:1"))
    }

    @Test
    fun `run with byte arrays produces output`() {
        val documents = mapOf("test.xml" to minimalNetexXml.toByteArray())
        val filter = NetexFilter()

        val result = filter.run(documents)

        assertTrue(result.documents.containsKey("test.xml"))
        val outputXml = String(result.documents["test.xml"]!!, Charsets.UTF_8)
        assertTrue(outputXml.contains("ScheduledStopPoint"))
        assertTrue(outputXml.contains("SSP:1"))
    }

    @Test
    fun `run with files produces output`(@TempDir tempDir: File) {
        val inputDir = File(tempDir, "input").also { it.mkdirs() }
        val outputDir = File(tempDir, "output")
        File(inputDir, "test.xml").writeText(minimalNetexXml)

        val filter = NetexFilter()
        val report = filter.run(inputDir, outputDir)

        assertTrue(outputDir.exists())
        val outputFile = File(outputDir, "test.xml")
        assertTrue(outputFile.exists())
        val outputXml = outputFile.readText()
        assertTrue(outputXml.contains("ScheduledStopPoint"))
    }

    @Test
    fun `between-pass hook allows accessing model before export`() {
        val documents = mapOf("test.xml" to minimalNetexXml.toByteArray())
        val filter = NetexFilter()

        // Pass 1
        filter.buildEntityModel(documents)

        // Between-pass hook: inspect model
        val entities = filter.model.listAllEntities()
        val stopPointIds = entities
            .filter { it.type == "ScheduledStopPoint" }
            .map { it.id }
        assertTrue(stopPointIds.contains("SSP:1"))
        assertTrue(stopPointIds.contains("SSP:2"))

        // Pass 2
        val (entitySelection, refSelection) = filter.selectEntities()
        val result = filter.exportToByteArrays(documents, entitySelection, refSelection)

        assertTrue(result.documents.containsKey("test.xml"))
    }

    @Test
    fun `plugins receive callbacks during byte-array processing`() {
        val plugin = StopPointCollectorPlugin()
        val filterConfig = FilterConfig().toBuilder()
            .withPlugins(listOf(plugin))
            .build()
        val filter = NetexFilter(filterConfig = filterConfig)
        val documents = mapOf("test.xml" to minimalNetexXml.toByteArray())

        filter.buildEntityModel(documents)

        val collected = plugin.getCollectedData() as List<*>
        assertEquals(2, collected.size)
        assertTrue(collected.contains("SSP:1"))
        assertTrue(collected.contains("SSP:2"))
    }

    @Test
    fun `buildEntityModel resets state on repeated calls`() {
        val filter = NetexFilter()
        val docs1 = mapOf("a.xml" to minimalNetexXml.toByteArray())

        filter.buildEntityModel(docs1)
        val countAfterFirst = filter.model.listAllEntities().size
        assertTrue(countAfterFirst > 0)

        // Second call should replace, not accumulate
        filter.buildEntityModel(docs1)
        assertEquals(countAfterFirst, filter.model.listAllEntities().size)
    }

    @Test
    fun `exportToByteArrays resets fileIndex on repeated calls`() {
        val filter = NetexFilter()
        val documents = mapOf("test.xml" to minimalNetexXml.toByteArray())

        filter.buildEntityModel(documents)
        val (entitySelection, refSelection) = filter.selectEntities()

        filter.exportToByteArrays(documents, entitySelection, refSelection)
        val entitiesCount1 = filter.fileIndex.entitiesByDocument.values.sumOf { it.size }

        // Second export should not accumulate fileIndex entries
        filter.exportToByteArrays(documents, entitySelection, refSelection)
        val entitiesCount2 = filter.fileIndex.entitiesByDocument.values.sumOf { it.size }

        assertEquals(entitiesCount1, entitiesCount2)
    }

    @Test
    fun `between-pass export validates report contents`() {
        val documents = mapOf("test.xml" to minimalNetexXml.toByteArray())
        val filter = NetexFilter()

        filter.buildEntityModel(documents)
        val (entitySelection, refSelection) = filter.selectEntities()
        val result = filter.exportToByteArrays(documents, entitySelection, refSelection)

        // Report should contain entries for the processed document
        assertTrue(result.report.entitiesByDocument.isNotEmpty())
        assertTrue(result.report.elementTypesByDocument.isNotEmpty())
        assertTrue(result.report.getNumberOfElementsOfType("ScheduledStopPoint") >= 2)
    }

    @Test
    fun `exportToByteArrays respects fileNameMap`() {
        val filterConfig = FilterConfig().toBuilder()
            .withFileNameMap(mapOf("test.xml" to "renamed.xml"))
            .build()
        val filter = NetexFilter(filterConfig = filterConfig)
        val documents = mapOf("test.xml" to minimalNetexXml.toByteArray())

        val result = filter.run(documents)

        assertTrue(result.documents.containsKey("renamed.xml"))
        assertFalse(result.documents.containsKey("test.xml"))
    }

    @Test
    fun `plugins receive document name string in stream mode`() {
        val plugin = DocumentNameTrackingPlugin()
        val filterConfig = FilterConfig().toBuilder()
            .withPlugins(listOf(plugin))
            .build()
        val filter = NetexFilter(filterConfig = filterConfig)
        val documents = mapOf("my-file.xml" to minimalNetexXml.toByteArray())

        filter.buildEntityModel(documents)

        assertEquals("my-file.xml", plugin.lastDocumentName)
        assertFalse(plugin.receivedFile, "Stream mode should call endDocument(String), not endDocument(File)")
    }

    @Test
    fun `plugins receive File in file mode`(@TempDir tempDir: File) {
        val plugin = DocumentNameTrackingPlugin()
        val filterConfig = FilterConfig().toBuilder()
            .withPlugins(listOf(plugin))
            .build()
        val filter = NetexFilter(filterConfig = filterConfig)
        File(tempDir, "my-file.xml").writeText(minimalNetexXml)

        filter.buildEntityModel(tempDir)

        assertEquals("my-file.xml", plugin.lastDocumentName)
        assertTrue(plugin.receivedFile, "File mode should call endDocument(File)")
    }

    private class StopPointCollectorPlugin : AbstractNetexPlugin() {
        private val stopPointIds = mutableListOf<String>()

        override fun getName() = "StopPointCollector"
        override fun getDescription() = "Collects ScheduledStopPoint IDs"
        override fun getSupportedElementTypes() = setOf("ScheduledStopPoint")

        override fun startElement(elementName: String, attributes: Attributes?, currentEntity: org.entur.netex.tools.lib.model.Entity?) {
            attributes?.getValue("id")?.let { stopPointIds.add(it) }
        }

        override fun getCollectedData(): List<String> = stopPointIds
    }

    private class DocumentNameTrackingPlugin : AbstractNetexPlugin() {
        var lastDocumentName: String? = null
        var receivedFile: Boolean = false

        override fun getName() = "DocumentNameTracker"
        override fun getDescription() = "Tracks document names"
        override fun getSupportedElementTypes() = setOf("ScheduledStopPoint")

        override fun endDocument(file: File) {
            receivedFile = true
            lastDocumentName = file.name
        }

        override fun endDocument(documentName: String) {
            lastDocumentName = documentName
        }

        override fun getCollectedData() = lastDocumentName
    }
}
