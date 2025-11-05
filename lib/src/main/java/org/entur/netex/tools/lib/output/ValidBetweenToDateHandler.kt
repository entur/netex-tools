package org.entur.netex.tools.lib.output

import org.entur.netex.tools.lib.extensions.toISO8601
import org.xml.sax.Attributes
import java.time.LocalDate

class ValidBetweenToDateHandler(val toDate: LocalDate): XMLElementHandler {
    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?,
        writer: DelegatingXMLElementWriter
    ) {
        writer.startElement(uri, localName, qName, attributes)
    }

    override fun characters(
        ch: CharArray?,
        start: Int,
        length: Int,
        writer: DelegatingXMLElementWriter
    ) {
        val content = toDate.toISO8601()
        writer.characters(content.toCharArray(), 0, content.length)
    }

    override fun endElement(
        uri: String?,
        localName: String?,
        qName: String?,
        writer: DelegatingXMLElementWriter
    ) {
        writer.endElement(uri, localName, qName)
    }
}