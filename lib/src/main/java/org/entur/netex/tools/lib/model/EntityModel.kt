package org.entur.netex.tools.lib.model

import org.entur.netex.tools.lib.model.Entity.Companion.EMPTY
import org.entur.netex.tools.lib.selections.EntitySelection
import org.entur.netex.tools.lib.selections.RefSelection

class EntityModel(private val alias: Alias) {
    private val entities = EntityIndex()
    private val references = RefIndex()
    private val referredEntities = mutableMapOf<String, MutableSet<Entity>>()

    fun addEntity(entity: Entity) = entities.add(entity)

    fun getEntity(id: EntityId) = entities.get(id)

    fun getEntitiesOfType(type: String): List<Entity> {
        return entities.list(type)
    }

    fun getEntitesByTypeAndId(): MutableMap<String, MutableMap<EntityId, Entity>> {
        return entities.entitiesByTypeAndId()
    }

    fun getEntitiesReferringTo(entity: Entity): Set<Entity> {
        val entityId = (entity.id as EntityId.Simple).id
        return referredEntities[entityId] ?: emptySet()
    }

    fun addRef(refObject: Ref) {
        referredEntities.computeIfAbsent(refObject.ref) { mutableSetOf() }.add(refObject.source)
        references.add(refObject)
    }

    fun getRefsOfTypeFrom(sourceId: EntityId, type: String): List<Ref> {
        return references.get(sourceId, type)
    }

    fun getRefOfTypeFromSourceIdAndRef(sourceId: EntityId, type: String, ref: EntityId): Ref? {
        return getRefsOfTypeFrom(sourceId, type).find { it == ref }
    }

    fun forAllEntities(type: String, body: (Entity) -> Unit) {
        entities.list(type).forEach{ body(it) }
    }

    fun forAllReferences(sourceType: String, body: (Ref) -> Unit) {
        references.list(sourceType).forEach{ body(it) }
    }

    fun listAllRefs() : List<Ref> = references.listAll()

    fun getEntitiesKeptReport(selection : EntitySelection): String =
        Report(
            "SELECTED ENTITIES",
            entities.listAll(),
            alias,
            { it.fullPath() },
            { selection.isSelected(it) }
        ).report()

    fun getRefsKeptReport(selection : EntitySelection, refSelection: RefSelection): String =
        Report(
            "SELECTED REFERENCES",
            references.listAll(),
            alias,
            { refStr(it) },
            { selection.isSelected(it.source) && refSelection.includes(it)}
        ).report()

    private fun refStr(ref : Ref) : String = ref.toString { entities.get(EntityId.Simple(it))?.fullPath() ?: EMPTY }
    fun listAllEntities() = entities.listAll()
}
