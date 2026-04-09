package org.entur.netex.tools.lib.output

fun interface XmlOutputStrategy {
    fun write(xmlContext: XmlContext)
}
