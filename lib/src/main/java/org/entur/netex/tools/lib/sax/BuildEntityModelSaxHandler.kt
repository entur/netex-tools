package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.extensions.hasAttribute
import org.entur.netex.tools.lib.model.CompositeEntityId
import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.Entity.Companion.EMPTY
import org.entur.netex.tools.lib.model.EntityModel
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
    var currentEntity : Entity? = null
    var currentElement : Element? = null

    protected var elementBeingSkipped: String? = null

    private val elementStack = Stack<String>()

    private val pluginRegistry = PluginRegistry()

    init {
        plugins.forEach { plugin ->
            pluginRegistry.registerPlugin(plugin)
        }
    }

    private fun currentPath(): String {
        return "/" + elementStack.joinToString(separator = "/")
    }

    private fun createEntityId(type: String, attributes: Attributes): String {
        val id = attributes.getValue("id")
        return if (type == "DayTypeAssignment") {
            val version = attributes.getValue("version")
            val order = attributes.getValue("order")
            CompositeEntityId.ByIdVersionAndOrder(
                baseId = id,
                version = nn(version),
                order = nn(order)
            ).id
        } else id
    }

    private fun registerEntity(type: String, attributes: Attributes) {
        val publication = attributes.getValue("publication") ?: "public"

        val entity = Entity(
            id = createEntityId(type, attributes),
            type = type,
            publication = publication,
            parent = currentEntity,
        )
        currentEntity = entity

        entityModel.addEntity(entity)
    }

    private fun registerRef(type: String, attributes: Attributes) {
        val ref = attributes.getValue("ref")
        val refObject = Ref(nn(type), currentEntity!!, ref)
        entityModel.addRef(refObject)
    }

    private fun inSkipMode() = elementBeingSkipped != null

    private fun startSkippingCurrentElement() {
        elementBeingSkipped = currentPath()
    }

    private fun stopSkippingCurrentElement() {
        elementBeingSkipped = null
    }

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        elementStack.push(qName)
        val type = qName!!
        currentElement = Element(type, currentElement, attributes)

        if (inSkipMode()) {
            return
        }

        if (inclusionPolicy.shouldInclude(currentElement!!, currentPath())) {
            val isEntity = attributes?.hasAttribute("id") ?: false
            val isRef = attributes?.hasAttribute("ref") ?: false
            if (isEntity) {
                registerEntity(type, attributes)
            } else if (isRef) {
                registerRef(type, attributes)
            }

            pluginRegistry.getPluginsForElement(type).forEach { plugin ->
                plugin.startElement(type, attributes, currentEntity)
            }
        } else {
            startSkippingCurrentElement()
        }
    }

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        val currentElementName = currentElement?.name
        if (currentElementName != null) {
            pluginRegistry.getPluginsForElement(currentElementName).forEach { plugin ->
                plugin.characters(currentElementName, ch, start, length)
            }
        }
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        if (elementBeingSkipped == currentPath()) {
            stopSkippingCurrentElement()
        }

        elementStack.pop()

        val currentElementName = currentElement?.name
        if (currentElementName != null) {
            pluginRegistry.getPluginsForElement(currentElementName).forEach { plugin ->
                plugin.endElement(currentElementName, currentEntity)
            }
        }

        currentElement = currentElement?.parent


        if (currentEntity?.type == qName) {
            currentEntity = currentEntity?.parent
        }
    }

    override fun endDocument() {
        pluginRegistry.getAllPlugins().forEach { it.endDocument() }
    }

    private fun nn(value : String?) = value ?: EMPTY
}
