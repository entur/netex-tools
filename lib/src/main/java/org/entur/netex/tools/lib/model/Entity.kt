package org.entur.netex.tools.lib.model

data class Entity(
    val id : EntityId,
    val type : String,
    val publication : String,
    val parent : Entity? = null,
) {
    companion object {
        val EMPTY = "Ø"
    }

    override fun toString() : String {
        val entityId = when(id) {
            is EntityId.Simple -> id.id
            is EntityId.Composite -> "${id.id}(${id.version},${id.order})"
        }
        return "($entityId ${fullPath()})"
    }

    fun path(): String {
        if(parent == null) {
            return ""
        }
        return parent.type + "/" + parent.path()
    }

    fun fullPath(): String {
        if(parent == null) {
            return type
        }
        return parent.fullPath() + "/" + type
    }
}