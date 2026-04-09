package org.entur.netex.tools.pipeline.app

import org.entur.netex.tools.lib.NetexFilter
import org.entur.netex.tools.lib.config.FilterConfig
import org.entur.netex.tools.lib.config.CliConfig
import org.entur.netex.tools.lib.report.FilterReport
import org.entur.netex.tools.lib.selections.EntitySelection
import org.entur.netex.tools.lib.utils.timedSeconds
import org.slf4j.LoggerFactory
import java.io.File

data class FilterNetexApp(
    val cliConfig : CliConfig = CliConfig(),
    val filterConfig: FilterConfig = FilterConfig(),
    val input : File,
    val target : File,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val filter = NetexFilter(cliConfig, filterConfig)

    val model get() = filter.model
    val fileIndex get() = filter.fileIndex

    fun run(): FilterReport {
        logger.info("CliConfig:\n$cliConfig")
        logger.info("FilterConfig:\n$filterConfig")
        logger.info("Read input from file: $input")
        logger.info("Write output to: ${target.absolutePath}")

        val (entitySelection, seconds) = timedSeconds {
            filter.buildEntityModel(input)
            val (entitiesToKeep, refsToKeep) = filter.selectEntities()
            filter.exportToFiles(input, target, entitiesToKeep, refsToKeep)
            entitiesToKeep
        }

        printReport(entitySelection, seconds)

        return FilterReport(
            entitiesByFile = fileIndex.entitiesByFile,
            elementTypesByFile = fileIndex.elementTypesByFile,
        )
    }

    private fun printReport(selection: EntitySelection, secondsSpent: Double) {
        if (cliConfig.printReport) {
            logger.info(model.getEntitiesKeptReport(selection))
            logger.info(model.getRefsKeptReport(selection))
        }
        logger.info("Filter NeTEx files done in $secondsSpent seconds.")
    }
}
