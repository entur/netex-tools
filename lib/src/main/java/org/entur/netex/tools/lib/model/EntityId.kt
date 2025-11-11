package org.entur.netex.tools.lib.model

import org.xml.sax.Attributes

object EntityId {
    fun from(type: String, attributes: Attributes) =
        when (type) {
            "DayTypeAssignment", "PassengerStopAssignment" -> {
                val version = attributes.getValue("version")
                val order = attributes.getValue("order")
                val id = attributes.getValue("id")
                CompositeEntityId.ByIdVersionAndOrder(id, version, order).id
            }
            else -> attributes.getValue("id")
        }
}