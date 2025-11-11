package org.entur.netex.tools.lib.model

class Entity(
    val id : String,
    val type : String,
    val publication : String,
    @Transient val parent : Entity? = null,
) {
    companion object {
        val EMPTY = "Ø"
    }

    override fun toString() : String = "($id ${fullPath()})"

    override fun hashCode(): Int = id.hashCode()

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