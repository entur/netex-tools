package org.entur.netex.tools.lib.model

data class Entity(
    val id : String,
    val type : String,
    val publication : String,
    val parent : Entity? = null,
    val externalRefs: MutableSet<String> = mutableSetOf()
) {
    companion object {
        val EMPTY = "Ø"
    }

    override fun toString() : String = "($id ${fullPath()})"

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

    fun addExternalRef(ref: Ref) {
        externalRefs.add(ref.ref)
    }
}