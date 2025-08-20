package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.Ref
import org.entur.netex.tools.lib.selections.EntitySelection
import org.entur.netex.tools.lib.selections.RefSelection

class SkipEntityAndElementHandler(
    private val entitySelection : EntitySelection,
    private val refSelection : RefSelection,
) {
    private var skipElement : Element? = null

    fun inSkipMode() = skipElement != null

    fun shouldSkip(ref: Ref): Boolean {
        if (inSkipMode()) {
            return true
        }
        return !refSelection.includes(ref)
    }

    fun shouldSkip(entity: Entity): Boolean {
        if (inSkipMode()) {
            return true
        }
        return !entitySelection.includes(entity)
    }

    fun shouldSkip(element: Element): Boolean {
        if (inSkipMode()) {
            return true
        }
        if (element.isEntity()) {
            return !entitySelection.includes(element)
        }
        if (element.isRef()) {
            return false
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
