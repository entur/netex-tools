package org.entur.netex.tools.lib.output

import org.xml.sax.Attributes

class TestXMLElementHandler: XMLElementHandler {
    var hasCalledStartElement = false
    var hasCalledCharacters = false
    var hasCalledEndElement = false

    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?,
        writer: DelegatingXMLElementWriter
    ) {
        hasCalledStartElement = true
    }

    override fun characters(
        ch: CharArray?,
        start: Int,
        length: Int,
        writer: DelegatingXMLElementWriter
    ) {
        hasCalledCharacters = true
    }

    override fun endElement(
        uri: String?,
        localName: String?,
        qName: String?,
        writer: DelegatingXMLElementWriter
    ) {
        hasCalledEndElement = true
    }

    fun reset() {
        hasCalledStartElement = false
        hasCalledCharacters = false
        hasCalledEndElement = false
    }
}