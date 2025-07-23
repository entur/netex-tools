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
    
    fun startSkip(currentElement: Element, id : String?): Boolean {
        if(inSkipMode()) {
            return true
        }
        if (id != null && !entitySelection.isSelected(currentElement.name, id)) {
            skipElement = currentElement
            return true
        }
        return false
    }

    fun skipRef(currentElement: Element): Boolean {
        if (inSkipMode()) {
            return true
        }
        if (!refSelection.includes(currentElement)) {
            // If the current element is not in the ref selection, we skip it.
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
