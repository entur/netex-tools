package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.model.CompositeEntityId
import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.Entity.Companion.EMPTY
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.Ref
import org.entur.netex.tools.lib.plugin.NetexPlugin
import org.entur.netex.tools.lib.plugin.PluginRegistry
import org.xml.sax.Attributes

class BuildEntityModelSaxHandler(
    val entities : EntityModel,
    val skipHandler : SkipElementHandler,
    plugins: List<NetexPlugin> = emptyList(),
) : NetexToolsSaxHandler() {
    var currentEntity : Entity? = null
    var currentElement : Element? = null
    
    // Plugin management
    private val pluginRegistry = PluginRegistry()
    private val elementPath = mutableListOf<String>()
    
    init {
        // Register and initialize plugins
        plugins.forEach { plugin ->
            pluginRegistry.registerPlugin(plugin)
        }
    }

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        val type = qName!!
        currentElement = Element(type, currentElement, attributes)
        elementPath.add(type)

        if(skipHandler.startSkip(currentElement!!)) {
            return
        }

        // Handle entity
        val id = attributes?.getValue("id")
        val version = attributes?.getValue("version")
        val order = attributes?.getValue("order")
        val publication = attributes?.getValue("publication") ?: "public"

        if (id != null) {
            val entity = Entity(
                id =
                    if (type == "DayTypeAssignment") "$id|${nn(version)}|${nn(order)}"
                    else id,
                type,
                publication,
                currentEntity,
                compositeId =
                    if (type == "DayTypeAssignment")
                        CompositeEntityId.IdVersionOrderId(
                            baseId = id,
                            version = nn(version),
                            order = nn(order)
                        )
                    else null

            )
            currentEntity = entity
            entities.addEntity(entity)
        } else {
            val ref = attributes?.getValue("ref")
            if (ref != null) {
                val refObject = Ref(nn(type), currentEntity!!, ref)
                entities.addRef(refObject)
            }
        }
        
        // Call plugins
        pluginRegistry.getPluginsForElement(type).forEach { plugin ->
            plugin.startElement(type, attributes, currentEntity)
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
        val currentElementName = currentElement?.name
        if (currentElementName != null) {
            pluginRegistry.getPluginsForElement(currentElementName).forEach { plugin ->
                plugin.endElement(currentElementName, currentEntity)
            }
        }

        val c = currentElement
        currentElement = currentElement?.parent
        if (elementPath.isNotEmpty()) {
            elementPath.removeAt(elementPath.size - 1)
        }
        
        if(skipHandler.endSkip(c)){
            return
        }

        if (currentEntity?.type == qName) {
            currentEntity = currentEntity?.parent
        }
    }

    private fun nn(value : String?) = value ?: EMPTY
}
