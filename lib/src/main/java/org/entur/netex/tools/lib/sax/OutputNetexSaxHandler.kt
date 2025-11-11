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
) : NetexToolsSaxHandler(), LexicalHandler {

    private val inclusionStack: Stack<Boolean> = Stack()

    override fun startDocument() {
        fileWriter.startDocument()
    }

    fun shouldIncludeCurrentElement(): Boolean {
        return inclusionPolicy.shouldInclude(elementStack)
    }

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        super.startElement(uri, localName, qName, attributes)

        val ancestorsIncluded = if(inclusionStack.isNotEmpty()) inclusionStack.peek() else true
        val shouldIncludeCurrentElement = ancestorsIncluded && inclusionPolicy.shouldInclude(elementStack)
        inclusionStack.push(shouldIncludeCurrentElement)

        if (shouldIncludeCurrentElement) {
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

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        if (inclusionStack.peek()) {
            elementWriter.handleCharacters(ch, start, length, currentPath = currentPath())
        }
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        if (inclusionStack.peek()) {
            elementWriter.handleEndElement(uri, localName, qName, currentPath = currentPath())
        }
        if (inclusionStack.isNotEmpty()) {
            inclusionStack.pop()
        }
        super.endElement(uri, localName, qName)
    }

    override fun comment(ch: CharArray?, start: Int, length: Int) {
        if (shouldIncludeCurrentElement()) {
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
