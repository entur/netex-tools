package org.entur.netex.tools.lib.output

import org.xml.sax.Attributes
import org.xml.sax.helpers.AttributesImpl

class ValidBetweenWriter(
    outputFileContent: StringBuilder,
    bufferedWhitespace: StringBuilder
): XMLElementWriter(
    outputFileContent,
    bufferedWhitespace
) {
    private fun writeAvailabilityConditionStartTag() {
        val attrs = AttributesImpl()
        val id = NetexIdGenerator.next("NWY", "AvailabilityCondition")
        attrs.addAttribute("", "version", "version", "CDATA", "1")
        attrs.addAttribute("", "id", "id", "CDATA", id)
        super.writeStartElement("AvailabilityCondition", attrs)
    }

    override fun writeStartElement(qName: String?) = writeAvailabilityConditionStartTag()
    override fun writeStartElement(qName: String?, attributes: Attributes?) = writeAvailabilityConditionStartTag()

    override fun writeEndElement(qName: String?) {
        super.writeEndElement("AvailabilityCondition")
    }
}