package org.entur.netex.tools.lib.selectors.entities

import org.entur.netex.tools.lib.config.FilterConfig
import org.entur.netex.tools.lib.config.FilterConfigBuilder
import org.entur.netex.tools.lib.data.TestDataFactory
import org.entur.netex.tools.lib.data.TestDataFactory.entitySelection
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.PublicationEnumeration
import org.entur.netex.tools.lib.selections.EntitySelection
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CompositeEntitySelectorTest {

    private lateinit var defaultFilterConfig: FilterConfig
    private lateinit var compositeEntitySelector: CompositeEntitySelector

    @BeforeEach
    fun setUp() {
        defaultFilterConfig = FilterConfigBuilder().build()
        compositeEntitySelector = CompositeEntitySelector(filterConfig = defaultFilterConfig)
    }

    private fun createSimpleEntitySelector(entityIdsToKeep: List<String>): EntitySelector {
        return object : EntitySelector {
            override fun selectEntities(context: EntitySelectorContext): EntitySelection {
                val selectedEntities = mutableMapOf<String, MutableMap<String, Entity>>()
                val model = context.entityModel
                for (entity in model.listAllEntities()) {
                    if (entityIdsToKeep.contains(entity.id)) {
                        val typeMap = selectedEntities.getOrPut(entity.type) { HashMap() }
                        typeMap[entity.id] = entity
                    }
                }
                return EntitySelection(selectedEntities, model)
            }
        }
    }

    @Test
    fun testRunSelectorIntersectsEntitySelections() {
        val entity1 = TestDataFactory.defaultEntity(id = "test1")
        val entity2 = TestDataFactory.defaultEntity(id = "test2")
        val entity3 = TestDataFactory.defaultEntity(id = "test3")

        val model = TestDataFactory.defaultEntityModel().apply {
            addEntity(entity1)
            addEntity(entity2)
            addEntity(entity3)
        }

        val testSelector = createSimpleEntitySelector(listOf(entity1.id, entity2.id))

        val entitySelection = compositeEntitySelector.runSelector(
            selector = testSelector,
            model = model,
            currentEntitySelection = entitySelection(listOf(entity2, entity3))
        )

        assertTrue(entitySelection.includes(entity2))
        assertFalse(entitySelection.includes(entity1))
        assertFalse(entitySelection.includes(entity3))
    }

    @Test
    fun testRemoveRestrictedEntities() {
        val publicEntity1 = TestDataFactory.defaultEntity(id = "test1")
        val publicEntity2 = TestDataFactory.defaultEntity(
            id = "test2",
            publication = PublicationEnumeration.PUBLIC.toString().lowercase()
        )
        val restrictedEntity =
            TestDataFactory.defaultEntity(id = "test3", publication = PublicationEnumeration.RESTRICTED.toString())

        val model = TestDataFactory.defaultEntityModel().apply {
            addEntity(publicEntity1)
            addEntity(publicEntity2)
            addEntity(restrictedEntity)
        }

        val entitySelection = compositeEntitySelector.removeRestrictedEntities(
            model = model,
            currentEntitySelection = entitySelection(
                listOf(
                    publicEntity1,
                    publicEntity2,
                    restrictedEntity
                )
            )
        )

        assertTrue(entitySelection.includes(publicEntity1))
        assertTrue(entitySelection.includes(publicEntity2))
        assertFalse(entitySelection.includes(restrictedEntity))
    }

    @Test
    fun testPruneUnreferencedEntities() {
        val unreferencedEntity = TestDataFactory.defaultEntity(
            id = "2",
            type = "Type"
        )
        val model = TestDataFactory.defaultEntityModel().apply {
            addEntity(unreferencedEntity)
        }
        val entitySelection = compositeEntitySelector.pruneUnreferencedEntities(
            model = model,
            currentEntitySelection = entitySelection(
                listOf(
                    unreferencedEntity
                )
            ),
            unreferencedEntitiesToPrune = setOf("Type")
        )
        assertFalse(entitySelection.includes(unreferencedEntity))
    }
}