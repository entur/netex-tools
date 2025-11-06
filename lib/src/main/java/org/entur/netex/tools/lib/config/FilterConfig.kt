package org.entur.netex.tools.lib.config

import kotlinx.serialization.Serializable
import org.entur.netex.tools.lib.output.XMLElementHandler

@Serializable
data class FilterConfig(
    var preserveComments : Boolean = true,
    var removePrivateData : Boolean = false,
    var period : TimePeriod = TimePeriod(
        start = null,
        end = null
    ),
    var skipElements : List<String> = listOf(),
    var unreferencedEntitiesToPrune : Set<String> = setOf(),
    var pruneReferences : Boolean = false,
    var referencesToExcludeFromPruning : Set<String> = setOf(),
    var useSelfClosingTagsWhereApplicable : Boolean = true,
    var removeInterchangesWithoutServiceJourneys: Boolean = true,
    var removePassengerStopAssignmentsWithUnreferredScheduledStopPoint: Boolean = true,
    var removeNoticeAssignmentWithoutNoticedObjectRef: Boolean = true,
    var renameFiles: Boolean = true,
    var customElementHandlers: Map<String, XMLElementHandler> = mapOf(),
) {
    fun toBuilder() : FilterConfigBuilder {
        return FilterConfigBuilder(filterConfig = this)
    }

    fun hasSpecifiedEntitiesToPrune() : Boolean = unreferencedEntitiesToPrune.isNotEmpty()
}