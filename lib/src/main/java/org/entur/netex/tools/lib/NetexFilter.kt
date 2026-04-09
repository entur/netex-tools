package org.entur.netex.tools.lib

import org.entur.netex.tools.lib.config.CliConfig
import org.entur.netex.tools.lib.config.FilterConfig
import org.entur.netex.tools.lib.io.XMLFiles.parseXmlDocuments
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.output.*
import org.entur.netex.tools.lib.plugin.NetexFileWriterContext
import org.entur.netex.tools.lib.report.FileIndex
import org.entur.netex.tools.lib.report.FilterReport
import org.entur.netex.tools.lib.sax.BuildEntityModelSaxHandler
import org.entur.netex.tools.lib.sax.OutputNetexSaxHandler
import org.entur.netex.tools.lib.selections.EntitySelection
import org.entur.netex.tools.lib.selections.InclusionPolicy
import org.entur.netex.tools.lib.selections.RefSelection
import org.entur.netex.tools.lib.selectors.entities.CompositeEntitySelector
import org.entur.netex.tools.lib.selectors.entities.EntitySelectorContext
import org.entur.netex.tools.lib.selectors.refs.CompositeRefSelector
import org.slf4j.LoggerFactory
import java.io.File

class NetexFilter(
    val cliConfig: CliConfig = CliConfig(),
    val filterConfig: FilterConfig = FilterConfig(),
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private var _model = EntityModel(cliConfig.alias())
    val model: EntityModel get() = _model

    private var _fileIndex = FileIndex()
    val fileIndex: FileIndex get() = _fileIndex

    // --- Pass 1: Build Entity Model ---

    fun buildEntityModel(inputDir: File) {
        resetModel()
        logger.info("Building entity model from directory: ${inputDir.absolutePath}")
        parseXmlDocuments(inputDir) { file ->
            logger.info("Building entity model for file ${file.name}")
            createReadHandler(file)
        }
        logModelSize()
    }

    fun buildEntityModel(documents: Map<String, ByteArray>) {
        resetModel()
        logger.info("Building entity model from ${documents.size} in-memory documents")
        parseXmlDocuments(documents) { name ->
            logger.info("Building entity model for document $name")
            createReadHandlerForDocument(name)
        }
        logModelSize()
    }

    private fun resetModel() {
        _model = EntityModel(cliConfig.alias())
    }

    private fun logModelSize() {
        logger.info(
            "Entity model contains ${model.listAllEntities().size} entities " +
                "and ${model.listAllRefs().size} references."
        )
    }

    // --- Entity/Ref Selection ---

    fun selectEntities(): Pair<EntitySelection, RefSelection> {
        val entitiesToKeep = CompositeEntitySelector(filterConfig).selectEntities(
            EntitySelectorContext(entityModel = model)
        )
        val refsToKeep = CompositeRefSelector(filterConfig, entitiesToKeep).selectRefs(model)
        return entitiesToKeep to refsToKeep
    }

    // --- Pass 2: Export ---

    fun exportToFiles(
        inputDir: File,
        targetDir: File,
        entitySelection: EntitySelection,
        refSelection: RefSelection,
    ): FilterReport {
        resetFileIndex()
        if (!targetDir.exists()) targetDir.mkdirs()
        require(!targetDir.isFile) { "Target is not a directory: ${targetDir.absolutePath}" }

        logger.info("Writing filtered XML files to ${targetDir.absolutePath}")
        parseXmlDocuments(inputDir) { file ->
            val outFile = resolveOutputFile(targetDir, file.name)
            logger.info("Writing output XML to ${outFile.name}")
            val writeOutput = XmlOutputStrategy { XMLFileWriter().writeToFile(it) }
            createWriteHandler(outFile, entitySelection, refSelection, writeOutput)
        }
        logger.info("Done writing filtered XML files to ${targetDir.absolutePath}")
        return buildReport()
    }

    fun exportToByteArrays(
        documents: Map<String, ByteArray>,
        entitySelection: EntitySelection,
        refSelection: RefSelection,
    ): ExportResult {
        resetFileIndex()
        val output = mutableMapOf<String, ByteArray>()

        logger.info("Exporting ${documents.size} documents to byte arrays")
        parseXmlDocuments(documents) { name ->
            val outName = filterConfig.fileNameMap[name] ?: name
            logger.info("Exporting document $outName")
            val writeOutput = XmlOutputStrategy { ctx ->
                output[outName] = ctx.stringWriter.toString().toByteArray(Charsets.UTF_8)
            }
            createWriteHandlerForDocument(outName, entitySelection, refSelection, writeOutput)
        }
        return ExportResult(documents = output, report = buildReport())
    }

    private fun resetFileIndex() {
        _fileIndex = FileIndex()
    }

    // --- Convenience: full pipeline ---

    fun run(inputDir: File, targetDir: File): FilterReport {
        buildEntityModel(inputDir)
        val (entitySelection, refSelection) = selectEntities()
        return exportToFiles(inputDir, targetDir, entitySelection, refSelection)
    }

    fun run(documents: Map<String, ByteArray>): ExportResult {
        buildEntityModel(documents)
        val (entitySelection, refSelection) = selectEntities()
        return exportToByteArrays(documents, entitySelection, refSelection)
    }

    // --- Internal helpers ---

    private fun resolveOutputFile(targetDir: File, inputFileName: String): File {
        val newFileName = filterConfig.fileNameMap[inputFileName]
        if (newFileName != null) {
            val outFile = File(targetDir, newFileName)
            if (!outFile.exists()) outFile.createNewFile()
            return outFile
        }
        return File(targetDir, inputFileName)
    }

    private fun createReadHandler(file: File) = BuildEntityModelSaxHandler(
        entityModel = model,
        inclusionPolicy = InclusionPolicy(
            entitySelection = null,
            refSelection = null,
            skipElements = filterConfig.skipElements,
        ),
        file = file,
        plugins = filterConfig.plugins,
    )

    private fun createReadHandlerForDocument(documentName: String) = BuildEntityModelSaxHandler(
        entityModel = model,
        inclusionPolicy = InclusionPolicy(
            entitySelection = null,
            refSelection = null,
            skipElements = filterConfig.skipElements,
        ),
        documentName = documentName,
        plugins = filterConfig.plugins,
    )

    private fun createWriteHandler(
        outputFile: File,
        entitySelection: EntitySelection,
        refSelection: RefSelection,
        writeOutput: XmlOutputStrategy,
    ): OutputNetexSaxHandler {
        val xmlContext = XmlContext(xmlFile = outputFile)
        val inclusionPolicy = InclusionPolicy(entitySelection, refSelection, filterConfig.skipElements)
        return buildWriteHandler(xmlContext, writeOutput) { fileWriter, elementWriter ->
            OutputNetexSaxHandler(
                entityModel = model,
                fileIndex = fileIndex,
                inclusionPolicy = inclusionPolicy,
                fileWriter = fileWriter,
                outputFile = outputFile,
                elementWriter = elementWriter,
                elementsRequiredChildren = filterConfig.elementsRequiredChildren,
            )
        }
    }

    private fun createWriteHandlerForDocument(
        documentName: String,
        entitySelection: EntitySelection,
        refSelection: RefSelection,
        writeOutput: XmlOutputStrategy,
    ): OutputNetexSaxHandler {
        val xmlContext = XmlContext(documentName)
        val inclusionPolicy = InclusionPolicy(entitySelection, refSelection, filterConfig.skipElements)
        return buildWriteHandler(xmlContext, writeOutput) { fileWriter, elementWriter ->
            OutputNetexSaxHandler(
                entityModel = model,
                fileIndex = fileIndex,
                inclusionPolicy = inclusionPolicy,
                fileWriter = fileWriter,
                documentName = documentName,
                elementWriter = elementWriter,
                elementsRequiredChildren = filterConfig.elementsRequiredChildren,
            )
        }
    }

    private fun buildWriteHandler(
        xmlContext: XmlContext,
        writeOutput: XmlOutputStrategy,
        factory: (NetexFileWriter, DelegatingXMLElementWriter) -> OutputNetexSaxHandler,
    ): OutputNetexSaxHandler {
        val fileWriter = NetexFileWriter(
            netexFileWriterContext = NetexFileWriterContext(
                useSelfClosingTagsWhereApplicable = filterConfig.useSelfClosingTagsWhereApplicable,
                removeEmptyCollections = true,
                preserveComments = filterConfig.preserveComments,
            ),
            xmlContext = xmlContext,
            writeOutput = writeOutput,
        )
        val elementWriter = DelegatingXMLElementWriter(
            handlers = filterConfig.customElementHandlers,
            xmlContext = xmlContext,
        )
        return factory(fileWriter, elementWriter)
    }

    private fun buildReport() = FilterReport(
        entitiesByFile = fileIndex.entitiesByFile,
        elementTypesByFile = fileIndex.elementTypesByFile,
        entitiesByDocument = fileIndex.entitiesByDocument,
        elementTypesByDocument = fileIndex.elementTypesByDocument,
    )
}
