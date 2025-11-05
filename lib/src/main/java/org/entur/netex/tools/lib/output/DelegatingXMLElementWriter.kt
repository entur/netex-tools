package org.entur.netex.tools.lib.output

import org.xml.sax.Attributes

class DelegatingXMLElementWriter(
    val handlers: Map<String, XMLElementHandler>,
    xmlContext: XmlContext,
) {
    val xmlWriter = xmlContext.xmlWriter

    fun elementHandler(path: String): XMLElementHandler? = handlers[path]

    fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        xmlWriter.startElement(uri, localName, qName, attributes)
    }

    fun characters(ch: CharArray?, start: Int, length: Int) {
        val text = String(ch!!, start, length)
        if (!text.isBlank()) {
            xmlWriter.characters(ch, start, length)
        }
    }

    fun endElement(uri: String?, localName: String?, qName: String?) {
        xmlWriter.endElement(uri, localName, qName)
    }

    fun handleStartElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?, currentPath: String) {
        val handler = elementHandler(currentPath)
        if (handler != null) {
            handler.startElement(uri, localName, qName, attributes, this)
        } else {
            startElement(uri, localName, qName, attributes)
        }
    }

    fun handleCharacters(ch: CharArray?, start: Int, length: Int, currentPath: String) {
        val text = String(ch!!, start, length)
        val handler = elementHandler(currentPath)
        if (handler != null) {
            handler.characters(ch, start, length, this)
        } else if (!text.isBlank()) {
            characters(ch, start, length)
        }
    }

    fun handleEndElement(uri: String?, localName: String?, qName: String?, currentPath: String) {
        val handler = elementHandler(currentPath)
        if (handler != null) {
            handler.endElement(uri, localName, qName, this)
        } else {
            endElement(uri, localName, qName)
        }
    }
}