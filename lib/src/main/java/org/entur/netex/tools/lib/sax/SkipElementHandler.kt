package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.model.Element


class SkipElementHandler(private val skipElements : Set<String>) {
    private var skipElement : Element? = null

    fun inSkipMode() = skipElement != null

    fun startSkip(currentElement: Element): Boolean {
        if(inSkipMode()) {
            return true
        }
        if(skipElements.contains(currentElement.name)) {
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
