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

class OutputNetexSaxHandler private constructor(
    private val entityModel: EntityModel,
    private val fileIndex: FileIndex,
    private val inclusionPolicy: InclusionPolicy,
    private val outputFile: File,
    private val fileWriter: NetexFileWriter,
    private val elementWriter: DelegatingXMLElementWriter,
    elementsRequiredChildren: Map<String, List<String>>,
    private val documentName: String?,
) : NetexToolsSaxHandler(), LexicalHandler {

    constructor(
        entityModel: EntityModel,
        fileIndex: FileIndex,
        inclusionPolicy: InclusionPolicy,
        outputFile: File,
        fileWriter: NetexFileWriter,
        elementWriter: DelegatingXMLElementWriter,
        elementsRequiredChildren: Map<String, List<String>> = mapOf(),
    ) : this(entityModel, fileIndex, inclusionPolicy, outputFile, fileWriter, elementWriter, elementsRequiredChildren, null)

    constructor(
        entityModel: EntityModel,
        fileIndex: FileIndex,
        inclusionPolicy: InclusionPolicy,
        documentName: String,
        fileWriter: NetexFileWriter,
        elementWriter: DelegatingXMLElementWriter,
        elementsRequiredChildren: Map<String, List<String>> = mapOf(),
    ) : this(entityModel, fileIndex, inclusionPolicy, File(documentName), fileWriter, elementWriter, elementsRequiredChildren, documentName)

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
        val record = currentEventRecord ?: return false
        return shouldDefer(record)
    }

    fun shouldInclude(element: Element): Boolean {
        return inclusionPolicy.shouldInclude(element, inclusionStack)
    }

    private fun shouldIncludeCurrentElement(): Boolean = inclusionStack.peek().second

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        super.startElement(uri, localName, qName, attributes)

        val currentElement = currentElement() ?: return
        val shouldIncludeCurrentElement = shouldInclude(currentElement)
        inclusionStack.push(Pair(currentElement, shouldIncludeCurrentElement))

        if (!shouldIncludeCurrentElement) return

        val eventRecord = currentEventRecord ?: return
        if (shouldDefer(eventRecord)) {
            defer(eventRecord)
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
                if (documentName != null) {
                    fileIndex.add(entity, documentName)
                } else {
                    fileIndex.add(entity, outputFile)
                }
            }
        }
    }

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        super.characters(ch, start, length)

        if (!shouldIncludeCurrentElement()) return

        val eventRecord = currentEventRecord ?: return
        if (shouldDefer(eventRecord)) {
            defer(eventRecord)
        } else {
            val path = currentElement()?.path() ?: return
            elementWriter.handleCharacters(ch, start, length, path = path)
        }
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

        val element = currentElement() ?: return
        val endEventRecord = EventRecord(
            event = EndElement(
                uri = uri,
                localName = localName,
                qName = qName,
            ),
            element = element
        )

        if (shouldDefer(endEventRecord)) {
            defer(endEventRecord)
            if (eventBuffer.hasReachedEndOfBufferedElement()) {
                flushDeferredEvents()
            }
        } else {
            elementWriter.handleEndElement(uri, localName, qName, path = element.path())
        }

        goToNextElement(uri, localName, qName)
    }

    override fun comment(ch: CharArray?, start: Int, length: Int) {
        super.comments(ch, start, length)

        if (!shouldIncludeCurrentElement()) return

        val eventRecord = currentEventRecord ?: return
        if (shouldDefer(eventRecord)) {
            defer(eventRecord)
        } else {
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
