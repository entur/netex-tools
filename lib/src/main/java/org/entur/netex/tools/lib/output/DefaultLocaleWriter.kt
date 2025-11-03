package org.entur.netex.tools.lib.output

import org.xml.sax.Attributes

class DefaultLocaleWriter(
    outputFileContent: StringBuilder,
    bufferedWhitespace: StringBuilder
): XMLElementWriter(
    outputFileContent,
    bufferedWhitespace
) {
    private fun writeDefaultLocale() {
        super.writeStartElement("DefaultLocale")

        super.write("\n\t\t  <TimeZone>Europe/Oslo</TimeZone>")
        super.write("\n\t\t  <DefaultLanguage>no</DefaultLanguage>")
    }

    override fun writeStartElement(qName: String?) = writeDefaultLocale()
    override fun writeStartElement(qName: String?, attributes: Attributes?) = writeDefaultLocale()

    override fun writeEndElement(qName: String?) {
        super.writeEndElement("DefaultLocale")
    }
}