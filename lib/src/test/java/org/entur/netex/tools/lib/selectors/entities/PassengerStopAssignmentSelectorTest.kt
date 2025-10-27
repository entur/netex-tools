package org.entur.netex.tools.lib.selectors.entities

import org.entur.netex.tools.lib.data.TestDataFactory
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.model.Ref
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class PassengerStopAssignmentSelectorTest {
    @Test
    fun selectPassengerStopAssignment() {
        val psa1 = TestDataFactory.defaultEntity("psa:1", NetexTypes.PASSENGER_STOP_ASSIGNMENT)
        val psa2 = TestDataFactory.defaultEntity("psa:2", NetexTypes.PASSENGER_STOP_ASSIGNMENT)

        val ssp1 = TestDataFactory.defaultEntity("ssp:1", NetexTypes.SCHEDULED_STOP_POINT)
        val ssp2 = TestDataFactory.defaultEntity("ssp:2", NetexTypes.SCHEDULED_STOP_POINT)

        val stopPointInJourneyPattern1 = TestDataFactory.defaultEntity("spijp:1", NetexTypes.STOP_POINT_IN_JOURNEY_PATTERN)
        val stopPointInJourneyPattern2 = TestDataFactory.defaultEntity("spijp:2", NetexTypes.STOP_POINT_IN_JOURNEY_PATTERN)

        val entityModel = TestDataFactory.defaultEntityModel()
        entityModel.addEntity(psa1)
        entityModel.addEntity(psa2)
        entityModel.addEntity(ssp1)
        entityModel.addEntity(ssp2)

        entityModel.addRef(Ref("ScheduledStopPointRef", psa1, ssp1.id))
        entityModel.addRef(Ref("ScheduledStopPointRef", stopPointInJourneyPattern1, ssp1.id))
        entityModel.addRef(Ref("ScheduledStopPointRef", stopPointInJourneyPattern2, ssp2.id))

        val entitySelection = TestDataFactory.entitySelection(
            entities = setOf(
                psa1, psa2, ssp1, ssp2, stopPointInJourneyPattern1, stopPointInJourneyPattern2
            )
        )

        val selection = PassengerStopAssignmentSelector(entitySelection).selectEntities(entityModel)
        assertTrue(selection.isSelected(psa1))
        assertTrue(selection.isSelected(ssp1))

        // not selected because PSA is the only type of entity in the entity selection that refer to ssp:2
        assertFalse(selection.isSelected(psa2))
    }
}