package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.EntitySelection


class SkipEntityAndElementHandler(
    private val skipElements : Set<String>,
    private val selection : EntitySelection
    ) {
    private var skipElement : Element? = null

    fun inSkipMode() = skipElement != null

    fun startSkip(currentElement: Element, id : String?): Boolean {
        if(inSkipMode()) {
            return true
        }
        if(skipElements.contains(currentElement.name)) {
            skipElement = currentElement
            return true
        }
        if (id != null && !selection.isSelected(currentElement.name, id)) {
            skipElement = currentElement
            return true
        }
        return false
    }

    fun endSkip(currentElement: Element?): Boolean {
        if(skipElement === currentElement) {
            skipElement = null
            return true
        }
        return inSkipMode()
    }
}
