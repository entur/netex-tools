package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.extensions.hasAttribute
import org.entur.netex.tools.lib.extensions.toMap
import org.entur.netex.tools.lib.model.CompositeEntityId
import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.Entity.Companion.EMPTY
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.model.Ref
import org.entur.netex.tools.lib.plugin.NetexPlugin
import org.entur.netex.tools.lib.plugin.PluginRegistry
import org.entur.netex.tools.lib.selections.InclusionPolicy
import org.xml.sax.Attributes
import java.util.Stack

class BuildEntityModelSaxHandler(
    val entityModel : EntityModel,
    val inclusionPolicy: InclusionPolicy,
    plugins: List<NetexPlugin> = emptyList(),
) : NetexToolsSaxHandler() {
    private val elementStack = Stack<Element>()
    private val entityStack = Stack<Entity>()

    private val pluginRegistry = PluginRegistry()

    init {
        plugins.forEach { plugin ->
            pluginRegistry.registerPlugin(plugin)
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

    private fun currentEntity(): Entity? {
        if (entityStack.isNotEmpty()) {
            return entityStack.peek()
        }
        return null
    }

    private fun currentElement(): Element? {
        if (elementStack.isNotEmpty()) {
            return elementStack.peek()
        }
        return null
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

    private fun createEntity(type: String, attributes: Attributes): Entity {
        val publication = attributes.getValue("publication") ?: "public"

        return Entity(
            id = createEntityId(type, attributes),
            type = type,
            publication = publication,
            parent = currentEntity(),
        )
    }

    private fun registerEntity(entity: Entity) {
        entityModel.addEntity(entity)
        entityStack.push(entity)
    }

    private fun registerRef(type: String, attributes: Attributes) {
        val ref = attributes.getValue("ref")
        val refObject = Ref(nn(type), currentEntity()!!, ref)
        entityModel.addRef(refObject)
    }

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        val currentElement = createCurrentElement(attributes, qName!!)
        elementStack.push(currentElement)

        if (inclusionPolicy.shouldInclude(elementStack)) {
            val isEntity = attributes?.hasAttribute("id") ?: false
            val isRef = attributes?.hasAttribute("ref") ?: false
            if (isEntity) {
                val entity = createEntity(qName, attributes)
                registerEntity(entity)
            } else if (isRef) {
                registerRef(qName, attributes)
            }

            pluginRegistry.getPluginsForElement(qName).forEach { plugin ->
                plugin.startElement(qName, attributes, currentEntity())
            }
        }
    }

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        if (!inclusionPolicy.shouldInclude(elementStack)) {
            return
        }

        val currentElementName = currentElement()?.name
        if (currentElementName != null) {
            pluginRegistry.getPluginsForElement(currentElementName).forEach { plugin ->
                plugin.characters(currentElementName, ch, start, length)
            }
        }
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        if (inclusionPolicy.shouldInclude(elementStack)) {
            val currentElement = currentElement()
            if (currentElement?.name != null) {
                pluginRegistry.getPluginsForElement(currentElement.name).forEach { plugin ->
                    plugin.endElement(currentElement.name, currentEntity())
                }
            }
        }

        if (currentElement()?.isEntity() == true && entityStack.isNotEmpty()) {
            entityStack.pop()
        }
        elementStack.pop()
    }

    override fun endDocument() {
        pluginRegistry.getAllPlugins().forEach { it.endDocument() }
    }

    private fun nn(value : String?) = value ?: EMPTY
}
