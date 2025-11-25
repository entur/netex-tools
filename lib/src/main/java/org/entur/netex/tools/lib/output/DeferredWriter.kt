package org.entur.netex.tools.lib.output

import org.entur.netex.tools.lib.extensions.toAttributes
import org.entur.netex.tools.lib.extensions.toMap
import org.xml.sax.Attributes

abstract class DeferredWriter(
    val fileWriter: NetexFileWriter,
    val deferredEvents: MutableList<Event> = mutableListOf(),
) {
    abstract fun shouldDeferWritingEvent(currentPath: String): Boolean
    abstract fun shouldWriteDeferredEvents(): Boolean

    private var depth = 0

    fun rootTagIsClosed() = depth == 0 && deferredEvents.isNotEmpty()

    fun write(event: Event, writer: DelegatingXMLElementWriter) {
        when (event) {
            is StartElement ->
                writer.startElement(
                    uri = event.uri,
                    localName = event.localName,
                    qName = event.qName,
                    attributes = event.attributes?.toAttributes()
                )

            is Characters ->
                writer.characters(
                    ch = event.ch,
                    start = event.start,
                    length = event.length,
                )

            is EndElement ->
                writer.endElement(
                    uri = event.uri,
                    localName = event.localName,
                    qName = event.qName,
                )

            is Comments ->
                fileWriter.writeComments(
                    ch = event.ch,
                    start = event.start,
                    length = event.length,
                )
        }
    }

    fun deferStartElementEvent(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        deferredEvents.add(
            StartElement(
                uri = uri,
                localName = localName,
                qName = qName,
                attributes = attributes?.toMap()
            )
        )
        depth++
    }

    fun deferCharactersEvent(ch: CharArray?, start: Int, length: Int) =
        deferredEvents.add(
            Characters(
                ch = ch,
                start = start,
                length = length
            )
        )

    fun deferCommentsEvent(ch: CharArray?, start: Int, length: Int) =
        deferredEvents.add(
            Comments(
                ch = ch,
                start = start,
                length = length
            )
        )

    fun deferEndElementEvent(uri: String?, localName: String?, qName: String?) {
        deferredEvents.add(
            EndElement(
                uri = uri,
                localName = localName,
                qName = qName,
            )
        )
        depth--
    }
    
    fun flush(xmlWriter: DelegatingXMLElementWriter) {
        if (shouldWriteDeferredEvents()) {
            for (event in deferredEvents) {
                write(event, xmlWriter)
            }
        }
        deferredEvents.clear()
        depth = 0
    }
}