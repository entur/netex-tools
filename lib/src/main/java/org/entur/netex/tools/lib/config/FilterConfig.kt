package org.entur.netex.tools.lib.config

import kotlinx.serialization.Serializable

@Serializable
data class FilterConfig(
    var preserveComments : Boolean = true,
    var removePrivateData : Boolean = false,
    var period : TimePeriod = TimePeriod(),
    var skipElements : List<String> = listOf(),
) {
    fun toBuilder() : FilterConfigBuilder {
        return FilterConfigBuilder(filterConfig = this)
    }
}