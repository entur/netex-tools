package org.entur.netex.tools.lib.model

import org.entur.netex.tools.lib.model.Entity.Companion.EMPTY
import org.entur.netex.tools.lib.selections.EntitySelection

class EntityModel(private val alias: Alias) {
    private val entities = EntityIndex()
    private val references = RefIndex()
    private val referredEntities = mutableMapOf<String, MutableSet<Entity>>()

    fun addEntity(entity: Entity) = entities.add(entity)

    fun getEntity(id: String) = entities.get(id)

    fun getEntitiesOfType(type: String): List<Entity> {
        return entities.list(type)
    }

    fun getEntitesByTypeAndId(): MutableMap<String, MutableMap<String, Entity>> {
        return entities.entitiesByTypeAndId()
    }

    fun getEntitiesReferringTo(entity: Entity): Set<Entity> {
        return referredEntities[entity.id] ?: emptySet()
    }

    fun addRef(type: String, entity: Entity, ref: String) {
        val refObject = Ref(type, entity, ref)
        referredEntities.computeIfAbsent(ref) { mutableSetOf() }.add(entity)
        references.add(refObject)
    }

    fun getRefsOfTypeFrom(sourceId: String, type: String): List<Ref> {
        return references.get(sourceId, type)
    }

    fun getRefOfTypeFromSourceIdAndRef(sourceId: String, type: String, ref: String): Ref? {
        return getRefsOfTypeFrom(sourceId, type).find { it.ref == ref }
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
