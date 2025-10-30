package org.entur.netex.tools.lib.output

import org.xml.sax.Attributes

/**
 * Writer for skipping all the contents of a certain element
 **/
class SkipElementWriter(
    outputFileContent: StringBuilder,
    bufferedWhitespace: StringBuilder
): XMLElementWriter(
    content = outputFileContent,
    bufferedWhitespace = bufferedWhitespace
) {
    override fun writeStartElement(qName: String?) {
        // should write nothing
    }

    override fun writeStartElement(qName: String?, attributes: Attributes?) {
        // should write nothing
    }

    override fun writeCharacters(ch: CharArray?, start: Int, length: Int) {
        // should write nothing
    }

    override fun writeEndElement(qName: String?) {
        // should write nothing
    }
}