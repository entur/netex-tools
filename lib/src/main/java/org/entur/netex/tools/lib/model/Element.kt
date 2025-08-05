package org.entur.netex.tools.lib.model

import org.xml.sax.Attributes

data class Element(
    val name : String,
    val parent : Element? = null,
    val attributes: Attributes? = null,
) {
    fun isEntity(): Boolean {
        return attributes?.getValue("id") != null
    }

    fun isRef(): Boolean {
        return attributes?.getValue("ref") != null
    }

    fun isDescendantOf(other: Element): Boolean {
        var current: Element? = this
        while (current != null) {
            if (current == other) {
                return true
            }
            current = current.parent
        }
        return false
    }
}