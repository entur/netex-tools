package org.entur.netex.tools.lib.model

class EntityIndex {
    private val mapById = mutableMapOf<String, Entity>()
    private val mapByType = mutableMapOf<String, MutableList<Entity>>()
    private val mapByTypeAndId = mutableMapOf<String, MutableMap<String, Entity>>()

    fun add(e : Entity): Boolean {
        val entityId = if (e.compositeId != null) e.compositeId.id else e.id
        if(!mapById.containsKey(entityId)) {
            mapById[entityId] = e
            mapByType.computeIfAbsent(e.type) { mutableListOf() }.add(e)
            mapByTypeAndId.computeIfAbsent(e.type) { mutableMapOf() }.put(entityId, e)
            return true
        }
        return false
    }

    fun get(id : String) = mapById[id]

    fun list(type : String) : List<Entity> = mapByType[type] ?: listOf()

    fun listAll() : Collection<Entity> = mapById.values

    fun entitiesByTypeAndId() : MutableMap<String, MutableMap<String, Entity>> = mapByTypeAndId
}