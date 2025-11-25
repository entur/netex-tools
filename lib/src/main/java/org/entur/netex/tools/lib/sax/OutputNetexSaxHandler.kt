package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.entur.netex.tools.lib.output.NetexFileWriter
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

    val deferredWriter = RequiredChildrenWriter(elementsRequiredChildren, fileWriter)

    private val inclusionStack: Stack<Pair<Element, Boolean>> = Stack()
    private val deferStack: Stack<Pair<Element, Boolean>> = Stack()

    override fun startDocument() {
        fileWriter.startDocument()
    }

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        super.startElement(uri, localName, qName, attributes)

        val shouldIncludeCurrentElement = inclusionPolicy.shouldInclude(currentElement!!, inclusionStack)
        inclusionStack.push(Pair(currentElement!!, shouldIncludeCurrentElement))

        val shouldDeferCurrentEvent = deferredWriter.shouldDeferWritingEvent(currentPath())
        deferStack.push(Pair(currentElement!!, shouldDeferCurrentEvent))

        if (!shouldIncludeCurrentElement) return

        if (shouldDeferCurrentEvent) {
            deferredWriter.deferStartElementEvent(
                uri = uri,
                localName = localName,
                attributes = attributes,
                qName = qName,
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
    private fun shouldDeferCurrentElement(): Boolean = deferStack.peek().second

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        if (!shouldIncludeCurrentElement()) return

        if (shouldDeferCurrentElement()) {
            deferredWriter.deferCharactersEvent(ch, start, length)
        } else {
            elementWriter.handleCharacters(ch, start, length, currentPath = currentPath())
        }
    }

    private fun goToNextElement(uri: String?, localName: String?, qName: String?) {
        if (inclusionStack.isNotEmpty()) {
            inclusionStack.pop()
            deferStack.pop()
        }
        super.endElement(uri, localName, qName)
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        if (!shouldIncludeCurrentElement()) {
            goToNextElement(uri, localName, qName)
            return
        }

        if (shouldDeferCurrentElement()) {
            deferredWriter.deferEndElementEvent(
                uri = uri,
                localName = localName,
                qName = qName,
            )

            if (deferredWriter.reachedEndOfDeferredRoot()) {
                deferredWriter.flush(elementWriter)
            }
        } else {
            elementWriter.handleEndElement(uri, localName, qName, currentPath = currentPath())
        }

        goToNextElement(uri, localName, qName)
    }

    override fun comment(ch: CharArray?, start: Int, length: Int) {
        if (!shouldIncludeCurrentElement()) return

        if (shouldDeferCurrentElement()) {
            deferredWriter.deferCommentsEvent(ch, start, length)
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
