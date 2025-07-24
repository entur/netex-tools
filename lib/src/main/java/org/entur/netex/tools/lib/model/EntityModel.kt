package org.entur.netex.tools.lib.model

import org.entur.netex.tools.lib.model.Entity.Companion.EMPTY
import org.entur.netex.tools.lib.selections.EntitySelection

class EntityModel(private val alias: Alias) {
    private val entities = EntityIndex()
    private val references = RefIndex()

    fun addEntity(entity: Entity) = entities.add(entity)

    fun getEntity(id: String) = entities.get(id)

    fun getEntitiesOfType(type: String): List<Entity> {
        return entities.list(type)
    }

    fun addRef(type: String, entity: Entity, ref: String) {
        references.add(Ref(type, entity, ref))
    }

    fun getRefsOfTypeFrom(sourceId: String, type: String): List<Ref> {
        return references.get(sourceId, type)
    }

    fun forAllEntities(type: String, body: (Entity) -> Unit) {
        entities.list(type).forEach{ body(it) }
    }

    fun forAllReferences(sourceType: String, body: (Ref) -> Unit) {
        references.list(sourceType).forEach{ body(it) }
    }

    fun listAllRefs() : List<Ref> = references.listAll()

    fun printEntities(selection : EntitySelection) {
        Report(
            "SELECTED ENTITIES",
            entities.listAll(),
            alias,
            { it.fullPath() },
            { selection.isSelected(it) }
        ).print()
    }

    fun printReferences(selection : EntitySelection) {
        Report(
            "SELECTED REFERENCES",
            references.listAll(),
            alias,
            { refStr(it) },
            { selection.isSelected(it.source) && selection.isSelected(getEntity(it.ref))}
        ).print()
    }
    private fun refStr(ref : Ref) : String = ref.toString { entities.get(it)?.fullPath() ?: EMPTY }
    fun listAllEntities() = entities.listAll()
}
