package org.entur.netex.tools.lib.plugin.activedates

import org.entur.netex.tools.lib.config.TimePeriod
import org.entur.netex.tools.lib.plugin.activedates.data.VehicleJourneyData
import org.entur.netex.tools.lib.plugin.activedates.helper.ActiveEntitiesCollector
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ActiveDatesCalculatorTest {
    @Test
    fun testShouldIncludeDatedServiceJourneyIfArrivalDayOffsetSkewsOperatingDay() {
        val repository = ActiveDatesRepository()
        repository.serviceJourneys.putIfAbsent("sj1", VehicleJourneyData(
            operatingDays = mutableListOf("opd1"),
            finalArrivalDayOffset = 1L
        ))
        repository.operatingDays.putIfAbsent("opd1", LocalDate.now().minusDays(1))

        val calculator = ActiveDatesCalculator(repository = repository)
        val collector = ActiveEntitiesCollector()

        collector.addServiceJourney("sj1")
        collector.addOperatingDay("opd1")

        val result = calculator.shouldIncludeDatedServiceJourney(
            "sj1",
            "opd1",
            collector,
            TimePeriod(start = LocalDate.now(), end = LocalDate.now())
        )
        assertTrue(result)
    }

    @Test
    fun testShouldNotIncludeDatedServiceJourneyIfArrivalDayOffsetDoesntSkewOperatingDayEnough() {
        val repository = ActiveDatesRepository()
        repository.serviceJourneys.putIfAbsent("sj1", VehicleJourneyData(
            operatingDays = mutableListOf("opd1"),
            finalArrivalDayOffset = 1L
        ))
        repository.operatingDays.putIfAbsent("opd1", LocalDate.now().minusDays(2))

        val calculator = ActiveDatesCalculator(repository = repository)
        val collector = ActiveEntitiesCollector()

        collector.addServiceJourney("sj1")
        collector.addOperatingDay("opd1")

        val result = calculator.shouldIncludeDatedServiceJourney(
            "sj1",
            "opd1",
            collector,
            TimePeriod(start = LocalDate.now(), end = LocalDate.now())
        )
        assertFalse(result)
    }
}