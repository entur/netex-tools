package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.model.Entity
import org.xml.sax.Attributes

abstract class NetexDataCollector {
    open fun characters(ch: CharArray?, start: Int, length: Int, currentEntity: Entity) {}
    open fun endElement(uri: String?, localName: String?, qName: String?, currentEntity: Entity) {}
    open fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?, currentEntity: Entity) {}
}