package org.entur.netex.tools.lib.plugin.activedates

import org.entur.netex.tools.lib.model.Entity
import org.xml.sax.Attributes

abstract class NetexDataCollector {
    open fun characters(ch: CharArray?, start: Int, length: Int) {}
    open fun endElement(currentEntity: Entity) {}
    open fun startElement(attributes: Attributes?, currentEntity: Entity) {}
}
