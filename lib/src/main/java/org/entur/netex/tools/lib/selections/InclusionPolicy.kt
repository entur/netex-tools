package org.entur.netex.tools.lib.selections

import org.entur.netex.tools.lib.model.Element
import java.util.Stack

/**
 * Implements logic used to determine whether to keep a certain element or not, using
 * a list of paths that should be skipped entirely - and optionally, selections of entities and refs.
 * */
class InclusionPolicy(
    private val entitySelection: EntitySelection?,
    private val refSelection: RefSelection?,
    private val skipElements: List<String>
) {
    private fun matchesSkipElements(currentPath: String) =
        skipElements.any { element -> currentPath.startsWith(element) }

    private fun pathOf(element: Element, ancestors: Stack<Pair<Element, Boolean>>): String {
        if (ancestors.isEmpty()) return "/${element.name}"

        return "/" + ancestors.joinToString(separator = "/") { it.first.name } + "/${element.name}"
    }

    private fun shouldBeSkippedBySkipElements(element: Element, ancestors: Stack<Pair<Element, Boolean>>): Boolean {
        val currentPath = pathOf(element, ancestors)
        return matchesSkipElements(currentPath)
    }

    /**
     * Determines whether to include an element or not.
     *
     * Note: Assumes the root element should be kept
     *
     * @param element The element to determine whether to keep
     * @param ancestors A Stack of ancestors to the current element, mapping every ancestor to whether the ancestor is included or not.
     * */
    fun shouldInclude(element: Element, ancestors: Stack<Pair<Element, Boolean>>): Boolean {
        // Assumes the root element should be kept
        if (ancestors.isEmpty()) {
            return true
        }

        // If the closest ancestor is not included, this element should not be included either
        if (!ancestors.peek().second) {
            return false
        }

        if (shouldBeSkippedBySkipElements(element, ancestors)) {
            return false
        }

        if (element.isRef() && refSelection != null && entitySelection != null) {
            return refSelection.includes(element.ref()!!)
        }

        if (element.isEntity() && entitySelection != null) {
            return entitySelection.includes(element.currentEntityId!!)
        }

        // In this case, the element should be included because we are dealing with an ordinary element that is:
        // 1. not skipped through a path of skipElements
        // 2. not encapsulated by an element that has been excluded
        return true
    }
}