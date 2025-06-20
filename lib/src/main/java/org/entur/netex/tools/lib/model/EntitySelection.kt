package org.entur.netex.tools.lib.model

import org.entur.netex.tools.lib.utils.Log

class EntitySelection(val model : EntityModel) {

    private val selection = mutableMapOf<String, MutableMap<String, Entity>>()

    fun isSelected(e : Entity?) : Boolean {
        if(e == null) { return false }
        return isSelected(e.type, e.id)
    }

    fun isSelected(type : String, id : String?) : Boolean {
        if(id == null) return false
        val m = selection[type]
        return m != null && m.containsKey(id)
    }

    fun select(e: Entity?) : Boolean {
        if(e == null) { return false; }
        val res = selection.computeIfAbsent(e.type) { mutableMapOf() }.put(e.id, e)
        return res == null
    }

    fun select(type : String, ids : List<String>) {
        ids.forEach {
            val e = model.getEntity(it)
            if(type == e?.type) {
                select(e)
            }
            else {
                Log.error("Entity not found: $type, $it")
            }
        }
    }

    /**
     * Propagate selection - if an entity is selected, then select its parent,
     * its grandparent and so on.
     */
    fun selectAllParents() = forEachSelected { selectParents(it) }

    private fun selectParents(e : Entity) {
        var p = e.parent
        while (p != null) {
            select(p)
            p = p.parent
        }
    }

    /**
     * Select all entities referenced by another selected entity.
     */
    fun selectAllReferencedEntities() {
        // TODO - This is an inefficient implementation. The iteration over references is in
        //        random order, and we need to repeat the iteration process multiple times to
        //        ref-chains is complete. If sorted, most references can be iterated over once,
        //        and to resolve reference cycles, an extra cycle is needed for the "back"
        //        reference.

        var modified = true
        var refsNext = model.listAllRefs()

        while (modified) {
            modified = false
            val refs = refsNext
            refsNext = mutableListOf()

            refs.forEach { ref ->
                val e1 = ref.source
                if(isSelected(e1)) {
                    val e2 = model.getEntity(ref.ref)
                    if (select(e2)) {
                        modified = true
                    }
                }
                else {
                    refsNext.add(ref)
                }
            }
        }
    }

    fun forEachSelected(consumer : (Entity) -> Unit) {
        selection.flatMap { it.value.values }.forEach{e -> consumer(e)}
    }


    fun selectType(type : String) = SelectByType(this, type)
}