package org.entur.netex.tools.lib.model

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class CounterTest {

    val counter = Counter()

    @Test
    fun inc() {
        assertEquals(0, counter.get())
        counter.inc()
        assertEquals(1, counter.get())
    }
}