package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.data.TestDataFactory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EventBufferTest {
    val eventRecord1 = TestDataFactory.startElementEventRecord()
    val eventRecord2 = TestDataFactory.startElementEventRecord()
    val eventRecord3 = TestDataFactory.endElementEventRecord()
    val eventRecord4 = TestDataFactory.endElementEventRecord()

    lateinit var eventBuffer: EventBuffer

    @BeforeEach
    fun setUp() {
        eventBuffer = EventBuffer().apply {
            add(eventRecord1)
            add(eventRecord2)
            add(eventRecord3)
            add(eventRecord4)
        }
    }

    @Test
    fun testFirst() {
        assertEquals(eventRecord1, eventBuffer.first())
    }

    @Test
    fun testMiddle() {
        val mid = eventBuffer.middle()
        assertEquals(eventRecord2, mid.first())
        assertEquals(eventRecord3, mid.last())
        assertEquals(2, mid.size)
    }

    @Test
    fun testHasReachedEndOfBufferedElementWhenBufferedEventsAreAligned() {
        assertTrue { eventBuffer.hasReachedEndOfBufferedElement() }
    }

    @Test
    fun testHasNotReachedEndOfBufferedElementWhenFlushed() {
        eventBuffer.flush {  }
        assertFalse { eventBuffer.hasReachedEndOfBufferedElement() }
    }

    @Test
    fun testHasNotReachedEndOfBufferedElementWhenEventsAreNotAligned() {
        eventBuffer.add(TestDataFactory.startElementEventRecord())
        assertFalse { eventBuffer.hasReachedEndOfBufferedElement() }
        eventBuffer.add(TestDataFactory.endElementEventRecord())
        assertTrue { eventBuffer.hasReachedEndOfBufferedElement() }
    }
}