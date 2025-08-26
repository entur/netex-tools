package org.entur.netex.tools.lib.selections

import org.entur.netex.tools.lib.data.TestDataFactory
import org.junit.jupiter.api.Test

class RefSelectionTest {
    @Test
    fun testIncludesOnExistingRef() {
        val includedRef = TestDataFactory.defaultRef("entity1")
        val refSelection = RefSelection(setOf(includedRef))

        assert(refSelection.includes(includedRef)) {
            "RefSelection should include element with ref matching included Ref"
        }
    }

    @Test
    fun testIncludesOnNonExistingRef() {
        val nonIncludedRefElement = TestDataFactory.defaultRef("entity")

        assert(!RefSelection(setOf()).includes(nonIncludedRefElement)) {
            "RefSelection should not include element with ref that does not match any Ref"
        }
    }
}