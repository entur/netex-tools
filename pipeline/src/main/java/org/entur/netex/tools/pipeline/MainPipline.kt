package org.entur.netex.tools.pipeline

import org.entur.netex.tools.pipeline.app.FilterNetexApp
import org.entur.netex.tools.pipeline.config.JsonConfig
import java.io.File


fun main(args : Array<String>) {
    if(args.size != 3) {
        printHelp()
        return
    }

    val app = FilterNetexApp(
        JsonConfig.load(File(args[0]).inputStream()),
        File(args[1]),
        File(args[2])
    )
    app.run()
}

fun printHelp() {
    println("""
    The app takes 3 arguments: 
       - <config-file-name>      : The name of the configuration file, relative to the local directory.
       - <netex-input-directory> : The file folder to read Netex data from (Zip not supported, extract).
       - <output-directory>      : The location for the output.     
    """.trimIndent())
}

