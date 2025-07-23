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
            return true
        }
        if (element.isEntity()) {
            // TODO: Selections may be abstracted. Do we want this?
            return !entitySelection.includes(element)
        }
        if (element.isRef()) {
            // TODO: Selections may be abstracted. Do we want this?
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

    fun endSkip(currentElement: Element?) {
        if(skipElement === currentElement) {
            skipElement = null
        }
    }
}
