package org.entur.netex.tools.lib.selections

import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.Ref
import java.util.Stack

class InclusionPolicy(
    private val entityModel: EntityModel,
    private val entitySelection: EntitySelection?,
    private val refSelection: RefSelection?,
    private val skipElements: List<String>
) {
    fun shouldInclude(ref: Ref?, refSelection: RefSelection): Boolean {
        if (ref != null) {
            return refSelection.includes(ref)
        }
        return false
    }

    fun shouldInclude(entity: Entity?, entitySelection: EntitySelection): Boolean {
        if (entity != null) {
            return entitySelection.includes(entity)
        }
        return false
    }

    fun shouldInclude(elementStack: Stack<Element>, entitySelection: EntitySelection): Boolean =
        elementStack.filter { it.isEntity() }.all { element -> entitySelection.includes(element) }

    fun shouldInclude(elementStack: Stack<Element>, entitySelection: EntitySelection, refSelection: RefSelection): Boolean {
        val allEntitiesOnStackAreIncluded = shouldInclude(elementStack, entitySelection)
        return allEntitiesOnStackAreIncluded && refSelection.includes(elementStack.peek().attributes.getValue("ref"))
    }

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
            return shouldInclude(
                elementStack = elementStack,
                entitySelection = entitySelection,
                refSelection = refSelection,
            )
        }

        if (entitySelection != null) {
            return shouldInclude(
                elementStack = elementStack,
                entitySelection = entitySelection,
            )
        }

        return true
    }
}