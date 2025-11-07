package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.extensions.toMap
import org.entur.netex.tools.lib.model.CompositeEntityId
import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.Entity.Companion.EMPTY
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.NetexTypes
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

    private val elementStack = Stack<Element>()
    private val entityStack = Stack<Entity>()

    override fun startDocument() {
        fileWriter.startDocument()
    }

    private fun currentPath(): String {
        return "/" + elementStack.joinToString(separator = "/") { it.name }
    }

    protected fun createCurrentElement(attributes: Attributes?, qName: String): Element {
        if (attributes?.getValue("id") != null) {
            val attributesAsMap = attributes.toMap()
            val id = getIdByQNameAndAttributes(qName = qName, attributes = attributesAsMap)
            return Element(qName, null, attributesAsMap, id)
        } else {
            // not an entity. Use parent's currentEntityId
            val attributesAsMap = attributes?.toMap() ?: mapOf()
            return Element(qName, null, attributesAsMap, currentEntity()?.id)
        }
    }

    protected fun getIdByQNameAndAttributes(qName: String, attributes: Map<String, String>): String? {
        return when (qName) {
            "DayTypeAssignment", "PassengerStopAssignment" -> {
                val version = attributes.getValue("version")
                val order = attributes.getValue("order")
                val id = attributes.getValue("id")
                CompositeEntityId.ByIdVersionAndOrder(id, version, order).id
            }
            else -> attributes.getValue("id")
        }
    }

    private fun currentEntity(): Entity? {
        if (entityStack.isNotEmpty()) {
            return entityStack.peek()
        }
        return null
    }

    fun shouldIncludeCurrentElement(): Boolean {
        return inclusionPolicy.shouldInclude(elementStack)
    }

    private fun createEntityId(type: String, attributes: Attributes): String {
        val id = attributes.getValue("id")
        return if (type == NetexTypes.DAY_TYPE_ASSIGNMENT || type == NetexTypes.PASSENGER_STOP_ASSIGNMENT) {
            val version = attributes.getValue("version")
            val order = attributes.getValue("order")
            CompositeEntityId.ByIdVersionAndOrder(
                baseId = id,
                version = nn(version),
                order = nn(order)
            ).id
        } else id
    }
    private fun nn(value : String?) = value ?: EMPTY

    private fun createEntity(type: String, attributes: Attributes): Entity {
        val publication = attributes.getValue("publication") ?: "public"

        return Entity(
            id = createEntityId(type, attributes),
            type = type,
            publication = publication,
            parent = currentEntity(),
        )
    }

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        val currentElement = createCurrentElement(attributes, qName!!)
        elementStack.push(currentElement)

        if (shouldIncludeCurrentElement()) {
            elementWriter.handleStartElement(
                uri = uri,
                localName = localName,
                attributes = attributes,
                qName = qName,
                currentPath = currentPath(),
            )
            indexElementIfEntity(currentElement)
        }

        if (currentElement.isEntity()) {
            entityStack.push(createEntity(qName, attributes!!))
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
        if (shouldIncludeCurrentElement()) {
            elementWriter.handleCharacters(ch, start, length, currentPath = currentPath())
        }
    }

    private fun currentElement(): Element? {
        if (elementStack.isNotEmpty()) {
            return elementStack.peek()
        }
        return null
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        if (shouldIncludeCurrentElement()) {
            elementWriter.handleEndElement(uri, localName, qName, currentPath = currentPath())
        }

        if (currentElement()?.isEntity() == true) {
            entityStack.pop()
        }

        if (elementStack.isNotEmpty()) {
            elementStack.pop()
        }
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
