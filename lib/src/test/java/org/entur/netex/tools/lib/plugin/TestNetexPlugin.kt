package org.entur.netex.tools.lib.plugin

import org.entur.netex.tools.lib.model.Entity
import org.xml.sax.Attributes
import java.io.File

class TestNetexPlugin: NetexPlugin {
    var hasCalledStartElement = false
    var hasCalledCharacters = false
    var hasCalledEndElement = false
    var supportedElementType = "pluginTestElement"

    fun reset() {
        hasCalledStartElement = false
        hasCalledCharacters = false
        hasCalledEndElement = false
    }

    override fun getName(): String {
        TODO("Not yet implemented")
    }

    override fun getDescription(): String {
        TODO("Not yet implemented")
    }

    override fun getSupportedElementTypes(): Set<String> = setOf(supportedElementType)

    override fun startElement(
        elementName: String,
        attributes: Attributes?,
        currentEntity: Entity?
    ) {
        hasCalledStartElement = true
    }

    override fun characters(
        elementName: String,
        ch: CharArray?,
        start: Int,
        length: Int
    ) {
        hasCalledCharacters = true
    }

    override fun endElement(
        elementName: String,
        currentEntity: Entity?
    ) {
        hasCalledEndElement = true
    }

    override fun endDocument(file: File) {
        TODO("Not yet implemented")
    }

    override fun getCollectedData(): Any? {
        TODO("Not yet implemented")
    }
}