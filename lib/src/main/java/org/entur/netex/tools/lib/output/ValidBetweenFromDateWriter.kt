package org.entur.netex.tools.lib.output

import org.entur.netex.tools.lib.extensions.toISO8601
import org.xml.sax.Attributes
import java.time.LocalDate

class ValidBetweenFromDateWriter(
    outputFileContent: StringBuilder,
    bufferedWhitespace: StringBuilder,
    private val fromDate: LocalDate,
): XMLElementWriter(
    outputFileContent,
    bufferedWhitespace
) {
    private fun writeFromDateXmlElement() {
        super.writeStartElement("FromDate")
        super.write(fromDate.toISO8601())
        super.writeEndElement("FromDate")
    }

    override fun writeStartElement(qName: String?) = writeFromDateXmlElement()
    override fun writeStartElement(qName: String?, attributes: Attributes?) = writeFromDateXmlElement()

    override fun writeCharacters(ch: CharArray?, start: Int, length: Int) {}
    override fun writeEndElement(qName: String?) {}
}