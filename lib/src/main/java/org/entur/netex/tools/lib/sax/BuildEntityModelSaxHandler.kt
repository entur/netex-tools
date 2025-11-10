package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.extensions.hasAttribute
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.Entity.Companion.EMPTY
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.Ref
import org.entur.netex.tools.lib.plugin.NetexPlugin
import org.entur.netex.tools.lib.plugin.PluginRegistry
import org.entur.netex.tools.lib.selections.InclusionPolicy
import org.xml.sax.Attributes

class BuildEntityModelSaxHandler(
    val entityModel : EntityModel,
    val inclusionPolicy: InclusionPolicy,
    plugins: List<NetexPlugin> = emptyList(),
) : NetexToolsSaxHandler() {

    private val pluginRegistry = PluginRegistry()

    init {
        plugins.forEach { plugin ->
            pluginRegistry.registerPlugin(plugin)
        }
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
        super.startElement(uri, localName, qName, attributes)

        if (inclusionPolicy.shouldInclude(elementStack)) {
            val isEntity = attributes?.hasAttribute("id") ?: false
            val isRef = attributes?.hasAttribute("ref") ?: false
            if (isEntity) {
                val entity = createEntity(type = qName!!, attributes = attributes)
                registerEntity(entity)
            } else if (isRef) {
                registerRef(type = qName!!, attributes = attributes)
            }

            pluginRegistry.getPluginsForElement(qName!!).forEach { plugin ->
                plugin.startElement(qName, attributes, currentEntity())
            }
        }
    }

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        if (inclusionPolicy.shouldInclude(elementStack)) {
            val currentElementName = currentElement()?.name
            if (currentElementName != null) {
                pluginRegistry.getPluginsForElement(currentElementName).forEach { plugin ->
                    plugin.characters(currentElementName, ch, start, length)
                }
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

        super.endElement(uri, localName, qName)
    }

    override fun endDocument() {
        pluginRegistry.getAllPlugins().forEach { it.endDocument() }
    }

    private fun nn(value : String?) = value ?: EMPTY
}
