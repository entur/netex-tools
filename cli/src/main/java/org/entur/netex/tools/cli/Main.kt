package org.entur.netex.tools.cli

import org.entur.netex.tools.cli.app.FilterNetexApp
import org.entur.netex.tools.lib.config.JsonConfig
import java.io.File


fun main(args : Array<String>) {
    if(args.size != 4) {
        printHelp()
        return
    }

    val cliConfig = File(args[0])
        .inputStream()
        .use {
            inputStream -> JsonConfig.loadCliConfig(inputStream)
        }

    val filterConfig = File(args[1])
        .inputStream()
        .use {
            inputStream -> JsonConfig.loadFilterConfig(inputStream)
        }

    val app = FilterNetexApp(
        cliConfig,
        filterConfig,
        File(args[2]),
        File(args[3])
    )
    app.run()
}

fun printHelp() {
    println("""
    The app takes 4 arguments: 
       - <cli-config-file-name>      : The name of the configuration file for CLI, relative to the local directory.
       - <filter-config-file-name>   : The name of the configuration file for filters, relative to the local directory.
       - <netex-input-directory> : The file folder to read Netex data from (Zip not supported, extract).
       - <output-directory>      : The location for the output.     
    """.trimIndent())
}

