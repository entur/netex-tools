package org.entur.netex.tools.lib.selectors.entities

import org.entur.netex.tools.lib.data.TestDataFactory
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ServiceJourneyInterchangeSelectorTest {

    @Test
    fun testSelectServiceJourneyInterchanges() {
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
        entityModel.addRef("FromJourneyRef", interchange1, serviceJourney1.id)
        entityModel.addRef("ToJourneyRef", interchange1, serviceJourney2.id)

        val selector = ServiceJourneyInterchangeSelector(entitySelection)
        val selection = selector.selectEntities(entityModel)

        assertTrue(selection.isSelected(interchange1))
        assertFalse(selection.isSelected(interchange2))
    }
}