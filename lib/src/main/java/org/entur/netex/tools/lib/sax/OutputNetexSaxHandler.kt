package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.extensions.toMap
import org.entur.netex.tools.lib.model.CompositeEntityId
import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.plugin.DefaultNetexFileWriter
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
    private val netexFileWriter: DefaultNetexFileWriter,
) : NetexToolsSaxHandler(), LexicalHandler {
    protected var currentElement : Element? = null
    protected var inSkipMode: Boolean = false

    private val elementStack = Stack<String>()

    protected fun startSkipMode() {
        inSkipMode = true
    }

    protected fun endSkipMode() {
        inSkipMode = false
    }

    override fun startDocument() {
        netexFileWriter.startDocument()
    }

    private fun currentPath(): String {
        return "/" + elementStack.joinToString(separator = "/")
    }

    protected fun updateCurrentElement(attributes: Attributes?, qName: String) {
        if (attributes?.getValue("id") != null) {
            val attributesAsMap = attributes.toMap()
            val id = getIdByQNameAndAttributes(qName = qName, attributes = attributesAsMap)
            currentElement = Element(qName, currentElement, attributesAsMap, id)
        } else {
            // not an entity. Use parent's currentEntityId
            val attributesAsMap = attributes?.toMap() ?: mapOf()
            currentElement = Element(qName, currentElement, attributesAsMap, currentElement?.currentEntityId)
        }
    }

    protected fun getIdByQNameAndAttributes(qName: String, attributes: Map<String, String>): String? {
        return when (qName) {
            "DayTypeAssignment" -> {
                val version = attributes.getValue("version")
                val order = attributes.getValue("order")
                val id = attributes.getValue("id")
                CompositeEntityId.ByIdVersionAndOrder(id, version, order).id
            }
            else -> attributes.getValue("id")
        }
    }

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        elementStack.push(qName)
        updateCurrentElement(attributes, qName!!)

        if (!inSkipMode) {
            val element = currentElement!!
            val elementShouldBeIncluded = inclusionPolicy.shouldInclude(element, currentPath())

            if (elementShouldBeIncluded) {
                netexFileWriter.writeStartElement(
                    qName = qName,
                    attributes = attributes,
                )
                indexElementIfEntity(element)
            } else {
                startSkipMode()
            }
        }
    }

    private fun indexElementIfEntity(element: Element) {
        if (element.isEntity()) {
            val entity = entityModel.getEntity(element)
            if (entity != null) {
                fileIndex.add(entity, outputFile)
            }
        }
    }

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        if(inSkipMode) {
            return
        }

        netexFileWriter.writeCharacters(ch, start, length)
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        if (elementStack.isNotEmpty()) {
            elementStack.pop()
        }

        if (!inSkipMode) {
            netexFileWriter.writeEndElement(qName)
        }

        val parent = currentElement?.parent
        if (parent == null || inclusionPolicy.shouldInclude(parent, currentPath())) {
            endSkipMode()
        }

        currentElement = currentElement?.parent
    }

    override fun comment(ch: CharArray?, start: Int, length: Int) {
        if(inSkipMode) {
            return
        }

        netexFileWriter.writeComments(ch, start, length)
    }

    override fun endDocument() {
        netexFileWriter.endDocument()
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
