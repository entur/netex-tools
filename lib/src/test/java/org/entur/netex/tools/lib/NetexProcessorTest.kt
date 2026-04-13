package org.entur.netex.tools.lib

import org.entur.netex.tools.lib.config.FilterConfig
import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.entur.netex.tools.lib.output.XMLElementHandler
import org.entur.netex.tools.lib.plugin.AbstractNetexPlugin
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.xml.sax.Attributes
import org.xml.sax.helpers.AttributesImpl
import java.io.File

class NetexProcessorTest {

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
        val filter = NetexProcessor()
        val documents = mapOf("test.xml" to minimalNetexXml.toByteArray())

        filter.buildEntityModel(documents)

        assertTrue(filter.model.listAllEntities().isNotEmpty())
        assertNotNull(filter.model.getEntity("SSP:1"))
        assertNotNull(filter.model.getEntity("SSP:2"))
    }

    @Test
    fun `buildEntityModel from directory populates model`(@TempDir tempDir: File) {
        File(tempDir, "test.xml").writeText(minimalNetexXml)

        val filter = NetexProcessor()
        filter.buildEntityModel(tempDir)

        assertTrue(filter.model.listAllEntities().isNotEmpty())
        assertNotNull(filter.model.getEntity("SSP:1"))
    }

    @Test
    fun `run with byte arrays produces output`() {
        val documents = mapOf("test.xml" to minimalNetexXml.toByteArray())
        val filter = NetexProcessor()

        val result = filter.run(documents)

        val outputBytes = requireNotNull(result.documents["test.xml"])
        val outputXml = String(outputBytes, Charsets.UTF_8)
        assertTrue(outputXml.contains("ScheduledStopPoint"))
        assertTrue(outputXml.contains("SSP:1"))
    }

    @Test
    fun `run with files produces output`(@TempDir tempDir: File) {
        val inputDir = File(tempDir, "input").also { it.mkdirs() }
        val outputDir = File(tempDir, "output")
        File(inputDir, "test.xml").writeText(minimalNetexXml)

        val filter = NetexProcessor()
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
        val filter = NetexProcessor()

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
        val filter = NetexProcessor(filterConfig = filterConfig)
        val documents = mapOf("test.xml" to minimalNetexXml.toByteArray())

        filter.buildEntityModel(documents)

        val collected = plugin.getCollectedData() as List<*>
        assertEquals(2, collected.size)
        assertTrue(collected.contains("SSP:1"))
        assertTrue(collected.contains("SSP:2"))
    }

    @Test
    fun `buildEntityModel resets state on repeated calls`() {
        val filter = NetexProcessor()
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
        val filter = NetexProcessor()
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
        val filter = NetexProcessor()

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
        val filter = NetexProcessor(filterConfig = filterConfig)
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
        val filter = NetexProcessor(filterConfig = filterConfig)
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
        val filter = NetexProcessor(filterConfig = filterConfig)
        File(tempDir, "my-file.xml").writeText(minimalNetexXml)

        filter.buildEntityModel(tempDir)

        assertEquals("my-file.xml", plugin.lastDocumentName)
        assertTrue(plugin.receivedFile, "File mode should call endDocument(File)")
    }

    private val netexWithCoordinates = """
        <?xml version="1.0" encoding="UTF-8"?>
        <PublicationDelivery xmlns="http://www.netex.org.uk/netex" version="1.0">
          <dataObjects>
            <CompositeFrame id="CF:1" version="1">
              <frames>
                <ServiceFrame id="SF:1" version="1">
                  <scheduledStopPoints>
                    <ScheduledStopPoint id="SSP:1" version="1">
                      <Name>Central Station</Name>
                      <Location>
                        <Longitude>10.75</Longitude>
                        <Latitude>59.91</Latitude>
                      </Location>
                    </ScheduledStopPoint>
                    <ScheduledStopPoint id="SSP:2" version="1">
                      <Name>Airport</Name>
                      <Location>
                        <Longitude>11.08</Longitude>
                        <Latitude>60.19</Latitude>
                      </Location>
                    </ScheduledStopPoint>
                  </scheduledStopPoints>
                </ServiceFrame>
              </frames>
            </CompositeFrame>
          </dataObjects>
        </PublicationDelivery>
    """.trimIndent()

    @Test
    fun `scoped plugin extracts nested coordinates`() {
        val plugin = ScopedCoordinateExtractorPlugin()
        val filterConfig = FilterConfig().toBuilder()
            .withPlugins(listOf(plugin))
            .build()
        val processor = NetexProcessor(filterConfig = filterConfig)

        processor.buildEntityModel(mapOf("test.xml" to netexWithCoordinates.toByteArray()))

        @Suppress("UNCHECKED_CAST")
        val coords = plugin.getCollectedData() as Map<String, Pair<String, String>>
        assertEquals(2, coords.size)
        assertEquals("10.75" to "59.91", coords["SSP:1"])
        assertEquals("11.08" to "60.19", coords["SSP:2"])
    }


    @Test
    fun `beforeEndElement hook injects new sibling elements before container closes`() {
        val containerPath = "/PublicationDelivery/dataObjects/CompositeFrame/frames/ServiceFrame/scheduledStopPoints"
        val injector = PassThroughWithInjectionHandler { writer ->
            val attrs = AttributesImpl().apply {
                addAttribute("", "id", "id", "CDATA", "SSP:INJECTED")
                addAttribute("", "version", "version", "CDATA", "1")
            }
            writer.startElement(NETEX_NS, "ScheduledStopPoint", "ScheduledStopPoint", attrs)
            writer.endElement(NETEX_NS, "ScheduledStopPoint", "ScheduledStopPoint")
        }

        val filterConfig = FilterConfig().toBuilder()
            .withCustomElementHandlers(mapOf(containerPath to injector))
            .build()
        val processor = NetexProcessor(filterConfig = filterConfig)
        val documents = mapOf("test.xml" to minimalNetexXml.toByteArray())

        val result = processor.run(documents)

        val outputXml = String(requireNotNull(result.documents["test.xml"]), Charsets.UTF_8)
        assertTrue(outputXml.contains("SSP:INJECTED"), "Injected element should appear in output: $outputXml")
        // Injected element should come after the original children and before </scheduledStopPoints>
        val ssp2Index = outputXml.indexOf("SSP:2")
        val injectedIndex = outputXml.indexOf("SSP:INJECTED")
        val closeTagIndex = outputXml.indexOf("</scheduledStopPoints>")
        assertTrue(ssp2Index in 0 until injectedIndex, "Injected element should follow existing children")
        assertTrue(injectedIndex < closeTagIndex, "Injected element should precede closing tag")
    }

    @Test
    fun `hooks fire in correct order for nested handlers`() {
        val parentPath = "/PublicationDelivery/dataObjects/CompositeFrame/frames/ServiceFrame/scheduledStopPoints"
        val childPath = "$parentPath/ScheduledStopPoint"
        val trace = mutableListOf<String>()
        val parent = TracingHandler("scheduledStopPoints", trace)
        val child = TracingHandler("ScheduledStopPoint", trace)

        val filterConfig = FilterConfig().toBuilder()
            .withCustomElementHandlers(mapOf(parentPath to parent, childPath to child))
            .build()
        val processor = NetexProcessor(filterConfig = filterConfig)
        val documents = mapOf("test.xml" to minimalNetexXml.toByteArray())

        processor.run(documents)

        // Expected ordering for one parent with two children:
        //   parent.startElement, parent.afterStartElement,
        //     child1.startElement, child1.afterStartElement, child1.beforeEndElement, child1.endElement,
        //     child2.startElement, child2.afterStartElement, child2.beforeEndElement, child2.endElement,
        //   parent.beforeEndElement, parent.endElement
        assertEquals(
            listOf(
                "scheduledStopPoints:startElement",
                "scheduledStopPoints:afterStartElement",
                "ScheduledStopPoint:startElement",
                "ScheduledStopPoint:afterStartElement",
                "ScheduledStopPoint:beforeEndElement",
                "ScheduledStopPoint:endElement",
                "ScheduledStopPoint:startElement",
                "ScheduledStopPoint:afterStartElement",
                "ScheduledStopPoint:beforeEndElement",
                "ScheduledStopPoint:endElement",
                "scheduledStopPoints:beforeEndElement",
                "scheduledStopPoints:endElement",
            ),
            trace,
        )
    }

    @Test
    fun `beforeEndElement hook fires for deferred elements when their required children are present`() {
        // ScheduledStopPoint is deferred until its child Name is seen; when flushed, hooks must fire.
        val handlerPath = "/PublicationDelivery/dataObjects/CompositeFrame/frames/ServiceFrame/scheduledStopPoints/ScheduledStopPoint"
        val injector = PassThroughWithInjectionHandler { writer ->
            writer.startElement(NETEX_NS, "ShortName", "ShortName", AttributesImpl())
            val text = "INJECTED".toCharArray()
            writer.characters(text, 0, text.size)
            writer.endElement(NETEX_NS, "ShortName", "ShortName")
        }
        val filterConfig = FilterConfig().toBuilder()
            .withCustomElementHandlers(mapOf(handlerPath to injector))
            .withElementsRequiredChildren(mapOf("ScheduledStopPoint" to listOf("Name")))
            .build()
        val processor = NetexProcessor(filterConfig = filterConfig)
        val documents = mapOf("test.xml" to minimalNetexXml.toByteArray())

        val result = processor.run(documents)

        val outputXml = String(requireNotNull(result.documents["test.xml"]), Charsets.UTF_8)
        // One <ShortName>INJECTED</ShortName> for each ScheduledStopPoint (two in total).
        val count = Regex("<ShortName[^>]*>INJECTED</ShortName>").findAll(outputXml).count()
        assertEquals(2, count, "beforeEndElement should fire once per deferred element on flush: $outputXml")
    }

    private companion object {
        const val NETEX_NS = "http://www.netex.org.uk/netex"
    }

    /** Passes source events through unchanged and runs [inject] in beforeEndElement. */
    private class PassThroughWithInjectionHandler(
        private val inject: (DelegatingXMLElementWriter) -> Unit,
    ) : XMLElementHandler {
        override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?, writer: DelegatingXMLElementWriter) {
            writer.startElement(uri, localName, qName, attributes)
        }

        override fun characters(ch: CharArray?, start: Int, length: Int, writer: DelegatingXMLElementWriter) {
            writer.characters(ch, start, length)
        }

        override fun endElement(uri: String?, localName: String?, qName: String?, writer: DelegatingXMLElementWriter) {
            writer.endElement(uri, localName, qName)
        }

        override fun beforeEndElement(uri: String?, localName: String?, qName: String?, writer: DelegatingXMLElementWriter) {
            inject(writer)
        }
    }

    /** Records which callbacks fired, tagged with a label, for ordering assertions. */
    private class TracingHandler(
        private val label: String,
        private val trace: MutableList<String>,
    ) : XMLElementHandler {
        override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?, writer: DelegatingXMLElementWriter) {
            trace += "$label:startElement"
            writer.startElement(uri, localName, qName, attributes)
        }

        override fun afterStartElement(uri: String?, localName: String?, qName: String?, writer: DelegatingXMLElementWriter) {
            trace += "$label:afterStartElement"
        }

        override fun characters(ch: CharArray?, start: Int, length: Int, writer: DelegatingXMLElementWriter) {
            writer.characters(ch, start, length)
        }

        override fun beforeEndElement(uri: String?, localName: String?, qName: String?, writer: DelegatingXMLElementWriter) {
            trace += "$label:beforeEndElement"
        }

        override fun endElement(uri: String?, localName: String?, qName: String?, writer: DelegatingXMLElementWriter) {
            trace += "$label:endElement"
            writer.endElement(uri, localName, qName)
        }
    }

    /** Uses scoped registration — separate callbacks per element, no descendant mode needed. */
    private class ScopedCoordinateExtractorPlugin : AbstractNetexPlugin() {
        private val coordinates = mutableMapOf<String, Pair<String, String>>()
        private var currentLon = StringBuilder()
        private var currentLat = StringBuilder()
        private var activeField: StringBuilder? = null
        private var currentStopPointId: String? = null

        override fun getName() = "ScopedCoordinateExtractor"
        override fun getDescription() = "Extracts coordinates via scoped element registration"
        override fun getSupportedElementTypes() = setOf(
            "ScheduledStopPoint",
            "ScheduledStopPoint/Longitude",
            "ScheduledStopPoint/Latitude",
        )

        override fun startElement(elementName: String, attributes: Attributes?, currentEntity: org.entur.netex.tools.lib.model.Entity?) {
            when (elementName) {
                "ScheduledStopPoint" -> currentStopPointId = currentEntity?.id
                "Longitude" -> activeField = currentLon
                "Latitude" -> activeField = currentLat
            }
        }

        override fun characters(elementName: String, ch: CharArray?, start: Int, length: Int) {
            ch?.let { activeField?.append(it, start, length) }
        }

        override fun endElement(elementName: String, currentEntity: org.entur.netex.tools.lib.model.Entity?) {
            when (elementName) {
                "Longitude", "Latitude" -> activeField = null
                "ScheduledStopPoint" -> {
                    val id = currentStopPointId
                    if (id != null && currentLon.isNotEmpty() && currentLat.isNotEmpty()) {
                        coordinates[id] = currentLon.toString() to currentLat.toString()
                    }
                    currentLon.clear()
                    currentLat.clear()
                    currentStopPointId = null
                }
            }
        }

        override fun getCollectedData(): Map<String, Pair<String, String>> = coordinates
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
