package org.entur.netex.tools.lib.output

class DefaultLocaleHandler: XMLElementHandler {
    override fun onEnterElement(
        type: String,
        attributes: Map<String, String>
    ): String {
        val startTag = XMLElementRenderer.startElement(type = type)
        val contentBuilder = StringBuilder()
        contentBuilder.append(startTag)
        contentBuilder.append("\n\t\t  <TimeZone>Europe/Oslo</TimeZone>")
        contentBuilder.append("\n\t\t  <DefaultLanguage>no</DefaultLanguage>")
        return contentBuilder.toString()
    }

    override fun onText(text: String): String = text
    override fun onLeaveElement(type: String): String = XMLElementRenderer.endElement(type = type)
}