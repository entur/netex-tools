package org.entur.netex.tools.lib.config

import kotlinx.serialization.Serializable

@Serializable
data class FilterConfig(
    var preserveComments : Boolean = true,
    var removePrivateData : Boolean = false,
    var period : TimePeriod = TimePeriod(),
    var skipElements : List<String> = listOf(),
    var unreferencedEntitiesToPrune : Set<String> = setOf(),
    var pruneReferences : Boolean = false,
    var referencesToExcludeFromPruning : Set<String> = setOf(),
    var useSelfClosingTagsWhereApplicable : Boolean = true,
) {
    fun toBuilder() : FilterConfigBuilder {
        return FilterConfigBuilder(filterConfig = this)
    }
}