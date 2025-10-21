package org.entur.netex.tools.lib.selections

import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.Ref

class InclusionPolicy(
    private val entityModel: EntityModel,
    private val entitySelection: EntitySelection,
    private val refSelection: RefSelection,
    private val skipElements: List<String>
) {
    fun shouldInclude(ref: Ref?): Boolean {
        if (ref != null) {
            return refSelection.includes(ref)
        }
        return false
    }

    fun shouldInclude(entity: Entity?): Boolean {
        if (entity != null) {
            return entitySelection.includes(entity)
        }
        return false
    }

    fun shouldInclude(element: Element, currentPath: String): Boolean {
        if (skipElements.contains(currentPath)) {
            return false
        }
        if (element.isEntity()) {
            val entity = entityModel.getEntity(element)
            return shouldInclude(entity)
        }
        if (element.isRef()) {
            val refAttributeValue = element.getAttribute("ref")
            val ref = entityModel.getRefOfTypeFromSourceIdAndRef(
                element.currentEntityId!!,
                element.name,
                refAttributeValue
            )
            return shouldInclude(ref)
        }
        return true
    }
}