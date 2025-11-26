package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.output.Comments
import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.entur.netex.tools.lib.output.EndElement
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

    val parentsWithRequiredChildrenDeferringRule = ParentsWithRequiredChildrenDeferEventsRule(
        parentsWithRequiredChildren = elementsRequiredChildren
    )

    private val inclusionStack: Stack<Pair<Element, Boolean>> = Stack()
    private val eventBuffer: EventBuffer = EventBuffer()

    override fun startDocument() {
        fileWriter.startDocument()
    }

    fun defer(eventRecord: EventRecord) = eventBuffer.add(eventRecord)

    fun shouldDefer(eventRecord: EventRecord): Boolean {
        return parentsWithRequiredChildrenDeferringRule.shouldDefer(eventRecord, eventBuffer)
    }

    fun shouldDeferCurrentEvent(): Boolean {
        return shouldDefer(currentEventRecord!!)
    }

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        super.startElement(uri, localName, qName, attributes)

        val currentElement = currentElement()!!

        val shouldIncludeCurrentElement = inclusionPolicy.shouldInclude(currentElement, inclusionStack)
        inclusionStack.push(Pair(currentElement, shouldIncludeCurrentElement))

        if (!shouldIncludeCurrentElement) return

        if (shouldDeferCurrentEvent()) {
            defer(currentEventRecord!!)
        } else {
            elementWriter.handleStartElement(
                uri = uri,
                localName = localName,
                attributes = attributes,
                qName = qName,
                path = currentElement.path(),
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
        super.characters(ch, start, length)

        if (!shouldIncludeCurrentElement()) return

        if (shouldDeferCurrentEvent()) {
            defer(currentEventRecord!!)
        } else {
            elementWriter.handleCharacters(ch, start, length, path = currentElement()!!.path())
        }
    }

    private fun goToNextElement(uri: String?, localName: String?, qName: String?) {
        if (inclusionStack.isNotEmpty()) {
            inclusionStack.pop()
        }
        super.endElement(uri, localName, qName)
    }

    fun flushDeferredEvents() {
        val shouldWriteElement = parentsWithRequiredChildrenDeferringRule.shouldHandleDeferredEvents(eventBuffer)
        eventBuffer.flush {
            if (shouldWriteElement) {
                if (it.event is Comments) {
                    fileWriter.writeComments(
                        it.event.ch, it.event.start, it.event.length
                    )
                } else {
                    if (it.event is StartElement) {
                        indexElementIfEntity(it.element)
                    }
                    elementWriter.write(it)
                }
            }
        }
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        if (!shouldIncludeCurrentElement()) {
            goToNextElement(uri, localName, qName)
            return
        }

        val currentEventRecord = EventRecord(
            event = EndElement(
                uri = uri,
                localName = localName,
                qName = qName,
            ),
            element = currentElement()!!
        )

        if (shouldDefer(currentEventRecord)) {
            defer(currentEventRecord)
            if (eventBuffer.hasReachedEndOfBufferedElement()) {
                flushDeferredEvents()
            }
        } else {
            elementWriter.handleEndElement(uri, localName, qName, path = currentElement()!!.path())
        }

        goToNextElement(uri, localName, qName)
    }

    override fun comment(ch: CharArray?, start: Int, length: Int) {
        super.comments(ch, start, length)

        if (!shouldIncludeCurrentElement()) return

        if (shouldDeferCurrentEvent()) {
            defer(currentEventRecord!!)
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
