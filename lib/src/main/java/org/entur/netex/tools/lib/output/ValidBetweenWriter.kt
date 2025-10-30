package org.entur.netex.tools.lib.output

import org.xml.sax.Attributes

class ValidBetweenWriter(
    outputFileContent: StringBuilder,
    bufferedWhitespace: StringBuilder
): XMLElementWriter(
    outputFileContent,
    bufferedWhitespace
) {
    override fun writeStartElement(qName: String?) {
        super.writeStartElement("AvailabilityCondition")
    }

    override fun writeStartElement(qName: String?, attributes: Attributes?) {
        super.writeStartElement("AvailabilityCondition")
    }

    override fun writeEndElement(qName: String?) {
        super.writeEndElement("AvailabilityCondition")
    }
}