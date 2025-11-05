package org.entur.netex.tools.lib.output

import org.entur.netex.tools.lib.model.NetexTypes

class ValidBetweenHandler(private val codespace: String): XMLElementHandler {
    override fun onEnterElement(
        type: String,
        attributes: Map<String, String>
    ): String {
        val newAttributes = mapOf(
            "id" to NetexIdGenerator.
            next(codespace, NetexTypes.AVAILABILITY_CONDITION),
            "version" to "1"
        )
        return XMLElementRenderer.startElement(
            type = NetexTypes.AVAILABILITY_CONDITION,
            attributes = newAttributes
        )
    }

    override fun onText(text: String): String = ""
    override fun onLeaveElement(type: String): String = XMLElementRenderer.endElement(NetexTypes.AVAILABILITY_CONDITION)
}
