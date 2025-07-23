package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.EntitySelection
import org.entur.netex.tools.lib.model.RefSelection

class SkipEntityAndElementHandler(
    private val entitySelection : EntitySelection,
    private val refSelection : RefSelection,
) {
    private var skipElement : Element? = null

    fun inSkipMode() = skipElement != null

    fun shouldSkip(element: Element): Boolean {
        if (inSkipMode()) {
            return true;
        }
        if (element.isEntity()) {
            return !entitySelection.includes(element)
        }
        if (element.isRef()) {
            return !refSelection.includes(element)
        }
        return false;
    }

    fun startSkip(currentElement: Element): Boolean {
        if(inSkipMode()) {
            return true
        }
        if (currentElement.isEntity() || currentElement.isRef()) {
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
