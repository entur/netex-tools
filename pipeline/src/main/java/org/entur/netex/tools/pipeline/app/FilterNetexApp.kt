package org.entur.netex.tools.pipeline.app

import org.entur.netex.tools.lib.config.FilterConfig
import org.entur.netex.tools.lib.config.CliConfig
import org.entur.netex.tools.lib.io.XMLFiles.parseXmlDocuments
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.entur.netex.tools.lib.output.NetexFileWriter
import org.entur.netex.tools.lib.plugin.NetexFileWriterContext
import org.entur.netex.tools.lib.output.XmlContext
import org.entur.netex.tools.lib.selections.EntitySelection
import org.entur.netex.tools.lib.selections.RefSelection
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

    fun run(): FilterReport {
        setupAndLogStartupInfo()

        val (entitySelection, seconds) = timedSeconds {
            // Step 1: collect data needed for filtering out entities
            buildEntityModel()

            // Step 2: select the entities and refs to keep
            val entitiesToKeep = CompositeEntitySelector(filterConfig).selectEntities(model)
            val refsToKeep = CompositeRefSelector(filterConfig, entitiesToKeep).selectRefs(model)

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
            logger.info("Building entity model for file ${it.name}")
            createNetexSaxReadHandler(it)
        }
        logger.info("Done reading xml files for building entity model. Model contains ${model.listAllEntities().size} entities and ${model.listAllRefs().size} references.")
    }

    private fun getOutputXmlFile(directory: File, file: File): File {
        val newFileName = filterConfig.fileNameMap[file.name]
        if (newFileName != null) {
            val outFile = File(target, newFileName)
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
            logger.info("Writing output XML to ${outFile.name}")
            val xmlContext = XmlContext(xmlFile = outFile)
            createNetexSaxWriteHandler(xmlContext, entitySelection, refSelection)
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

    private fun createNetexSaxReadHandler(file: File): BuildEntityModelSaxHandler =
        BuildEntityModelSaxHandler(
            entityModel = model,
            plugins = filterConfig.plugins,
            file = file,
            inclusionPolicy = InclusionPolicy(
                entitySelection = null,
                refSelection = null,
                skipElements = filterConfig.skipElements
            )
        )

    private fun createNetexSaxWriteHandler(xmlContext: XmlContext, entitySelection: EntitySelection, refSelection: RefSelection): OutputNetexSaxHandler {
        val netexFileWriterContext = NetexFileWriterContext(
            useSelfClosingTagsWhereApplicable = filterConfig.useSelfClosingTagsWhereApplicable,
            removeEmptyCollections = true,
            preserveComments = filterConfig.preserveComments,
        )

        val defaultNetexFileWriter = NetexFileWriter(
            netexFileWriterContext = netexFileWriterContext,
            xmlContext = xmlContext,
        )

        val delegatingXMLElementWriter = DelegatingXMLElementWriter(
            handlers = filterConfig.customElementHandlers,
            xmlContext = xmlContext,
        )

        return OutputNetexSaxHandler(
            entityModel = model,
            fileIndex = fileIndex,
            inclusionPolicy = InclusionPolicy(
                entitySelection = entitySelection,
                refSelection = refSelection,
                skipElements = filterConfig.skipElements
            ),
            fileWriter = defaultNetexFileWriter,
            outputFile = xmlContext.xmlFile,
            elementWriter = delegatingXMLElementWriter,
        )
    }
}
