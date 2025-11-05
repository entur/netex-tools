package org.entur.netex.tools.lib.output

object XMLElementRenderer {
    fun startElement(type: String): String = "<$type>"

    fun startElement(type: String, attributes: Map<String, String>): String {
        val startTagBuilder = StringBuilder()
        startTagBuilder.append("<$type")
        for ((key, value) in attributes) {
            startTagBuilder.append(" ${key}=\"${value}\"")
        }
        startTagBuilder.append(">")
        return startTagBuilder.toString()
    }

    fun characters(ch: CharArray?, start: Int, length: Int): String = String(ch!!, start, length)

    fun endElement(type: String?): String = "</$type>"
}
