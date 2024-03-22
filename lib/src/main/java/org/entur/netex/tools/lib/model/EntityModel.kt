package org.entur.netex.tools.lib.model

import org.entur.netex.tools.lib.model.Entity.Companion.EMPTY

class EntityModel(private val alias: Alias) {
    private val entities = EntityIndex()
    private val references = ArrayList<Ref>()

    fun addEntity(entity: Entity) = entities.add(entity)

    fun getEntity(id: String) =  entities.get(id)

    fun addRef(type: String, entity: Entity, ref: String) {
        references.add(Ref(type, entity, ref))
    }

    fun forAllEntities(type: String, body: (Entity) -> Unit) {
        entities.list(type).forEach{ body(it) }
    }

    fun forAllReferences(sourceType: String, body: (Ref) -> Unit) {
        references.filter {it.source.type == sourceType}.forEach{ body(it) }
    }

    fun listAllRefs() : List<Ref> = references

    fun printEntities(selection : EntitySelection) {
        Report(
            "SELECTED ENTITIES",
            entities.listAll(),
            alias,
            { it.fullPath() },
            { selection.isSelected(it) }
        ).print()
    }

    fun printReferences(selection : EntitySelection) {
        Report(
            "SELECTED REFERENCES",
            references,
            alias,
            { refStr(it) },
            { selection.isSelected(it.source) && selection.isSelected(getEntity(it.ref))}
        ).print()
    }
    private fun refStr(ref : Ref) : String = ref.toString { entities.get(it)?.fullPath() ?: EMPTY }
}
