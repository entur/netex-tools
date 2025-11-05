package org.entur.netex.tools.lib.output

/**
 * Writer for skipping all the contents of a certain element
 **/
class SkipElementHandler: XMLElementHandler {
    override fun onEnterElement(type: String, attributes: Map<String, String>): String = ""
    override fun onText(text: String): String = ""
    override fun onLeaveElement(type: String): String = ""
}