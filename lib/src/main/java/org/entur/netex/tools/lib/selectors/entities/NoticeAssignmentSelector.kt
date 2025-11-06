package org.entur.netex.tools.lib.selectors.entities

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.selections.EntitySelection

class NoticeAssignmentSelector(val entitySelection: EntitySelection): EntitySelector() {
    fun findNoticeAssignmentsToKeep(entityModel: EntityModel): List<Entity> {
        val noticeAssignments = entityModel.getEntitiesOfType(NetexTypes.NOTICE_ASSIGNMENT)
        return noticeAssignments.filter { noticeAssignment ->
            val noticeAssignmentId = noticeAssignment.id
            val noticedObjectRef = entityModel.getRefsOfTypeFrom(noticeAssignmentId, "NoticedObjectRef").firstOrNull()?.ref
            entitySelection.includes(noticedObjectRef!!)
        }
    }

    override fun selectEntities(model: EntityModel): EntitySelection {
        val noticeAssignmentsToKeep = findNoticeAssignmentsToKeep(model)
        entitySelection.selection[NetexTypes.NOTICE_ASSIGNMENT] = mutableMapOf()
        noticeAssignmentsToKeep.forEach { noticeAssignment ->
            entitySelection.selection[NetexTypes.NOTICE_ASSIGNMENT]!![noticeAssignment.id] = noticeAssignment
        }
        return entitySelection
    }
}