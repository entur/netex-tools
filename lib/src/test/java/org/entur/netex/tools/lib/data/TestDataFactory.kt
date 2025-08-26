package org.entur.netex.tools.lib.data

import org.entur.netex.tools.lib.data.TestDataFactory.entityWithReference
import org.entur.netex.tools.lib.model.Alias
import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.selections.EntitySelection
import org.entur.netex.tools.lib.model.PublicationEnumeration
import org.entur.netex.tools.lib.model.Ref
import org.xml.sax.helpers.AttributesImpl

object TestDataFactory {
    fun defaultEntityModel(): EntityModel = EntityModel(alias = Alias.of(emptyMap()))

    fun defaultEntity(id: String, type: String = "testType"): Entity = Entity(
        id = id,
        type = type,
        publication = PublicationEnumeration.PUBLIC.toString(),
        parent = null
    )

    fun defaultRef(id: String): Ref = Ref(
        type = "testType",
        source = defaultEntity(id),
        ref = id
    )

    fun entityWithReference(id: String, ref: String, type: String = "testType"): Entity {
        val entity = defaultEntity(id=id, type=type)
        entity.addExternalRef(defaultRef(id=ref))
        return entity
    }

    fun entitySelectionWithUnreferredEntities(): EntitySelection {
        val entities = listOf(
            defaultEntity(id = "entity1", type = "unreferencedType"),
            entityWithReference(id = "entity2", ref = "entity1", type = "unreferencedType"),
            entityWithReference(id = "entity3", ref = "entity2", type = "unreferencedType"),
            entityWithReference(id = "entity4", ref = "entity3", type = "unreferencedType")
        )
        return entitySelection(entities)
    }

    fun entitySelectionWithReferredEntities(): EntitySelection {
        val entities = listOf(
            entityWithReference(id = "entity1", ref = "entity2", type = "unreferencedType"),
            entityWithReference(id = "entity2", ref = "entity1", type = "unreferencedType"),
        )
        return entitySelection(entities)
    }

    fun entitySelection(entities: Collection<Entity>) : EntitySelection {
        val selection = mutableMapOf<String, MutableMap<String, Entity>>()
        val entityModel = EntityModel(alias = Alias.of(emptyMap()))
        entities.forEach { entity ->
            entityModel.addEntity(entity)
            if (selection.containsKey(entity.type)) {
                selection[entity.type]?.put(entity.id, entity)
            } else {
                selection[entity.type] = mutableMapOf(entity.id to entity)
            }
        }
        return EntitySelection(selection, entityModel)
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