package org.entur.netex.tools.lib.output

import org.entur.netex.tools.lib.extensions.addNewAttribute
import org.xml.sax.Attributes
import org.xml.sax.helpers.AttributesImpl

class QuayRefHandler: XMLElementHandler {
    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?,
        writer: DelegatingXMLElementWriter
    ) {
        val refValue = attributes?.getValue("ref") ?: ""
        val attrs = AttributesImpl()
        attrs.addNewAttribute("ref", refValue)
        writer.startElement(
            qName = qName,
            attributes = attrs,
            uri = uri,
            localName = localName,
        )
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
