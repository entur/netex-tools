package org.entur.netex.tools.lib.output

import org.entur.netex.tools.lib.extensions.toAttributes
import org.entur.netex.tools.lib.extensions.toMap
import org.xml.sax.Attributes

/**
 * Simplifies deferring of SAX event handling. Used for cases where we cannot determine whether to write XML elements
 * before we reach the end of the element.
 **/
abstract class DeferredWriter(
    val fileWriter: NetexFileWriter,
    val deferredEvents: MutableList<Event> = mutableListOf(),
) {
    /**
     * Implement this function to decide whether or not to defer an event
     **/
    abstract fun shouldDeferWritingEvent(currentPath: String): Boolean

    /**
     * Implement this function to decide whether or not to write the deferred events when flush() is called
     **/
    abstract fun shouldWriteDeferredEvents(): Boolean

    private var depth = 0

    fun reachedEndOfDeferredRoot() = depth == 0 && deferredEvents.isNotEmpty()

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