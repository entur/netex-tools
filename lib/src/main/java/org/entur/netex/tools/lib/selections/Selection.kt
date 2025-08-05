package org.entur.netex.tools.lib.selections

import org.entur.netex.tools.lib.model.Element

abstract class Selection {
    /**
     * Checks if the given element is included in the selection.
     * @param element The element to check.
     * @return true if the element is included, false otherwise.
     */
    abstract fun includes(element: Element): Boolean
}