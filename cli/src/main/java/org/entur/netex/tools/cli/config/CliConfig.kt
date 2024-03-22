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
    var lines : Array<String> = arrayOf(),
    var flexLines : Array<String> = arrayOf(),
    var serviceJourneys : Array<String> = arrayOf(),
    var skipElements : Array<String> = arrayOf(),
    var alias : Array<String> = arrayOf()
) {
    fun alias()  = Alias.of(alias)
}

