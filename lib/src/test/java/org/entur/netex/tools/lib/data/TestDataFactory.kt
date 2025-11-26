package org.entur.netex.tools.lib.data

import org.entur.netex.tools.lib.model.Alias
import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.selections.EntitySelection
import org.entur.netex.tools.lib.model.PublicationEnumeration
import org.entur.netex.tools.lib.model.Ref
import org.entur.netex.tools.lib.output.Characters
import org.entur.netex.tools.lib.output.EndElement
import org.entur.netex.tools.lib.output.StartElement
import org.entur.netex.tools.lib.sax.EventRecord
import org.xml.sax.Attributes
import org.xml.sax.helpers.AttributesImpl

object TestDataFactory {
    fun defaultEntityModel(): EntityModel = EntityModel(alias = Alias.of(emptyMap()))

    fun defaultEntity(
        id: String,
        type: String = "testType",
        publication: String = PublicationEnumeration.PUBLIC.toString().lowercase()
    ): Entity = Entity(
        id = id,
        type = type,
        publication = publication,
        parent = null,
    )

    fun defaultRef(id: String): Ref = Ref(
        type = "testType",
        source = defaultEntity(id),
        ref = id,
    )

    fun defaultRef(entity: Entity): Ref = Ref(
        type = "testType",
        source = entity,
        ref = entity.id,
    )

    fun entitySelectionWithUnreferredEntities(): EntitySelection {
        val entities = listOf(
            defaultEntity(id = "entity1", type = "unreferencedType"),
            defaultEntity(id = "entity2", type = "unreferencedType"),
            defaultEntity(id = "entity3", type = "unreferencedType"),
            defaultEntity(id = "entity4", type = "unreferencedType")
        )
        return entitySelection(entities)
    }

    fun entitySelection(entities: Collection<Entity>, refs: Set<Ref> = emptySet()) : EntitySelection {
        val entitySelection = mutableMapOf<String, MutableMap<String, Entity>>()
        val entityModel = EntityModel(alias = Alias.of(emptyMap()))
        entities.forEach { entity ->
            entityModel.addEntity(entity)
            if (entitySelection.containsKey(entity.type)) {
                entitySelection[entity.type]?.put(entity.id, entity)
            } else {
                entitySelection[entity.type] = mutableMapOf(entity.id to entity)
            }
        }
        refs.forEach { entityModel.addRef(it) }
        return EntitySelection(entitySelection, entityModel)
    }

    fun defaultElement(name: String, id: String? = null, ref: String? = null, currentEntityId: String? = null): Element {
        val attributes = mutableMapOf<String, String>()
        if (id != null) {
            attributes["id"] = id
        }
        if (ref != null) {
            attributes["ref"] = ref
        }
        return Element(
            name = name,
            attributes = attributes,
            parent = null,
            currentEntityId = currentEntityId,
        )
    }

    fun defaultAttributes(id: String? = null, ref: String? = null): Attributes {
        val attrs = AttributesImpl()
        if (id != null) {
            attrs.addAttribute("", "id", "id", "CDATA", id)
        }
        if (ref != null) {
            attrs.addAttribute("", "ref", "ref", "CDATA", ref)
        }
        return attrs
    }

    fun elementWithParentEntity(name: String, currentEntityId: String): Element {
        val element = defaultElement(name = name, currentEntityId = currentEntityId)
        return element
    }

    fun startElementEventRecord() =
        EventRecord(
            event = StartElement(
                uri = "uri",
                qName = "qName",
                localName = "localName",
                attributes = mapOf()
            ),
            element = defaultElement(name = "testType", id = "testType"),
        )

    fun charactersEventRecord() =
        EventRecord(
            event = Characters(
                ch = "Content".toCharArray(),
                start = 0,
                length = "Content".length
            ),
            element = defaultElement(name = "testType", id = "testType"),
        )

    fun endElementEventRecord() =
        EventRecord(
            event = EndElement(
                uri = "uri",
                qName = "qName",
                localName = "localName",
            ),
            element = defaultElement(name = "testType", id = "testType"),
        )
}