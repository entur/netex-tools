package org.entur.netex.tools.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

fun main(args: Array<String>) = NetexToolsCli().main(args)

class NetexToolsCli : CliktCommand(
    name = "netex-tools",
    help = "NeTEx Tools — utilities for parsing, filtering, and transforming NeTEx data.",
    printHelpOnEmptyArgs = true,
) {
    init {
        subcommands(FilterCommand())
    }

    override fun run() = Unit
}
