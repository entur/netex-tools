package org.entur.netex.tools.lib.config

import org.entur.netex.tools.lib.utils.Log

class CliConfigBuilder(
    private val cliConfig: CliConfig = CliConfig()
) {
    fun withLogLevel(logLevel: Log.Level): CliConfigBuilder {
        cliConfig.logLevel = logLevel
        return this
    }

    fun withPrintReport(printReport: Boolean): CliConfigBuilder {
        cliConfig.printReport = printReport
        return this
    }

    fun withAliases(alias: Map<String, String>): CliConfigBuilder {
        cliConfig.alias = alias
        return this
    }

    fun build(): CliConfig {
        return cliConfig
    }
}