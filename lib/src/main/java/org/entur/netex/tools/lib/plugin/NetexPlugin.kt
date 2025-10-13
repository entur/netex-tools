package org.entur.netex.tools.lib.plugin

import org.entur.netex.tools.lib.model.Entity
import org.xml.sax.Attributes

/**
 * Plugin interface that defines the contract for data collection plugins.
 * Each plugin can decide what to do with the data it gets from the BuildEntityModelSaxHandler.
 */
interface NetexPlugin {
    
    /**
     * Gets the unique name of this plugin
     */
    fun getName(): String
    
    /**
     * Gets a description of what this plugin does
     */
    fun getDescription(): String
    
    /**
     * Gets the set of XML element types that this plugin is interested in processing
     */
    fun getSupportedElementTypes(): Set<String>
    
    /**
     * Called when an XML element starts
     */
    fun startElement(elementName: String, attributes: Attributes?, currentEntity: Entity?)
    
    /**
     * Called when character data is encountered within an element
     */
    fun characters(elementName: String, ch: CharArray?, start: Int, length: Int)
    
    /**
     * Called when an XML element ends
     */
    fun endElement(elementName: String, currentEntity: Entity?)

    /**
     * Called when an XML document ends
     * */
    fun endDocument()

    /**
     * Gets the data collected by this plugin (plugin-specific format)
     */
    fun getCollectedData(): Any?
}