package org.entur.netex.tools.lib.output

import org.xml.sax.Attributes

/**
 * Writer for skipping all the contents of a certain element
 **/
class SkipElementHandler: XMLElementHandler {
    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?,
        writer: DelegatingXMLElementWriter
    ) {
        // should write nothing
    }

    override fun characters(
        ch: CharArray?,
        start: Int,
        length: Int,
        writer: DelegatingXMLElementWriter
    ) {
        // should write nothing
    }

    override fun endElement(
        uri: String?,
        localName: String?,
        qName: String?,
        writer: DelegatingXMLElementWriter
    ) {
        // should write nothing
    }

}