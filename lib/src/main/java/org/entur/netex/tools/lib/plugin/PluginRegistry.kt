package org.entur.netex.tools.lib.plugin

import org.entur.netex.tools.lib.extensions.putOrAddToList

/**
 * Registry for managing NetEx plugins.
 *
 * Supports exact element matching (e.g., "Date") and scoped matching
 * (e.g., "DayTypeAssignment/Date" — matches "Date" only inside "DayTypeAssignment").
 */
class PluginRegistry {
    private val plugins: MutableList<NetexPlugin> = mutableListOf()
    private val elementToPluginsMap: MutableMap<String, MutableList<NetexPlugin>> = mutableMapOf()
    private val scopedElementToPluginsMap: MutableMap<String, MutableList<ScopedPluginMatch>> = mutableMapOf()

    /**
     * Registers a plugin with the registry
     */
    fun registerPlugin(plugin: NetexPlugin) {
        plugins.add(plugin)
        
        // Update element-to-plugins mapping
        plugin.getSupportedElementTypes().forEach { elementType ->
            if ("/" in elementType) {
                val parts = elementType.split("/")
                require(parts.size == 2) {
                    "Scoped element type must be 'Ancestor/Element', got: $elementType"
                }
                val (ancestor, leaf) = parts
                scopedElementToPluginsMap.getOrPut(leaf) { mutableListOf() }
                    .add(ScopedPluginMatch(plugin, ancestor))
            } else {
                elementToPluginsMap.putOrAddToList(elementType, plugin)
            }
        }
    }
    
    /**
     * Gets all plugins that are interested in processing a specific element type
     */
    fun getPluginsForElement(elementType: String): List<NetexPlugin> {
        return elementToPluginsMap[elementType] ?: emptyList()
    }

    fun getScopedPluginsForElement(elementType: String): List<ScopedPluginMatch> {
        return scopedElementToPluginsMap[elementType] ?: emptyList()
    }

    /**
     * Gets all registered plugins
     */
    fun getAllPlugins(): List<NetexPlugin> = plugins

    data class ScopedPluginMatch(val plugin: NetexPlugin, val requiredAncestor: String)
}