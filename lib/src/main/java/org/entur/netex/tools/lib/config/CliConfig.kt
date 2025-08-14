package org.entur.netex.tools.lib.config

import kotlinx.serialization.Serializable
import org.entur.netex.tools.lib.model.Alias
import org.entur.netex.tools.lib.utils.Log

@Serializable
data class CliConfig(
    var logLevel: Log.Level = Log.Level.INFO,
    var printReport : Boolean = true,
    var alias : Map<String, String> = mapOf(
        "CompositeFrame" to "CF",
        "ResourceFrame" to "RF",
        "ServiceCalendarFrame" to "SCF",
        "ServiceFrame" to "SF",
        "Network" to "NW",
        "TimetableFrame" to "TF",
        "SiteFrame" to "SF",
        "StopPointInJourneyPattern" to "SPInJP"
    )
) {
    fun toBuilder() = CliConfigBuilder(this)

    fun alias()  = Alias.of(alias)
}
