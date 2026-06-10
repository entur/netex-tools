package org.entur.netex.tools.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import org.entur.netex.tools.cli.app.FilterNetexApp
import org.entur.netex.tools.lib.config.CliConfig
import org.entur.netex.tools.lib.config.JsonConfig

class FilterCommand : CliktCommand(
    name = "filter",
    help = "Filter a NeTEx dataset and write the result to an output directory."
) {
    private val cliConfigFile by option(
        "--cli-config",
        help = "Path to CLI config JSON file (optional). If omitted, defaults are used."
    ).file(mustExist = true, canBeDir = false)

    private val filterConfigFile by option(
        "--filter-config",
        help = "Path to filter config JSON file."
    ).file(mustExist = true, canBeDir = false).required()

    private val input by option(
        "--input",
        help = "Input directory containing NeTEx XML files."
    ).file(mustExist = true, canBeDir = true, canBeFile = false).required()

    private val output by option(
        "--output",
        help = "Output directory for filtered NeTEx XML files. Created if it does not exist."
    ).file().required()

    override fun run() {
        val cliConfig = cliConfigFile?.inputStream()?.use { JsonConfig.loadCliConfig(it) } ?: CliConfig()
        val filterConfig = filterConfigFile.inputStream().use { JsonConfig.loadFilterConfig(it) }
        FilterNetexApp(cliConfig, filterConfig, input, target = output).run()
    }
}
