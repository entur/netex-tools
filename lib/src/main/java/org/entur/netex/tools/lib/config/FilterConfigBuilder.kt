package org.entur.netex.tools.lib.config

class FilterConfigBuilder(
    private val filterConfig: FilterConfig = FilterConfig()
) {
    fun withPreserveComments(preserveComments: Boolean): FilterConfigBuilder {
        filterConfig.preserveComments = preserveComments
        return this
    }

    fun withRemovePrivateData(removePrivateData: Boolean): FilterConfigBuilder {
        filterConfig.removePrivateData = removePrivateData
        return this
    }

    fun withPeriod(period: TimePeriod): FilterConfigBuilder {
        filterConfig.period = period
        return this
    }

    fun withSkipElements(skipElements: List<String>): FilterConfigBuilder {
        filterConfig.skipElements = skipElements
        return this
    }

    fun withUnreferencedEntitiesToPrune(unreferencedEntitiesToPrune: Set<String>): FilterConfigBuilder {
        filterConfig.unreferencedEntitiesToPrune = unreferencedEntitiesToPrune
        return this
    }

    fun withPruneReferences(pruneReferences: Boolean): FilterConfigBuilder {
        filterConfig.pruneReferences = pruneReferences
        return this
    }

    fun withReferencesToExcludeFromPruning(referencesToExcludeFromPruning: Set<String>): FilterConfigBuilder {
        filterConfig.referencesToExcludeFromPruning = referencesToExcludeFromPruning
        return this
    }

    fun withUseSelfClosingTagsWhereApplicable(useSelfClosingTagsWhereApplicable: Boolean): FilterConfigBuilder {
        filterConfig.useSelfClosingTagsWhereApplicable = useSelfClosingTagsWhereApplicable
        return this
    }

    fun withRemoveInterchangesWithoutServiceJourneys(removeInterchangesWithoutServiceJourneys: Boolean): FilterConfigBuilder {
        filterConfig.removeInterchangesWithoutServiceJourneys = removeInterchangesWithoutServiceJourneys
        return this
    }

    fun withRemovePassengerStopAssignmentsWithUnreferredScheduledStopPoint(removePassengerStopAssignmentsWithUnreferredScheduledStopPoint: Boolean): FilterConfigBuilder {
        filterConfig.removePassengerStopAssignmentsWithUnreferredScheduledStopPoint = removePassengerStopAssignmentsWithUnreferredScheduledStopPoint
        return this
    }

    fun build(): FilterConfig {
        return filterConfig
    }
}