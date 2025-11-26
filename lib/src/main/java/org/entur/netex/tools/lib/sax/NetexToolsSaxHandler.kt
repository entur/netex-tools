package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.extensions.toMap
import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityId
import org.entur.netex.tools.lib.output.Characters
import org.entur.netex.tools.lib.output.Comments
import org.entur.netex.tools.lib.output.StartElement
import org.entur.netex.tools.lib.utils.Log
import org.xml.sax.Attributes
import org.xml.sax.SAXParseException
import org.xml.sax.helpers.DefaultHandler
import java.util.Stack

abstract class NetexToolsSaxHandler : DefaultHandler() {
    val elementStack = Stack<Element>()
    val entityStack = Stack<Entity>()

    var currentEventRecord: EventRecord? = null

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        val currentElement = createCurrentElement(attributes, qName!!)

        elementStack.push(currentElement)
        currentEventRecord = EventRecord(
            event = StartElement(
                qName = qName,
                uri = uri,
                attributes = attributes?.toMap(),
                localName = localName
            ),
            element = currentElement
        )

        if (currentElement.isEntity()) {
            entityStack.push(createEntity(qName, attributes!!))
        }
    }

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        currentEventRecord = EventRecord(
            event = Characters(
                ch = ch,
                start = start,
                length = length,
            ),
            element = currentElement()!!
        )
    }

    fun comments(ch: CharArray?, start: Int, length: Int) {
        currentEventRecord = EventRecord(
            event = Comments(
                ch = ch,
                start = start,
                length = length,
            ),
            element = currentElement()!!
        )
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        if (currentElement()!!.isEntity()) {
            entityStack.pop()
        }

        if (elementStack.isNotEmpty()) {
            elementStack.pop()
        }
    }

    protected fun createEntity(type: String, attributes: Attributes): Entity {
        val currentEntity = currentEntity()
        val publication = attributes.getValue("publication") ?: "public"
        return Entity(
            id = EntityId.from(type, attributes),
            type = type,
            publication = publication,
            parent = currentEntity,
        )
    }

    protected fun currentElement(): Element? {
        if (elementStack.isNotEmpty()) {
            return elementStack.peek()
        }
        return null
    }

    protected fun currentEntity(): Entity? {
        if (entityStack.isNotEmpty()) {
            return entityStack.peek()
        }
        return null
    }

    protected fun createCurrentElement(attributes: Attributes?, qName: String): Element {
        val parent = currentElement()
        if (attributes?.getValue("id") != null) {
            val attributesAsMap = attributes.toMap()
            val id = EntityId.from(type = qName, attributes = attributes)
            return Element(qName, parent, attributesAsMap, id)
        } else {
            // not an entity. Use parent's currentEntityId
            val attributesAsMap = attributes?.toMap() ?: mapOf()
            return Element(qName, parent, attributesAsMap, currentEntity()?.id)
        }
    }

    override fun warning(e: SAXParseException?) = Log.warn(e?.message, e)

    override fun error(e: SAXParseException?) = Log.error(e?.message, e)

    override fun fatalError(e: SAXParseException?) = Log.fatal(e?.message, e)

}