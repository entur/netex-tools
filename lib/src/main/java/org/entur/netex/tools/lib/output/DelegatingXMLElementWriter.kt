package org.entur.netex.tools.lib.output

import org.entur.netex.tools.lib.extensions.toAttributes
import org.entur.netex.tools.lib.sax.EventRecord
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
        val chars = ch ?: return
        val text = String(chars, start, length)
        if (!text.isBlank()) {
            xmlWriter.characters(chars, start, length)
        }
    }

    fun endElement(uri: String?, localName: String?, qName: String?) {
        xmlWriter.endElement(uri, localName, qName)
    }

    fun startPrefixMapping(prefix: String, uri: String) {
        xmlWriter.startPrefixMapping(prefix, uri)
    }

    fun endPrefixMapping(prefix: String) {
        xmlWriter.endPrefixMapping(prefix)
    }

    fun handleStartElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?, path: String) {
        val handler = elementHandler(path)
        if (handler != null) {
            handler.startElement(uri, localName, qName, attributes, this)
            handler.afterStartElement(uri, localName, qName, this)
        } else {
            startElement(uri, localName, qName, attributes)
        }
    }

    fun handleCharacters(ch: CharArray?, start: Int, length: Int, path: String) {
        val chars = ch ?: return
        val text = String(chars, start, length)
        val handler = elementHandler(path)
        if (handler != null) {
            handler.characters(chars, start, length, this)
        } else if (!text.isBlank()) {
            characters(chars, start, length)
        }
    }

    fun handleEndElement(uri: String?, localName: String?, qName: String?, path: String) {
        val handler = elementHandler(path)
        if (handler != null) {
            handler.beforeEndElement(uri, localName, qName, this)
            handler.endElement(uri, localName, qName, this)
        } else {
            endElement(uri, localName, qName)
        }
    }

    fun write(eventRecord: EventRecord) {
        val event = eventRecord.event
        val path = eventRecord.element.path()
        when (event) {
            is StartElement -> {
                handleStartElement(
                    uri = event.uri,
                    localName = event.localName,
                    qName = event.qName,
                    attributes = event.attributes?.toAttributes(),
                    path = path
                )
            }

            is Characters ->
                handleCharacters(
                    ch = event.ch,
                    start = event.start,
                    length = event.length,
                    path = path
                )

            is EndElement ->
                handleEndElement(
                    uri = event.uri,
                    localName = event.localName,
                    qName = event.qName,
                    path = path
                )
            is Comments -> {
                // do nothing
            }
        }
    }
}