package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.extensions.middle
import org.entur.netex.tools.lib.extensions.toMap
import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.output.Characters
import org.entur.netex.tools.lib.output.Comments
import org.entur.netex.tools.lib.output.EndElement
import org.entur.netex.tools.lib.output.StartElement
import org.xml.sax.Attributes

data class DeferredEventsBuffer(
    val events: MutableList<EventRecord> = mutableListOf(),
    var depth: Int = 0,
) {
    fun first() = events.first()
    fun middle() = events.middle()

    fun reachedEndOfDeferredElement(): Boolean = depth == 0 && events.isNotEmpty()

    fun deferStartElementEvent(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?,
        element: Element
    ) {
        events.add(
            EventRecord(
                event =
                    StartElement(
                        uri = uri,
                        localName = localName,
                        qName = qName,
                        attributes = attributes?.toMap()
                    ),
                element = element
            )
        )
        depth++
    }

    fun deferCharactersEvent(ch: CharArray?, start: Int, length: Int, element: Element) =
        events.add(
            EventRecord(
                event = Characters(
                    ch = ch,
                    start = start,
                    length = length
                ),
                element = element
            )
        )

    fun deferCommentsEvent(ch: CharArray?, start: Int, length: Int, element: Element) =
        events.add(
            EventRecord(
                event = Comments(
                    ch = ch,
                    start = start,
                    length = length
                ),
                element = element
            )
        )

    fun deferEndElementEvent(uri: String?, localName: String?, qName: String?, element: Element) {
        events.add(
            EventRecord(
                event = EndElement(
                    uri = uri,
                    localName = localName,
                    qName = qName,
                ),
                element = element
            )
        )
        depth--
    }

    fun flush(eventHandler: (EventRecord) -> Unit) {
        for (event in events) {
            eventHandler(event)
        }

        events.clear()
        depth = 0
    }
}