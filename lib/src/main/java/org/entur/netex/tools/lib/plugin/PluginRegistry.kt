package org.entur.netex.tools.lib.plugin

/**
 * Registry for managing NetEx plugins
 */
class PluginRegistry {
    private val plugins: MutableList<NetexPlugin> = mutableListOf()
    private val elementToPluginsMap: MutableMap<String, MutableList<NetexPlugin>> = mutableMapOf()
    
    /**
     * Registers a plugin with the registry
     */
    fun registerPlugin(plugin: NetexPlugin) {
        plugins.add(plugin)
        
        // Update element-to-plugins mapping
        plugin.getSupportedElementTypes().forEach { elementType ->
            elementToPluginsMap.computeIfAbsent(elementType) { mutableListOf() }.add(plugin)
        }
    }
    
    /**
     * Gets all plugins that are interested in processing a specific element type
     */
    fun getPluginsForElement(elementType: String): List<NetexPlugin> {
        return elementToPluginsMap[elementType] ?: emptyList()
    }
}