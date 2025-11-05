package org.entur.netex.tools.lib.output

interface XMLElementHandler {
    fun onEnterElement(type: String, attributes: Map<String, String>): String
    fun onText(text: String): String
    fun onLeaveElement(type: String): String
}
