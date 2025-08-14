package org.entur.netex.tools.cli.config

import org.entur.netex.tools.lib.config.TimePeriod
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate

class TimePeriodTest {

    val start = LocalDate.of(2024, 1, 1)
    val end = LocalDate.of(2024,2,28)
    val subject = TimePeriod(start, end)

    @Test
    fun getStart() {
        assertEquals(start, subject.start)
    }

    @Test
    fun getEnd() {
        assertEquals(end, subject.end)
    }

    @Test
    fun testToString() {
        assertEquals("TimePeriod(start=2024-01-01, end=2024-02-28)", subject.toString())
    }
}