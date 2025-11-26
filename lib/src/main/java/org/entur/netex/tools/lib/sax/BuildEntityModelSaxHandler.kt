package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.extensions.hasAttribute
import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.Entity.Companion.EMPTY
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.Ref
import org.entur.netex.tools.lib.plugin.NetexPlugin
import org.entur.netex.tools.lib.plugin.PluginRegistry
import org.entur.netex.tools.lib.selections.InclusionPolicy
import org.xml.sax.Attributes
import java.io.File
import java.util.Stack

class BuildEntityModelSaxHandler(
    val entityModel : EntityModel,
    val inclusionPolicy: InclusionPolicy,
    val file: File,
    plugins: List<NetexPlugin> = emptyList(),
) : NetexToolsSaxHandler() {

    private val pluginRegistry = PluginRegistry()
    private val inclusionStack: Stack<Pair<Element, Boolean>> = Stack()

    init {
        plugins.forEach { plugin ->
            pluginRegistry.registerPlugin(plugin)
        }
    }

    private fun registerEntity(entity: Entity) {
        entityModel.addEntity(entity)
    }

    private fun registerRef(type: String, attributes: Attributes) {
        val ref = attributes.getValue("ref")
        val refObject = Ref(nn(type), currentEntity()!!, ref)
        entityModel.addRef(refObject)
    }

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        super.startElement(uri, localName, qName, attributes)

        val currentElement = currentElement()!!

        val shouldIncludeCurrentElement = inclusionPolicy.shouldInclude(currentElement, inclusionStack)
        inclusionStack.push(Pair(currentElement, shouldIncludeCurrentElement))

        if (shouldIncludeCurrentElement) {
            val isEntity = attributes?.hasAttribute("id") ?: false
            val isRef = attributes?.hasAttribute("ref") ?: false
            if (isEntity) {
                registerEntity(currentEntity()!!)
            } else if (isRef) {
                registerRef(type = qName!!, attributes = attributes)
            }

            pluginRegistry.getPluginsForElement(qName!!).forEach { plugin ->
                plugin.startElement(qName, attributes, currentEntity())
            }
        }
    }

    private fun shouldIncludeCurrentElement(): Boolean = inclusionStack.peek().second

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        if (shouldIncludeCurrentElement()) {
            val currentElementName = currentElement()?.name
            if (currentElementName != null) {
                pluginRegistry.getPluginsForElement(currentElementName).forEach { plugin ->
                    plugin.characters(currentElementName, ch, start, length)
                }
            }
        }
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        if (shouldIncludeCurrentElement()) {
            val currentElement = currentElement()
            if (currentElement?.name != null) {
                pluginRegistry.getPluginsForElement(currentElement.name).forEach { plugin ->
                    plugin.endElement(currentElement.name, currentEntity())
                }
            }
        }

        if (inclusionStack.isNotEmpty()) {
            inclusionStack.pop()
        }

        super.endElement(uri, localName, qName)
    }

    override fun endDocument() {
        pluginRegistry.getAllPlugins().forEach { it.endDocument(file) }
    }

    private fun nn(value : String?) = value ?: EMPTY
}
