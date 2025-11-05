package org.entur.netex.tools.lib.output

import org.entur.netex.tools.lib.extensions.addNewAttribute
import org.entur.netex.tools.lib.model.NetexTypes
import org.xml.sax.Attributes
import org.xml.sax.helpers.AttributesImpl

class ValidBetweenHandler(private val codespace: String): XMLElementHandler {
    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?,
        writer: DelegatingXMLElementWriter
    ) {
        val newAttributes = AttributesImpl()
        val id = NetexIdGenerator.next(codespace.uppercase(), NetexTypes.AVAILABILITY_CONDITION)
        newAttributes.addNewAttribute("id", id)
        newAttributes.addNewAttribute("version", "1")
        writer.startElement(uri, NetexTypes.AVAILABILITY_CONDITION, NetexTypes.AVAILABILITY_CONDITION, newAttributes)
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
        writer.endElement(uri, NetexTypes.AVAILABILITY_CONDITION, NetexTypes.AVAILABILITY_CONDITION)
    }
}
