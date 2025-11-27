package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.data.TestDataFactory
import org.entur.netex.tools.lib.model.NetexTypes
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ParentsWithRequiredChildrenDeferEventsRuleTest {
    val rule = ParentsWithRequiredChildrenDeferEventsRule(
        parentsWithRequiredChildren = mapOf(
            NetexTypes.NOTICE_ASSIGNMENT to listOf(
                NetexTypes.NOTICE_REF,
                NetexTypes.NOTICED_OBJECT_REF,
            )
        )
    )

    @Test
    fun testShouldDeferIfElementHasRequiredChildren() {
        val eventRecord = TestDataFactory.startElementEventOfType(NetexTypes.NOTICE_ASSIGNMENT)
        val shouldDefer = rule.shouldDefer(eventRecord, EventBuffer())
        assertTrue(shouldDefer)
    }

    @Test
    fun testShouldDeferIfBufferNotEmpty() {
        val eventRecord = TestDataFactory.startElementEventOfType("TestType")
        val shouldDefer = rule.shouldDefer(eventRecord, EventBuffer().apply {
            add(eventRecord)
        })
        assertTrue(shouldDefer)
    }

    @Test
    fun testShouldNotDeferIfElementHasNoRequiredChildren() {
        val eventRecord = TestDataFactory.startElementEventOfType("TestType")
        val shouldDefer = rule.shouldDefer(eventRecord, EventBuffer())
        assertFalse(shouldDefer)
    }

    @Test
    fun testShouldHandleDeferredEventsIfBufferHasAllRequiredChildren() {
        val buffer = EventBuffer()
        buffer.add(TestDataFactory.startElementEventOfType(NetexTypes.NOTICE_ASSIGNMENT))
        buffer.add(TestDataFactory.startElementEventOfType(NetexTypes.NOTICE_REF))
        buffer.add(TestDataFactory.startElementEventOfType(NetexTypes.NOTICED_OBJECT_REF))
        buffer.add(TestDataFactory.endElementEventOfType(NetexTypes.NOTICED_OBJECT_REF))
        buffer.add(TestDataFactory.endElementEventOfType(NetexTypes.NOTICE_REF))
        buffer.add(TestDataFactory.endElementEventOfType(NetexTypes.NOTICE_ASSIGNMENT))
        assertTrue {
            rule.shouldHandleDeferredEvents(buffer)
        }
    }

    @Test
    fun testShouldNotHandleDeferredEventsIfBufferHasNoRequiredChildren() {
        val buffer = EventBuffer()
        buffer.add(TestDataFactory.startElementEventOfType(NetexTypes.NOTICE_ASSIGNMENT))
        buffer.add(TestDataFactory.endElementEventOfType(NetexTypes.NOTICE_ASSIGNMENT))
        assertFalse {
            rule.shouldHandleDeferredEvents(buffer)
        }
    }

    @Test
    fun testShouldNotHandleDeferredEventsIfBufferDoesNotHaveAllRequiredChildren() {
        val buffer = EventBuffer()
        buffer.add(TestDataFactory.startElementEventOfType(NetexTypes.NOTICE_ASSIGNMENT))
        buffer.add(TestDataFactory.startElementEventOfType(NetexTypes.NOTICED_OBJECT_REF))
        buffer.add(TestDataFactory.endElementEventOfType(NetexTypes.NOTICED_OBJECT_REF))
        buffer.add(TestDataFactory.endElementEventOfType(NetexTypes.NOTICE_ASSIGNMENT))
        assertFalse {
            rule.shouldHandleDeferredEvents(buffer)
        }
    }
}