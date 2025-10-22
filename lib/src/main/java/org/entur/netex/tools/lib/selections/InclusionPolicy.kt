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

    fun shouldInclude(element: Element?, currentPath: String): Boolean {
        if (element == null) {
            return true
        }
        if (skipElements.contains(currentPath)) {
            return false
        }
        if (element.isEntity() && entitySelection != null) {
            val entity = entityModel.getEntity(element)
            return shouldInclude(
                entity = entity,
                entitySelection = entitySelection,
            )
        }
        if (element.isRef() && refSelection != null) {
            val refAttributeValue = element.getAttribute("ref")
            val ref = entityModel.getRefOfTypeFromSourceIdAndRef(
                element.currentEntityId!!,
                element.name,
                refAttributeValue
            )
            return shouldInclude(
                ref = ref,
                refSelection = refSelection,
            )
        }
        if (element.currentEntityId != null && entitySelection != null) {
            val currentEntity = entityModel.getEntity(element.currentEntityId)
            return shouldInclude(
                entity = currentEntity,
                entitySelection = entitySelection,
            )
        }
        return true
    }
}