package org.entur.netex.tools.lib.plugin

import org.entur.netex.tools.lib.model.Entity
import org.xml.sax.Attributes

/**
 * Abstract base class for NetEx plugins that provides default implementations
 */
abstract class AbstractNetexPlugin : NetexPlugin {
    

    override fun startElement(elementName: String, attributes: Attributes?, currentEntity: Entity?) {
        // Default implementation - do nothing
    }
    
    override fun characters(elementName: String, ch: CharArray?, start: Int, length: Int) {
        // Default implementation - do nothing
    }
    
    override fun endElement(elementName: String, currentEntity: Entity?) {
        // Default implementation - do nothing
    }

    override fun endDocument() {
        // Default implementation - do nothing
    }
    
    override fun getCollectedData(): Any? {
        return null
    }
}