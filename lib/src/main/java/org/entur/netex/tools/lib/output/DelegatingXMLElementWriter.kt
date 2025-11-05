package org.entur.netex.tools.lib.output

import org.apache.commons.lang3.StringEscapeUtils.escapeXml11
import org.entur.netex.tools.lib.plugin.NetexFileWriterContext

class DelegatingXMLElementWriter(
    val handlers: Map<String, XMLElementHandler>,
    netexFileWriterContext: NetexFileWriterContext
) {
    val outputFileContent = netexFileWriterContext.outputFileContent
    val bufferedWhitespace = netexFileWriterContext.bufferedWhitespace

    fun elementHandler(path: String): XMLElementHandler? = handlers[path]

    fun write(text : String) {
        printCachedWhiteSpace()
        outputFileContent.append(text)
    }

    private fun printCachedWhiteSpace() {
        if (bufferedWhitespace.isNotEmpty()) {
            outputFileContent.append(bufferedWhitespace)
            bufferedWhitespace.clear()
        }
    }

    fun writeStartElement(
        type: String,
        attributes: Map<String, String>,
        currentPath: String,
    ) {
        val handler = elementHandler(currentPath)
        val startTag = if (handler != null) {
            handler.onEnterElement(
                type = type,
                attributes = attributes
            )
        } else {
            XMLElementRenderer.startElement(
                type = type,
                attributes = attributes
            )
        }
        write(text = startTag)
    }

    fun writeCharacters(ch: CharArray?, start: Int, length: Int, currentPath: String) {
        val text = String(ch!!, start, length)
        if (text.isBlank()) {
            bufferedWhitespace.clear()contentEquals
            bufferedWhitespace.append(text)
            return
        }

        val handler = elementHandler(currentPath)
        val content = if (handler != null) {
            handler.onText(text)
        } else {
            XMLElementRenderer.characters(ch, start, length)
        }
//        write(text = escapeXml11(content))
        write(text = content)
    }

    fun writeEndElement(type: String, currentPath: String) {
        val handler = elementHandler(currentPath)
        val endTag = if (handler != null) {
            handler.onLeaveElement(type = type)
        } else {
            XMLElementRenderer.endElement(type = type)
        }
        write(text = endTag)
    }
}