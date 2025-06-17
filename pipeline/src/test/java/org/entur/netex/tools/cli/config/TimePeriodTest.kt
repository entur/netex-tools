package org.entur.netex.tools.cli.config

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class TimePeriodTest {

    val start = "2024-01-01"
    val end = "2024-02-28"
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