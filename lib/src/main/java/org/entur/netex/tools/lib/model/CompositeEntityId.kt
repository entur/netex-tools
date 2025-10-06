package org.entur.netex.tools.lib.model

sealed class CompositeEntityId {
    abstract val id: String

    class ByIdVersionAndOrder(
        baseId: String,
        version: String,
        order: String
    ) : CompositeEntityId() {
        override val id: String = buildId(baseId, version, order)

        companion object {
            private const val DELIMITER = "|"

            fun buildId(baseId: String, version: String, order: String): String =
                listOf(baseId, version, order).joinToString(DELIMITER)
        }

        override fun equals(other: Any?): Boolean =
            other is ByIdVersionAndOrder && id == other.id

        override fun hashCode(): Int = id.hashCode()

        override fun toString(): String = id
    }
}