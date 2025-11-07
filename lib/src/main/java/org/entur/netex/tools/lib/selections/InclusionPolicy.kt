package org.entur.netex.tools.lib.selections

import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.Ref

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

    fun shouldInclude(element: Element, entitySelection: EntitySelection?): Boolean {
        if (element.currentEntityId == null || entitySelection == null) {
            return true
        }
        val parentEntity = entityModel.getEntity(element.currentEntityId)
        return shouldInclude(parentEntity, entitySelection)
    }

    fun matchesSkipElementsPath(currentPath: String) =
        skipElements.any { element -> currentPath.startsWith(element) }

    fun shouldInclude(element: Element?, currentPath: String): Boolean {
        if (element == null) {
            return true
        }
        if (matchesSkipElementsPath(currentPath)) {
            return false
        }
        if (element.isEntity() && entitySelection != null) {
            return shouldInclude(
                entity = entityModel.getEntity(element),
                entitySelection = entitySelection,
            )
        }
        if (element.isRef() && refSelection != null) {
            return shouldInclude(
                ref = entityModel.getRef(element),
                refSelection = refSelection,
            )
        }
        return shouldInclude(element, entitySelection)
    }
}