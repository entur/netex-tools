package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.model.Entity
import org.xml.sax.Attributes

interface NetexDataCollector {
    fun characters(ch: CharArray?, start: Int, length: Int, currentEntity: Entity)
    fun endElement(uri: String?, localName: String?, qName: String?, currentEntity: Entity)
    fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?)
}