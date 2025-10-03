package org.entur.netex.tools.lib.model

sealed class CompositeEntityId {
    abstract val id: String

    data class IdVersionOrderId(
        val baseId: String,
        val version: String,
        val order: String
    ) : CompositeEntityId() {
        override val id: String = "$baseId|$version|$order"
        override fun equals(other: Any?) = other is IdVersionOrderId && id == other.id
        override fun hashCode(): Int = id.hashCode()
    }
}