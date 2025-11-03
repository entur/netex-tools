package org.entur.netex.tools.lib.output

import org.apache.commons.lang3.StringEscapeUtils
import org.xml.sax.Attributes

abstract class XMLElementWriter(
    private val content: StringBuilder,
    private val bufferedWhitespace: StringBuilder
) {
    protected fun write(text : String) {
        printCachedWhiteSpace()
        content.append(text)
    }

    private fun printCachedWhiteSpace() {
        if (bufferedWhitespace.isNotEmpty()) {
            content.append(bufferedWhitespace)
            bufferedWhitespace.clear()
        }
    }

    open fun writeStartElement(qName: String?) {
        write("<$qName>")
    }

    open fun writeStartElement(qName: String?, attributes: Attributes?) {
        write("<$qName")
        if(attributes != null) {
            for (i in 0..<attributes.length) {
                write(" ${attributes.getQName(i)}=\"${attributes.getValue(i)}\"")
            }
        }
        write(">")
    }

    open fun writeCharacters(ch: CharArray?, start: Int, length: Int) {
        val text = String(ch!!, start, length)
        if(text.isBlank()) {
            bufferedWhitespace.clear()
            bufferedWhitespace.append(text)
        } else {
            write(StringEscapeUtils.escapeXml11(text))
        }
    }

    open fun writeEndElement(qName: String?) {
        write("</$qName>")
    }
}