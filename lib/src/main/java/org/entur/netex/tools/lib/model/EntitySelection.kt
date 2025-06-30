package org.entur.netex.tools.lib.model

import org.entur.netex.tools.lib.utils.Log

class EntitySelection(val model : EntityModel) {

    // mapping from type -> (id, entity)
    private val selection = mutableMapOf<String, MutableMap<String, Entity>>()

    fun isSelected(e : Entity?) : Boolean {
        if (e == null) { return false }
        return isSelected(e.type, e.id)
    }

    fun isSelected(type : String, id : String?) : Boolean {
        if (id == null) return false
        val m = selection[type]
        return m != null && m.containsKey(id)
    }

    fun select(e: Entity?) : Boolean {
        if (e == null) { return false; }
        val res = selection.computeIfAbsent(e.type) { mutableMapOf() }.put(e.id, e)
        return res == null
    }

    fun select(type : String, ids : List<String>) {
        ids.forEach {
            val e = model.getEntity(it)
            if (type == e?.type) {
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

    /**
     * Remove entities from this selection based on a map of entity types to sets of entity IDs.
     */
    fun removeAll(entitiesToRemove: Map<String, Set<String>>) {
        entitiesToRemove.forEach { (type, idsToRemove) ->
            val entitiesOfType = selection[type]
            if (entitiesOfType != null) {
                idsToRemove.forEach { id ->
                    entitiesOfType.remove(id)
                }
                // Clean up empty type maps
                if (entitiesOfType.isEmpty()) {
                    selection.remove(type)
                }
            }
        }
    }

    fun remove(consumer : (Entity) -> Boolean) {
        val toRemove = mutableListOf<Pair<String, String>>()

        selection.forEach { (type, entitiesMap) ->
            entitiesMap.forEach { (id, entity) ->
                if (consumer(entity)) {
                    toRemove.add(Pair(type, id))
                }
            }
        }

        // Remove entities that match the predicate
        toRemove.forEach { (type, id) ->
            selection[type]?.remove(id)
            // Clean up empty type maps
            if (selection[type]?.isEmpty() == true) {
                selection.remove(type)
            }
        }
    }

    fun forEachSelected(consumer : (Entity) -> Unit) {
        selection.flatMap { it.value.values }.forEach{e -> consumer(e)}
    }

    fun selectType(type : String) = SelectByType(this, type)

    fun includeAll() {
        model.listAllEntities().forEach { select(it) }
    }

    /**
     * Remove entities of specified types that are not referenced by any selected entities.
     * This is useful for cleaning up orphaned entities after filtering operations.
     * Runs multiple passes until no more entities are removed to handle cascading deletions.
     * 
     * @param entityTypes List of entity types to check for unreferenced entities
     */
    fun removeUnreferencedEntities(entityTypes: List<String>) {
        var entitiesRemoved: Boolean
        var pass = 1
        
        do {
            entitiesRemoved = false
            Log.info("Pruning pass $pass...")
            
            entityTypes.forEach { entityType ->
                if (removeUnreferencedEntitiesOfType(entityType)) {
                    entitiesRemoved = true
                }
            }
            pass++
        } while (entitiesRemoved)
        
        Log.info("Pruning completed after ${pass - 1} passes")
    }

    /**
     * Remove entities of a specific type that are not referenced by any selected entities.
     * @return true if any entities were removed, false otherwise
     */
    private fun removeUnreferencedEntitiesOfType(entityType: String): Boolean {
        val entitiesOfType = selection[entityType] ?: return false
        val unreferencedIds = mutableSetOf<String>()

        // Check each entity of the specified type
        entitiesOfType.keys.forEach { entityId ->
            if (!isEntityReferenced(entityType, entityId)) {
                unreferencedIds.add(entityId)
                Log.info("Removing unreferenced $entityType: $entityId")
            }
        }

        // Remove unreferenced entities
        unreferencedIds.forEach { entityId ->
            entitiesOfType.remove(entityId)
        }

        // Clean up empty type maps
        if (entitiesOfType.isEmpty()) {
            selection.remove(entityType)
        }
        
        return unreferencedIds.isNotEmpty()
    }

    /**
     * Check if an entity is referenced by any other selected entities.
     */
    private fun isEntityReferenced(entityType: String, entityId: String): Boolean {
        return model.listAllRefs().any { ref ->
            ref.ref == entityId && isSelected(ref.source)
        }
    }
}