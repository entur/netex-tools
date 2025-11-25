package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.extensions.toAttributes
import org.entur.netex.tools.lib.extensions.toMap
import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.output.Characters
import org.entur.netex.tools.lib.output.Comments
import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.entur.netex.tools.lib.output.EndElement
import org.entur.netex.tools.lib.output.Event
import org.entur.netex.tools.lib.output.NetexFileWriter
import org.entur.netex.tools.lib.output.StartElement
import org.entur.netex.tools.lib.report.FileIndex
import org.entur.netex.tools.lib.selections.InclusionPolicy
import org.xml.sax.Attributes
import org.xml.sax.ext.LexicalHandler
import java.io.File
import java.util.Stack

class OutputNetexSaxHandler(
    private val entityModel: EntityModel,
    private val fileIndex: FileIndex,
    private val inclusionPolicy: InclusionPolicy,
    private val outputFile: File,
    private val fileWriter: NetexFileWriter,
    private val elementWriter: DelegatingXMLElementWriter,
    elementsRequiredChildren: Map<String, List<String>> = mapOf()
) : NetexToolsSaxHandler(), LexicalHandler {

    val parentsWithRequiredChildrenDeferringRule = ParentsWithRequiredChildrenDeferringRule(
        parentsWithRequiredChildren = elementsRequiredChildren
    )

    fun write(event: Event) {
        when (event) {
            is StartElement -> {
                elementWriter.startElement(
                    uri = event.uri,
                    localName = event.localName,
                    qName = event.qName,
                    attributes = event.attributes?.toAttributes()
                )
                indexElementIfEntity(currentElement)
            }

            is Characters ->
                elementWriter.characters(
                    ch = event.ch,
                    start = event.start,
                    length = event.length,
                )

            is EndElement ->
                elementWriter.endElement(
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

    private val inclusionStack: Stack<Pair<Element, Boolean>> = Stack()
    private val eventBuffer: DeferredEventsBuffer = DeferredEventsBuffer()

    override fun startDocument() {
        fileWriter.startDocument()
    }

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        super.startElement(uri, localName, qName, attributes)

        val shouldIncludeCurrentElement = inclusionPolicy.shouldInclude(currentElement!!, inclusionStack)
        inclusionStack.push(Pair(currentElement!!, shouldIncludeCurrentElement))

        val eventRecord = EventRecord(
            StartElement(
                uri = uri,
                localName = localName,
                qName = qName,
                attributes = attributes?.toMap()
            ),
            element = currentElement!!,
        )

        if (!shouldIncludeCurrentElement) return

        if (parentsWithRequiredChildrenDeferringRule.shouldDefer(eventRecord, eventBuffer)) {
            eventBuffer.deferStartElementEvent(
                uri = uri,
                localName = localName,
                attributes = attributes,
                qName = qName,
                element = currentElement!!
            )
        } else {
            elementWriter.handleStartElement(
                uri = uri,
                localName = localName,
                attributes = attributes,
                qName = qName,
                currentPath = currentPath(),
            )
            indexElementIfEntity(currentElement)
        }
    }

    private fun indexElementIfEntity(element: Element?) {
        if (element?.isEntity() == true) {
            val entity = entityModel.getEntity(element)
            if (entity != null) {
                fileIndex.add(entity, outputFile)
            }
        }
    }

    private fun shouldIncludeCurrentElement(): Boolean = inclusionStack.peek().second

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        if (!shouldIncludeCurrentElement()) return

        val eventRecord = EventRecord(
            Characters(
                ch = ch,
                start = start,
                length = length,
            ),
            element = currentElement!!,
        )
        if (parentsWithRequiredChildrenDeferringRule.shouldDefer(eventRecord, eventBuffer)) {
            eventBuffer.deferCharactersEvent(ch, start, length, currentElement!!)
        } else {
            elementWriter.handleCharacters(ch, start, length, currentPath = currentPath())
        }
    }

    private fun goToNextElement(uri: String?, localName: String?, qName: String?) {
        if (inclusionStack.isNotEmpty()) {
            inclusionStack.pop()
        }
        super.endElement(uri, localName, qName)
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        if (!shouldIncludeCurrentElement()) {
            goToNextElement(uri, localName, qName)
            return
        }

        val eventRecord = EventRecord(
            event = EndElement(
                uri = uri,
                localName = localName,
                qName = qName,
            ),
            element = currentElement!!,
        )

        if (parentsWithRequiredChildrenDeferringRule.shouldDefer(eventRecord, eventBuffer)) {
            eventBuffer.deferEndElementEvent(
                uri = uri,
                localName = localName,
                qName = qName,
                element = currentElement!!
            )

            if (eventBuffer.reachedEndOfDeferredElement()) {
                val shouldWriteElement = parentsWithRequiredChildrenDeferringRule.shouldHandleDeferredEvents(eventBuffer)
                eventBuffer.flush {
                    if (shouldWriteElement) {
                        write(it.event)
                    }
                }
            }
        } else {
            elementWriter.handleEndElement(uri, localName, qName, currentPath = currentPath())
        }

        goToNextElement(uri, localName, qName)
    }

    override fun comment(ch: CharArray?, start: Int, length: Int) {
        if (!shouldIncludeCurrentElement()) return

        val eventRecord = EventRecord(
            event = Comments(
                ch = ch,
                start = start,
                length = length,
            ),
            element = currentElement!!,
        )

        if (parentsWithRequiredChildrenDeferringRule.shouldDefer(eventRecord, eventBuffer)) {
            eventBuffer.deferCommentsEvent(
                ch = ch,
                start = start,
                length = length,
                element = currentElement!!
            )
        }
        else {
            fileWriter.writeComments(ch, start, length)
        }
    }

    override fun endDocument() {
        fileWriter.endDocument()
    }

    // LexicalHandler methods for comment preservation
    override fun startDTD(name: String?, publicId: String?, systemId: String?) {
        // Not needed for NeTEx files
    }

    override fun endDTD() {
        // Not needed for NeTEx files
    }

    override fun startEntity(name: String?) {
        // Not needed for NeTEx files
    }

    override fun endEntity(name: String?) {
        // Not needed for NeTEx files
    }

    override fun startCDATA() {
        // Not needed for NeTEx files
    }

    override fun endCDATA() {
        // Not needed for NeTEx files
    }
}
