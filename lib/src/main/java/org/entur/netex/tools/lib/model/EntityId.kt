package org.entur.netex.tools.lib.model

sealed class EntityId {
    abstract val id: String

    data class Simple(override val id: String) : EntityId()
    data class Composite(override val id: String, val version: String, val order: String) : EntityId()
}