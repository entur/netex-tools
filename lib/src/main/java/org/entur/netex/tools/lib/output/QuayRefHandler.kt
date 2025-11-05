package org.entur.netex.tools.lib.output

class QuayRefHandler: XMLElementHandler {
    override fun onEnterElement(
        type: String,
        attributes: Map<String, String>
    ): String {
        val newAttributes = mapOf(
            "ref" to attributes.getValue("ref")
        )
        return XMLElementRenderer.startElement(type = type, attributes = newAttributes)
    }

    override fun onText(text: String): String = text
    override fun onLeaveElement(type: String): String = XMLElementRenderer.endElement(type = type)
}
