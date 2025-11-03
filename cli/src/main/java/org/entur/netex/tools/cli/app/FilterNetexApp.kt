package org.entur.netex.tools.cli.app

import org.entur.netex.tools.lib.config.FilterConfig
import org.entur.netex.tools.lib.config.CliConfig
import org.entur.netex.tools.lib.io.XMLFiles.parseXmlDocuments
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.output.DefaultLocaleWriter
import org.entur.netex.tools.lib.output.DefaultXMLElementWriter
import org.entur.netex.tools.lib.output.NetexFileWriter
import org.entur.netex.tools.lib.plugin.NetexFileWriterContext
import org.entur.netex.tools.lib.plugin.NetexPlugin
import org.entur.netex.tools.lib.output.SkipElementWriter
import org.entur.netex.tools.lib.output.ValidBetweenFromDateWriter
import org.entur.netex.tools.lib.output.ValidBetweenToDateWriter
import org.entur.netex.tools.lib.output.ValidBetweenWriter
import org.entur.netex.tools.lib.selections.EntitySelection
import org.entur.netex.tools.lib.selections.RefSelection
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesPlugin
import org.entur.netex.tools.lib.plugin.file.FileNamePlugin
import org.entur.netex.tools.lib.report.FileIndex
import org.entur.netex.tools.lib.report.FilterReport
import org.entur.netex.tools.lib.sax.*
import org.entur.netex.tools.lib.selections.InclusionPolicy
import org.entur.netex.tools.lib.selectors.entities.CompositeEntitySelector
import org.entur.netex.tools.lib.selectors.refs.CompositeRefSelector
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

    val model = EntityModel(cliConfig.alias())
    val fileIndex = FileIndex()

    // Plugin system
    private val activeDatesRepository = ActiveDatesRepository()
    private val activeDatesPlugin = ActiveDatesPlugin(activeDatesRepository)

    fun run(): FilterReport {
        setupAndLogStartupInfo()

        val (entitySelection, seconds) = timedSeconds {
            // Step 1: collect data needed for filtering out entities
            buildEntityModel()

            // Step 2: select the entities and refs to keep
            val entitiesToKeep = CompositeEntitySelector(filterConfig, activeDatesPlugin).selectEntities(model)
            val refsToKeep = CompositeRefSelector(filterConfig, entitiesToKeep, activeDatesPlugin).selectRefs(model)

            // Step 3: export the filtered data to XML files
            exportXmlFiles(entitiesToKeep, refsToKeep)

            entitiesToKeep
        }

        printReport(entitySelection, seconds)

        return FilterReport(
            entitiesByFile = fileIndex.entitiesByFile,
            elementTypesByFile = fileIndex.elementTypesByFile,
        )
    }

    private fun setupAndLogStartupInfo() {
        logger.info("CliConfig:\n$cliConfig")
        logger.info("FilterConfig:\n$cliConfig")
        logger.info("Read inout from file: $input")
        logger.info("Write output to: ${target.absolutePath}")
    }

    private fun buildEntityModel() {
        logger.info("Load xml files for building entity model")
        parseXmlDocuments(input) {
            createNetexSaxReadHandler(it)
        }
        logger.info("Done reading xml files for building entity model. Model contains ${model.listAllEntities().size} entities and ${model.listAllRefs().size} references.")
    }

    private fun getOutputXmlFile(directory: File, file: File): File {
        if (filterConfig.renameFiles) {
            val newFileName = fileIndex.filesToRename[file.name]
            val outFile = File(target, newFileName ?: file.name)
            if (!outFile.exists()) {
                outFile.createNewFile()
            }
            return outFile
        }
        return File(directory, file.name)
    }

    private fun exportXmlFiles(entitySelection : EntitySelection, refSelection : RefSelection) {
        logger.info("Save xml files")
        if(!target.exists()) {
            target.mkdirs()
        }
        if(target.isFile) {
            throw IllegalArgumentException("Target file is not a directory : ${target.absolutePath}")
        }

        logger.info("Writing filtered xml files to ${target.absolutePath}")
        parseXmlDocuments(input) { file ->
            val outFile = getOutputXmlFile(target, file)
            createNetexSaxWriteHandler(outFile, entitySelection, refSelection)
        }
        logger.info("Done writing filtered xml files to ${target.absolutePath}")
    }

    private fun printReport(selection: EntitySelection, secondsSpent: Double) {
        if (cliConfig.printReport) {
            logger.info(model.getEntitiesKeptReport(selection))
            logger.info(model.getRefsKeptReport(selection))
        }
        logger.info("Filter NeTEx files done in $secondsSpent seconds.")
    }

    private fun getPluginsBy(
        filterConfig: FilterConfig,
        file: File
    ): List<NetexPlugin> {
        val plugins = mutableListOf<NetexPlugin>()
        if (filterConfig.renameFiles) {
            plugins.add(
                FileNamePlugin(
                    currentFile = file,
                    fileIndex = fileIndex,
                )
            )
        }
        if (filterConfig.period.hasStartOrEnd()) {
            plugins.add(activeDatesPlugin)
        }
        return plugins
    }

    private fun createNetexSaxReadHandler(file: File): BuildEntityModelSaxHandler =
        BuildEntityModelSaxHandler(
            entityModel = model,
            plugins = getPluginsBy(filterConfig, file),
            inclusionPolicy = InclusionPolicy(
                entityModel = model,
                entitySelection = null,
                refSelection = null,
                skipElements = filterConfig.skipElements
            )
        )

    private fun createNetexSaxWriteHandler(file: File, entitySelection: EntitySelection, refSelection: RefSelection): OutputNetexSaxHandler {
        val netexFileWriterContext = NetexFileWriterContext(
            file = file,
            useSelfClosingTagsWhereApplicable = filterConfig.useSelfClosingTagsWhereApplicable,
            removeEmptyCollections = true,
            preserveComments = filterConfig.preserveComments,
            period = filterConfig.period,
        )

        val outputFileContent = StringBuilder()
        val bufferedWhitespace = StringBuilder()
        val fileWriter = file.bufferedWriter(Charsets.UTF_8)
        val defaultNetexFileWriter = NetexFileWriter(
            netexFileWriterContext = netexFileWriterContext,
            writer = fileWriter,
            outputFileContent = outputFileContent
        )

        val skipElementWriter = SkipElementWriter(outputFileContent, bufferedWhitespace)
        val validBetweenWriter = ValidBetweenWriter(outputFileContent, bufferedWhitespace)
        val validBetweenFromDateWriter = ValidBetweenFromDateWriter(outputFileContent, bufferedWhitespace, filterConfig.period.start!!)
        val validBetweenToDateWriter = ValidBetweenToDateWriter(outputFileContent, bufferedWhitespace, filterConfig.period.end!!)
        val defaultLocaleWriter = DefaultLocaleWriter(outputFileContent, bufferedWhitespace)

        return OutputNetexSaxHandler(
            entityModel = model,
            fileIndex = fileIndex,
            inclusionPolicy = InclusionPolicy(
                entityModel = model,
                entitySelection = entitySelection,
                refSelection = refSelection,
                skipElements = filterConfig.skipElements
            ),
            fileWriter = defaultNetexFileWriter,
            outputFile = file,
            defaultElementWriter = DefaultXMLElementWriter(outputFileContent,bufferedWhitespace),
            elementWriters = mapOf(
                "/PublicationDelivery/dataObjects/ServiceCalendarFrame/ServiceFrame" to skipElementWriter,
                "/PublicationDelivery/dataObjects/CompositeFrame/frames/ServiceCalendarFrame/ServiceFrame" to skipElementWriter,
                "/PublicationDelivery/dataObjects/CompositeFrame/validityConditions/ValidBetween" to validBetweenWriter,
                "/PublicationDelivery/dataObjects/CompositeFrame/validityConditions/ValidBetween/FromDate" to validBetweenFromDateWriter,
                "/PublicationDelivery/dataObjects/CompositeFrame/validityConditions/ValidBetween/ToDate" to validBetweenToDateWriter,
                "/PublicationDelivery/dataObjects/CompositeFrame/FrameDefaults/DefaultLocale/TimeZone" to skipElementWriter,
                "/PublicationDelivery/dataObjects/CompositeFrame/FrameDefaults/DefaultLocale/DefaultLanguage" to skipElementWriter,
                "/PublicationDelivery/dataObjects/CompositeFrame/FrameDefaults/DefaultLocale" to defaultLocaleWriter,
            )
        )
    }
}
