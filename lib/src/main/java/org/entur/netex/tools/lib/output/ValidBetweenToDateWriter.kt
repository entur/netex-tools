package org.entur.netex.tools.lib.output

import org.xml.sax.Attributes

class ValidBetweenToDateWriter(
    outputFileContent: StringBuilder,
    bufferedWhitespace: StringBuilder,
): XMLElementWriter(
    outputFileContent,
    bufferedWhitespace
) {
    override fun writeCharacters(ch: CharArray?, start: Int, length: Int) {
        super.writeStartElement("AvailabilityCondition")
    }
}