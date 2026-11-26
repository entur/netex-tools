package org.entur.netex.tools.lib.sax

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.xml.sax.SAXParseException
import java.io.ByteArrayInputStream
import java.io.File

/**
 * Verifies that [XMLFileParser] does not resolve XML external entities (XXE).
 * All inputs below are mock test fixtures: a throwaway file in a temp dir and a
 * placeholder marker string stand in for whatever a real external entity might
 * reference. Nothing here touches real files or data.
 */
class XMLFileParserTest {

    /** Collects every character event the parser reports, so the test can inspect what was parsed. */
    private class CapturingSaxHandler : NetexToolsSaxHandler() {
        val text = StringBuilder()

        override fun characters(ch: CharArray?, start: Int, length: Int) {
            if (ch != null) text.append(ch, start, length)
        }
    }

    private fun netexReferencing(systemId: String) = """
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE PublicationDelivery [ <!ENTITY payload SYSTEM "$systemId"> ]>
        <PublicationDelivery xmlns="http://www.netex.org.uk/netex" version="1.0">
          <Description>&payload;</Description>
        </PublicationDelivery>
    """.trimIndent()

    @Test
    fun `does not inline local file contents referenced by an external entity`(@TempDir tempDir: File) {
        // Mock target: a temp file whose unique marker content we can look for in the parsed output.
        val markerContent = "mock-external-entity-content"
        val referencedFile = File(tempDir, "entity-target.txt").apply { writeText(markerContent) }
        val handler = CapturingSaxHandler()

        runCatching {
            XMLFileParser.parseXml(
                ByteArrayInputStream(netexReferencing(referencedFile.toURI().toString()).toByteArray()),
                handler
            )
        }

        assertFalse(
            handler.text.contains(markerContent),
            "external entity was resolved, leaking referenced file contents into the parsed document"
        )
    }

    @Test
    fun `rejects documents declaring a DOCTYPE`() {
        // The system id is a placeholder; the DOCTYPE must be rejected before any resolution is attempted.
        val xml = netexReferencing("file:///mock/entity-target")

        assertThrows<SAXParseException> {
            XMLFileParser.parseXml(ByteArrayInputStream(xml.toByteArray()), CapturingSaxHandler())
        }
    }
}
