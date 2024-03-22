package org.entur.netex.tools.lib.model


/**
 * Uses the `selection` to select all objects of a given `type` if
 * a condition is meet. To supply a condition, call one of the four
 * member methods.
 */
class SelectByType(
    private val selection : EntitySelection,
    private val type : String
) {

    fun ifRefTargetSelected(targetType :String) : EntitySelection {
        selection.model.forAllReferences(type) { ref ->
            val target = selection.model.getEntity(ref.ref)
            if (targetType == target?.type && selection.isSelected(target)) {
                selection.select(ref.source)
            }
        }
        return selection
    }

    fun ifRefSourceSelected(sourceType :String) : EntitySelection {
        selection.model.forAllReferences(sourceType) { sourceRef ->
            if(selection.isSelected(sourceRef.source)) {
                val target = selection.model.getEntity(sourceRef.ref)
                if (type == target?.type) {
                    selection.select(target)
                }
            }
        }
        return selection
    }

    fun ifParentSelected(parentType: String) : EntitySelection {
        selection.model.forAllEntities(type) { child ->
            val parent = child.parent!!
            if(parent.type == parentType && selection.isSelected(parent)) {
                selection.select(child)
            }
        }
        return selection
    }

    fun ifChildSelected(childType: String)  : EntitySelection {
        selection.model.forAllEntities(childType) { child ->
            val parent = child.parent
            if(parent?.type == type && selection.isSelected(child)) {
                selection.select(parent)
            }
        }
        return selection
    }
}