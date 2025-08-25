//package org.entur.netex.tools.lib.selections
//
//import org.entur.netex.tools.lib.data.TestDataFactory
//import org.junit.jupiter.api.Test
//
//class RefSelectionTest {
//    @Test
//    fun testIncludesOnExistingRef() {
//        val includedRef = TestDataFactory.defaultRef("entity1")
//        val includedRefElement = TestDataFactory.defaultElement(includedRef.type, ref = includedRef.ref)
//        val refSelection = RefSelection(setOf(includedRef.ref))
//
//        assert(refSelection.includes(includedRefElement)) {
//            "RefSelection should include element with ref matching included Ref"
//        }
//    }
//
//    @Test
//    fun testIncludesOnNonExistingRef() {
//        val nonIncludedRefElement = TestDataFactory.defaultElement("entity1", ref = "entity2")
//
//        assert(!RefSelection(setOf()).includes(nonIncludedRefElement)) {
//            "RefSelection should not include element with ref that does not match any Ref"
//        }
//    }
//
//    @Test
//    fun testIncludesOnInvalidRef() {
//        val invalidRefElement = TestDataFactory.defaultElement("entity1")
//
//        assert(!RefSelection(setOf()).includes(invalidRefElement)) {
//            "RefSelection should not include element without a ref attribute"
//        }
//    }
//}