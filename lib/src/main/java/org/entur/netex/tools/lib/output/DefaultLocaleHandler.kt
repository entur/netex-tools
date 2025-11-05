package org.entur.netex.tools.lib.output

import org.xml.sax.Attributes
import org.xml.sax.helpers.AttributesImpl

class DefaultLocaleHandler: XMLElementHandler {
    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?,
        writer: DelegatingXMLElementWriter
    ) {
        writer.startElement(uri, localName, qName, attributes)

        val timezone = "Europe/Oslo"
        writer.startElement("", "TimeZone", "TimeZone", AttributesImpl())
        writer.characters(timezone.toCharArray(), 0, timezone.length)
        writer.endElement("", "TimeZone", "TimeZone")

        val defaultLanguage = "no"
        writer.startElement("", "DefaultLanguage", "DefaultLanguage", AttributesImpl())
        writer.characters(defaultLanguage.toCharArray(), 0, defaultLanguage.length)
        writer.endElement("", "DefaultLanguage", "DefaultLanguage")
    }

    override fun characters(
        ch: CharArray?,
        start: Int,
        length: Int,
        writer: DelegatingXMLElementWriter
    ) {
        writer.characters(ch, start, length)
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