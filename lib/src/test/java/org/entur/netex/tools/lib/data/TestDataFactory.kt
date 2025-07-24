package org.entur.netex.tools.lib.data

import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.selections.EntitySelection
import org.entur.netex.tools.lib.model.PublicationEnumeration
import org.entur.netex.tools.lib.model.Ref
import org.xml.sax.helpers.AttributesImpl

object TestDataFactory {
    fun defaultEntity(id: String): Entity = Entity(
        id = id,
        type = "testType",
        publication = PublicationEnumeration.PUBLIC.toString(),
        parent = null
    )

    fun defaultRef(id: String): Ref = Ref(
        type = "testType",
        source = defaultEntity(id),
        ref = id
    )

    fun entitySelection(entities: Collection<Entity>) : EntitySelection {
        val selection = mutableMapOf<String, MutableMap<String, Entity>>()
        entities.forEach { entity ->
            if (selection.containsKey(entity.type)) {
                selection[entity.type]?.put(entity.id, entity)
            } else {
                selection[entity.type] = mutableMapOf(entity.id to entity)
            }
        }
        return EntitySelection(selection)
    }

    fun defaultElement(name: String, id: String? = null, ref: String? = null): Element {
        val attributes = AttributesImpl()
        if (id != null) {
            attributes.addAttribute("", "id", "id", "CDATA", id)
        }
        if (ref != null) {
            attributes.addAttribute("", "ref", "ref", "CDATA", ref)
        }
        return Element(
            name = name,
            attributes = attributes,
            parent = null
        )
    }
}
