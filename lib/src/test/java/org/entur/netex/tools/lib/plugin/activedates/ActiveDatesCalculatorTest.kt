package org.entur.netex.tools.lib.plugin.activedates

import org.entur.netex.tools.lib.config.TimePeriod
import org.entur.netex.tools.lib.model.EntityId
import org.entur.netex.tools.lib.plugin.activedates.data.VehicleJourneyData
import org.entur.netex.tools.lib.plugin.activedates.helper.ActiveEntitiesCollector
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ActiveDatesCalculatorTest {
    @Test
    fun testShouldIncludeDatedServiceJourneyIfArrivalDayOffsetSkewsOperatingDay() {
        val serviceJourneyId = EntityId.Simple("sj1")
        val operatingDayId = EntityId.Simple("opd1")

        val repository = ActiveDatesRepository()
        repository.serviceJourneys.putIfAbsent(serviceJourneyId, VehicleJourneyData(
            operatingDays = mutableListOf(operatingDayId),
            finalArrivalDayOffset = 1L
        ))
        repository.operatingDays.putIfAbsent(operatingDayId, LocalDate.now().minusDays(1))

        val calculator = ActiveDatesCalculator(repository = repository)
        val collector = ActiveEntitiesCollector()

        collector.addServiceJourney(serviceJourneyId)
        collector.addOperatingDay(operatingDayId)

        val result = calculator.shouldIncludeDatedServiceJourney(
            serviceJourneyId,
            operatingDayId,
            collector,
            TimePeriod(start = LocalDate.now(), end = LocalDate.now())
        )
        assertTrue(result)
    }

    @Test
    fun testShouldNotIncludeDatedServiceJourneyIfArrivalDayOffsetDoesntSkewOperatingDayEnough() {
        val serviceJourneyId = EntityId.Simple("sj1")
        val operatingDayId = EntityId.Simple("opd1")

        val repository = ActiveDatesRepository()
        repository.serviceJourneys.putIfAbsent(serviceJourneyId, VehicleJourneyData(
            operatingDays = mutableListOf(operatingDayId),
            finalArrivalDayOffset = 1L
        ))
        repository.operatingDays.putIfAbsent(operatingDayId, LocalDate.now().minusDays(2))

        val calculator = ActiveDatesCalculator(repository = repository)
        val collector = ActiveEntitiesCollector()

        collector.addServiceJourney(serviceJourneyId)
        collector.addOperatingDay(operatingDayId)

        val result = calculator.shouldIncludeDatedServiceJourney(
            serviceJourneyId,
            operatingDayId,
            collector,
            TimePeriod(start = LocalDate.now(), end = LocalDate.now())
        )
        assertFalse(result)
    }
}