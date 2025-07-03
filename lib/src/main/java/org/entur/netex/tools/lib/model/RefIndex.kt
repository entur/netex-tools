package org.entur.netex.tools.lib.model

class RefIndex {
    private val references = mutableListOf<Ref>()
    private val mapBySourceId = mutableMapOf<String, MutableList<Ref>>()
    private val mapBySourceIdAndType = mutableMapOf<String, MutableMap<String, MutableList<Ref>>>()
    private val mapBySourcetype = mutableMapOf<String, MutableList<Ref>>()

    fun add(ref: Ref): Boolean {
        references.add(ref)
        mapBySourceId.computeIfAbsent(ref.source.id) { mutableListOf() }.add(ref)
        mapBySourceIdAndType.computeIfAbsent(ref.source.id) { mutableMapOf() }
            .computeIfAbsent(ref.type) { mutableListOf() }.add(ref)
        mapBySourcetype.computeIfAbsent(ref.source.type) { mutableListOf() }.add(ref)
        return true
    }

    fun get(id: String, type: String): List<Ref> {
        return mapBySourceIdAndType[id]?.get(type) ?: listOf()
    }

    fun get(sourceId: String): List<Ref> {
        return mapBySourceId[sourceId] ?: listOf()
    }

    fun list(sourceType: String): List<Ref> {
        return mapBySourcetype[sourceType] ?: listOf()
    }

    fun listAll(): List<Ref> {
        return references
    }
}