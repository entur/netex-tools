package org.entur.netex.tools.lib.model

sealed class CompositeEntityId {
    abstract val id: String

    /**
     * Composite identifier for assignment-style entities (e.g. PassengerStopAssignment,
     * DayTypeAssignment, NoticeAssignment) where uniqueness is the triple
     * `(id, version, order)`.
     *
     * Per the NeTEx XSD, `version` and `order` are optional on assignment elements, so
     * this class accepts them as nullable and normalises missing values to an empty
     * segment in the composite key.
     */
    class ByIdVersionAndOrder(
        baseId: String,
        version: String?,
        order: String?,
    ) : CompositeEntityId() {
        override val id: String = buildId(baseId, version.orEmpty(), order.orEmpty())

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
