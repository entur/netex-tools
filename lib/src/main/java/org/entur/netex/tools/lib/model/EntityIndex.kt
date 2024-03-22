package org.entur.netex.tools.lib.model

class EntityIndex {
    private val mapById = mutableMapOf<String, Entity>()
    private val mapByType = mutableMapOf<String, MutableList<Entity>>()

    fun add(e : Entity): Boolean {
        if(!mapById.containsKey(e.id)) {
            mapById[e.id] = e
            mapByType.computeIfAbsent(e.type) { mutableListOf() }.add(e)
            return true
        }
        return false
    }

    fun get(id : String) = mapById[id]

    fun list(type : String) : List<Entity> = mapByType[type] ?: listOf()

    fun listAll() : Collection<Entity> = mapById.values
}