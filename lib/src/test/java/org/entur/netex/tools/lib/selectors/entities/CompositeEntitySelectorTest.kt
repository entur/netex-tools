package org.entur.netex.tools.lib.selectors.entities

import org.entur.netex.tools.lib.config.FilterConfig
import org.entur.netex.tools.lib.config.FilterConfigBuilder
import org.entur.netex.tools.lib.config.TimePeriod
import org.entur.netex.tools.lib.data.TestDataFactory
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.model.PublicationEnumeration
import org.entur.netex.tools.lib.model.Ref
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesPlugin
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.selections.EntitySelection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class CompositeEntitySelectorTest {

    private lateinit var defaultFilterConfig: FilterConfig
    private lateinit var compositeEntitySelector: CompositeEntitySelector
    private lateinit var activeDatesPlugin: ActiveDatesPlugin

    private val defaultPeriod = TimePeriod(
        start = LocalDate.of(2025, 1, 1),
        end = LocalDate.of(2025, 1, 1),
    )

    @BeforeEach
    fun setUp() {
        activeDatesPlugin = ActiveDatesPlugin(ActiveDatesRepository())
        defaultFilterConfig = FilterConfigBuilder().build()
        compositeEntitySelector = CompositeEntitySelector(
            filterConfig = defaultFilterConfig,
            activeDatesPlugin = activeDatesPlugin,
        )
    }

    @Test
    fun testGetInitialEntitySelectors() {
        var selectors = compositeEntitySelector.getInitialEntitySelectors(defaultFilterConfig)
        assertEquals(1, selectors.size)
        assertTrue(selectors[0] is AllEntitiesSelector)

        val filterConfig = FilterConfigBuilder()
            .withPeriod(defaultPeriod)
            .withRemovePrivateData(true)
            .build()

        selectors = compositeEntitySelector.getInitialEntitySelectors(filterConfig)
        assertEquals(3, selectors.size)
        assertTrue(selectors[0] is AllEntitiesSelector)
        assertTrue(selectors[1] is PublicEntitiesSelector)
        assertTrue(selectors[2] is ActiveDatesSelector)
    }

    @Test
    fun testRunSelector() {
        val model = TestDataFactory.defaultEntityModel()
        val entity = TestDataFactory.defaultEntity(id = "testId")
        model.addEntity(entity)

        val selector = AllEntitiesSelector()
        val entitySelection = compositeEntitySelector.runSelector(selector, model)
        assertTrue(entitySelection.includes(entity))
    }

    private fun createSimpleEntitySelector(entityIdsToKeep: List<String>): EntitySelector {
        return object : EntitySelector() {
            override fun selectEntities(model: EntityModel): EntitySelection {
                val selectedEntities = mutableMapOf<String, MutableMap<String, Entity>>()
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
    fun testSelectEntitiesToKeepIntersectsEntitySelections() {
        val entity1 = TestDataFactory.defaultEntity(id = "test1")
        val entity2 = TestDataFactory.defaultEntity(id = "test2")
        val entity3 = TestDataFactory.defaultEntity(id = "test3")

        val model = TestDataFactory.defaultEntityModel().apply {
            addEntity(entity1)
            addEntity(entity2)
            addEntity(entity3)
        }

        val testSelector1 = createSimpleEntitySelector(listOf(entity1.id, entity2.id))
        val testSelector2 = createSimpleEntitySelector(listOf(entity2.id, entity3.id))

        val entitySelection = compositeEntitySelector.selectEntitiesToKeep(
            selectors = listOf(testSelector1, testSelector2),
            entityModel = model
        )

        assertTrue(entitySelection.includes(entity2))
        assertFalse(entitySelection.includes(entity1))
        assertFalse(entitySelection.includes(entity3))
    }

    @Test
    fun testRunInitialEntitySelection() {
        val entity1 = TestDataFactory.defaultEntity(id = "test1", publication = PublicationEnumeration.RESTRICTED.toString())
        val entity2 = TestDataFactory.defaultEntity(id = "test2")
        val entity3 = TestDataFactory.defaultEntity(id = "test3")

        val model = TestDataFactory.defaultEntityModel().apply {
            addEntity(entity1)
            addEntity(entity2)
            addEntity(entity3)
        }

        val filterConfig = FilterConfigBuilder()
            .withRemovePrivateData(true)
            .build()

        val entitySelection = compositeEntitySelector.runInitialEntitySelection(model, filterConfig)
        assertFalse(entitySelection.includes(entity1))
        assertTrue(entitySelection.includes(entity2))
        assertTrue(entitySelection.includes(entity3))
    }

    @Test
    fun testPruneUnreferencedEntities() {
        val referredEntity = TestDataFactory.defaultEntity(id = "referredId", type = "referredType")
        val unreferredEntity = TestDataFactory.defaultEntity(id = "unreferredId", type = "unreferredType")

        val entityModel = TestDataFactory.defaultEntityModel().apply {
            addEntity(unreferredEntity)
            addEntity(referredEntity)
            addRef(TestDataFactory.defaultRef(referredEntity))
        }

        val entitySelectionWithUnreferredEntities = EntitySelection(
            selection = mutableMapOf(
                "referredType" to mutableMapOf(referredEntity.id to referredEntity),
                "unreferredType" to mutableMapOf(unreferredEntity.id to unreferredEntity)
            ),
            model = entityModel
        )

        val filterConfig = FilterConfigBuilder()
            .withUnreferencedEntitiesToPrune(setOf("referredType", "unreferredType"))
            .build()

        val prunedEntitySelection = compositeEntitySelector.pruneUnreferencedEntities(
            entityModel,
            entitySelectionWithUnreferredEntities,
            filterConfig
        )

        assertTrue(prunedEntitySelection.includes(referredEntity))
        assertFalse(prunedEntitySelection.includes(unreferredEntity))
    }

    @Test
    fun testRemoveInterchangesWithoutServiceJourneys() {
        val serviceJourney1 = TestDataFactory.defaultEntity("1", "ServiceJourney")
        val serviceJourney2 = TestDataFactory.defaultEntity("2", "ServiceJourney")

        val interchange1 = TestDataFactory.defaultEntity("1", "ServiceJourneyInterchange")
        val interchange2 = TestDataFactory.defaultEntity("2", "ServiceJourneyInterchange")

        val entitySelection = TestDataFactory.entitySelection(
            entities = setOf(
                serviceJourney1,
                serviceJourney2,
                interchange1,
                interchange2
            )
        )

        val entityModel = TestDataFactory.defaultEntityModel()
        entityModel.addEntity(interchange1)
        entityModel.addEntity(interchange2)
        entityModel.addRef(Ref("FromJourneyRef", interchange1, serviceJourney1.id))
        entityModel.addRef(Ref("ToJourneyRef", interchange1, serviceJourney2.id))

        val selection = compositeEntitySelector.removeInterchangesWithoutServiceJourneys(entityModel, entitySelection)

        assertTrue(selection.isSelected(interchange1))
        assertFalse(selection.isSelected(interchange2))
    }

    @Test
    fun testRemovePassengerStopAssignmentsWithUnreferredScheduledStopPoint() {
        val passengerStopAssignment1 = TestDataFactory.defaultEntity("psa:1", NetexTypes.PASSENGER_STOP_ASSIGNMENT)
        val passengerStopAssignment2 = TestDataFactory.defaultEntity("psa:2", NetexTypes.PASSENGER_STOP_ASSIGNMENT)

        val stopReferredFromAssignmentOnly = TestDataFactory.defaultEntity("ssp:2", NetexTypes.SCHEDULED_STOP_POINT)
        val stopReferredFromAssignmentAndJourneyPattern = TestDataFactory.defaultEntity("ssp:1", NetexTypes.SCHEDULED_STOP_POINT)

        val stopPointInJourneyPattern1 = TestDataFactory.defaultEntity("spijp:1", NetexTypes.STOP_POINT_IN_JOURNEY_PATTERN)
        val stopPointInJourneyPattern2 = TestDataFactory.defaultEntity("spijp:2", NetexTypes.STOP_POINT_IN_JOURNEY_PATTERN)

        val entityModel = TestDataFactory.defaultEntityModel().apply {
            addEntity(passengerStopAssignment1)
            addEntity(passengerStopAssignment2)
            addEntity(stopReferredFromAssignmentAndJourneyPattern)
            addEntity(stopReferredFromAssignmentOnly)
            addRef(Ref("ScheduledStopPointRef", passengerStopAssignment1, stopReferredFromAssignmentAndJourneyPattern.id))
            addRef(Ref("ScheduledStopPointRef", stopPointInJourneyPattern1, stopReferredFromAssignmentAndJourneyPattern.id))
            addRef(Ref("ScheduledStopPointRef", passengerStopAssignment2, stopReferredFromAssignmentOnly.id))
        }

        val entitySelection = TestDataFactory.entitySelection(
            entities = setOf(
                passengerStopAssignment1,
                passengerStopAssignment2,
                stopReferredFromAssignmentAndJourneyPattern,
                stopReferredFromAssignmentOnly,
                stopPointInJourneyPattern1,
                stopPointInJourneyPattern2
            )
        )

        val selection = compositeEntitySelector.removePassengerStopAssignmentsWithUnreferredScheduledStopPoint(
            entityModel,
            entitySelection,
        )
        assertTrue(selection.includes(passengerStopAssignment1))
        assertTrue(selection.includes(stopReferredFromAssignmentAndJourneyPattern))
        assertFalse(selection.includes(passengerStopAssignment2))
        assertFalse(selection.includes(stopReferredFromAssignmentOnly))
    }
}