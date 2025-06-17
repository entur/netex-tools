package org.entur.netex.tools.cli.config

import kotlinx.serialization.Serializable
import org.entur.netex.tools.lib.model.Alias
import org.entur.netex.tools.lib.utils.Log

@Serializable
data class CliConfig(
    var logLevel: Log.Level = Log.Level.INFO,
    var printReport : Boolean = false,
    var rmUnusedQuays : Boolean = false,
    var area : String? = null,
    var period : TimePeriod? = null,
    var lines : List<String> = listOf(),
    var flexLines : List<String> = listOf(),
    var serviceJourneys : List<String> = listOf(),
    var skipElements : List<String> = listOf(),
    var alias : Map<String, String> = mapOf()
) {
    fun alias()  = Alias.of(alias)
}

