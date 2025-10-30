package org.entur.netex.tools.lib.output

import org.entur.netex.tools.lib.extensions.toISO8601
import org.xml.sax.Attributes
import java.time.LocalDate

class ValidBetweenToDateWriter(
    outputFileContent: StringBuilder,
    bufferedWhitespace: StringBuilder,
    private val toDate: LocalDate,
): XMLElementWriter(
    outputFileContent,
    bufferedWhitespace
) {
    private fun writeToDateXmlElement() {
        super.writeStartElement("ToDate")
        super.write(toDate.toISO8601())
        super.writeEndElement("ToDate")
    }

    override fun writeStartElement(qName: String?) = writeToDateXmlElement()
    override fun writeStartElement(qName: String?, attributes: Attributes?) = writeToDateXmlElement()
    override fun writeCharacters(ch: CharArray?, start: Int, length: Int) {}
    override fun writeEndElement(qName: String?) {}
}