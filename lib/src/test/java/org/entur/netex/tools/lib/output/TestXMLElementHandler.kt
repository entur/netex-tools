package org.entur.netex.tools.lib.output

import org.xml.sax.Attributes

class TestXMLElementHandler: XMLElementHandler {
    var hasCalledStartElement = false
    var hasCalledAfterStartElement = false
    var hasCalledCharacters = false
    var hasCalledBeforeEndElement = false
    var hasCalledEndElement = false

    /**
     * Order in which callbacks fired, for verifying dispatch ordering.
     */
    val callOrder: MutableList<String> = mutableListOf()

    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?,
        writer: DelegatingXMLElementWriter
    ) {
        hasCalledStartElement = true
        callOrder += "startElement"
    }

    override fun afterStartElement(
        uri: String?,
        localName: String?,
        qName: String?,
        writer: DelegatingXMLElementWriter
    ) {
        hasCalledAfterStartElement = true
        callOrder += "afterStartElement"
    }

    override fun characters(
        ch: CharArray?,
        start: Int,
        length: Int,
        writer: DelegatingXMLElementWriter
    ) {
        hasCalledCharacters = true
        callOrder += "characters"
    }

    override fun beforeEndElement(
        uri: String?,
        localName: String?,
        qName: String?,
        writer: DelegatingXMLElementWriter
    ) {
        hasCalledBeforeEndElement = true
        callOrder += "beforeEndElement"
    }

    override fun endElement(
        uri: String?,
        localName: String?,
        qName: String?,
        writer: DelegatingXMLElementWriter
    ) {
        hasCalledEndElement = true
        callOrder += "endElement"
    }

    fun reset() {
        hasCalledStartElement = false
        hasCalledAfterStartElement = false
        hasCalledCharacters = false
        hasCalledBeforeEndElement = false
        hasCalledEndElement = false
        callOrder.clear()
    }
}
