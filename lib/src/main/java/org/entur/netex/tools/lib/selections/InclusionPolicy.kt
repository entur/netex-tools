package org.entur.netex.tools.lib.selections

import org.entur.netex.tools.lib.model.Element
import java.util.Stack

class InclusionPolicy(
    private val entitySelection: EntitySelection?,
    private val refSelection: RefSelection?,
    private val skipElements: List<String>
) {
    fun matchesSkipElements(currentPath: String) =
        skipElements.any { element -> currentPath.startsWith(element) }

    fun currentPath(elementStack: Stack<Element>): String = "/" + elementStack.joinToString(separator = "/") { it.name }

    fun shouldInclude(elementStack: Stack<Element>): Boolean {
        if (elementStack.isEmpty()) {
            return true
        }
        if (matchesSkipElements(currentPath(elementStack))) {
            return false
        }

        val currentElement = elementStack.peek()

        if (currentElement.isRef() && refSelection != null && entitySelection != null) {
            return refSelection.includes(currentElement.ref()!!)
        }

        if (currentElement.isEntity() && entitySelection != null) {
            return entitySelection.includes(currentElement.currentEntityId!!)
        }

        return true
    }
}