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

    fun build(): FilterConfig {
        return filterConfig
    }
}