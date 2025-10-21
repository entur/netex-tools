package org.entur.netex.tools.pipeline.app

import org.entur.netex.tools.lib.config.FilterConfig
import org.entur.netex.tools.lib.config.CliConfig
import org.entur.netex.tools.lib.io.XMLFiles.parseXmlDocuments
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.plugin.DefaultNetexFileWriter
import org.entur.netex.tools.lib.plugin.NetexFileWriterContext
import org.entur.netex.tools.lib.plugin.NetexPlugin
import org.entur.netex.tools.lib.selections.EntitySelection
import org.entur.netex.tools.lib.selections.RefSelection
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesPlugin
import org.entur.netex.tools.lib.plugin.file.FileNamePlugin
import org.entur.netex.tools.lib.report.FileIndex
import org.entur.netex.tools.lib.report.FilterReport
import org.entur.netex.tools.lib.sax.*
import org.entur.netex.tools.lib.selections.InclusionPolicy
import org.entur.netex.tools.lib.selectors.entities.ActiveDatesSelector
import org.entur.netex.tools.lib.selectors.entities.AllEntitiesSelector
import org.entur.netex.tools.lib.selectors.entities.EntitySelector
import org.entur.netex.tools.lib.selectors.entities.EntityPruningSelector
import org.entur.netex.tools.lib.selectors.entities.PublicEntitiesSelector
import org.entur.netex.tools.lib.selectors.entities.ServiceJourneyInterchangeSelector
import org.entur.netex.tools.lib.selectors.refs.ActiveDatesRefSelector
import org.entur.netex.tools.lib.selectors.refs.AllRefsSelector
import org.entur.netex.tools.lib.selectors.refs.RefPruningSelector
import org.entur.netex.tools.lib.selectors.refs.RefSelector
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.system.measureTimeMillis
import kotlin.time.measureTime

data class FilterNetexApp(
  val cliConfig : CliConfig = CliConfig(),
  val filterConfig: FilterConfig = FilterConfig(),
  val input : File,
  val target : File
) {
  private val logger = LoggerFactory.getLogger(javaClass)

  val skipElements = filterConfig.skipElements.toHashSet()
  val startTime = System.currentTimeMillis()
  val model = EntityModel(cliConfig.alias())
  val fileIndex = FileIndex()

  // Plugin system
  private val activeDatesRepository = ActiveDatesRepository()
  private val activeDatesPlugin = ActiveDatesPlugin(activeDatesRepository)

  fun run(): FilterReport {
    setupAndLogStartupInfo()

    // Step 1: collect data needed for filtering out entities
    buildEntityModel()

    // Step 2: filter data based on configuration and data collected in step 1
    val entitySelectors = setupEntitySelectors()

    var entitiesToKeep = selectEntitiesToKeep(entitySelectors)

    entitiesToKeep = if (filterConfig.unreferencedEntitiesToPrune.isEmpty()) {
      entitiesToKeep
    } else {
      logger.info("Pruning unreferenced entities...")
      val (prunedEntitySelection, ms) = timed {
        pruneUnreferencedEntities(entitiesToKeep)
      }
      logger.info("Pruned unreferenced entities in $ms")
      prunedEntitySelection
    }

    logger.info("Removing interchanges without service journeys...")
    val (result, ms) = timed {
      ServiceJourneyInterchangeSelector(entitiesToKeep)
      .selectEntities(model)
      .intersectWith(entitiesToKeep)
    }
    logger.info("Removed interchanges without service journeys in $ms")
    entitiesToKeep = result

    val refSelectors = setupRefSelectors(entitiesToKeep)
    val refSelection = selectRefsToKeep(refSelectors)

    // Step 3: export the filtered data to XML files
    exportXmlFiles(entitiesToKeep, refSelection)

    printReport(entitiesToKeep)

    return FilterReport(
        entitiesByFile = fileIndex.entitiesByFile,
        elementTypesByFile = fileIndex.elementTypesByFile,
    )
  }

  val publicEntitiesSelector = PublicEntitiesSelector()
  val activeDatesSelector = ActiveDatesSelector(activeDatesPlugin, filterConfig.period)

  inline fun <T> timed(block: () -> T): Pair<T, Long> {
    var result: T
    val time = measureTimeMillis { result = block() }
    return result to time
  }

  private fun setupEntitySelectors(): List<EntitySelector> {
    val selectors = mutableListOf<EntitySelector>(AllEntitiesSelector())
    if (filterConfig.removePrivateData) {
        selectors.add(publicEntitiesSelector)
    }
    if (filterConfig.period.start != null || filterConfig.period.end != null) {
        selectors.add(activeDatesSelector)
    }
    return selectors
  }

  private fun setupRefSelectors(entitySelection: EntitySelection): List<RefSelector> {
    val selectors = mutableListOf<RefSelector>(AllRefsSelector())
    if (filterConfig.pruneReferences) {
      selectors.add(RefPruningSelector(entitySelection, filterConfig.referencesToExcludeFromPruning))
    }
    if (filterConfig.period.start != null || filterConfig.period.end != null) {
      selectors.add(ActiveDatesRefSelector(activeDatesPlugin, filterConfig.period))
    }
    return selectors
  }

  private fun pruneUnreferencedEntities(initialEntitySelection: EntitySelection): EntitySelection {
    val unreferencedEntityPruningSelector = EntityPruningSelector(
      entitySelection = initialEntitySelection,
      typesToRemove = filterConfig.unreferencedEntitiesToPrune
    )
    val prunedEntitySelection = unreferencedEntityPruningSelector
      .selectEntities(model)
      .intersectWith(initialEntitySelection)
    return prunedEntitySelection
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

  private fun selectEntitiesToKeep(selectors: List<EntitySelector>): EntitySelection {
    logger.info("Starting initial entity selection...")
    var entitySelection: EntitySelection
    val time = measureTime {
      entitySelection = selectors
        .map { selector ->
          logger.info("Running entity selector: ${selector::class.simpleName}")
          selector.selectEntities(model)
        }
        .reduce { acc, selection -> selection.intersectWith(acc) }
    }
    logger.info("Initial entity collection done in $time. Collected a total of ${entitySelection.allIds().size} entities")
    return entitySelection
  }

  private fun selectRefsToKeep(selectors: List<RefSelector>): RefSelection {
    logger.info("Starting ref selection...")
    var refSelection: RefSelection
    val time = measureTime {
      refSelection = selectors
        .map { selector ->
          logger.info("Running ref selector: ${selector::class.simpleName}")
          selector.selectRefs(model)
        }
        .reduce { acc, selection -> selection.intersectWith(acc) }
    }
    logger.info("Ref collection done in $time. Collected a total of ${refSelection.selection.size} references")
    return refSelection
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
      val newFileName = fileIndex.filesToRename[file.name]
      val outFile = File(target, newFileName ?: file.name)
      if (!outFile.exists()) {
          outFile.createNewFile()
      }
      createNetexSaxWriteHandler(outFile, entitySelection, refSelection)
    }
    logger.info("Done writing filtered xml files to ${target.absolutePath}")
  }

  private fun printReport(selection: EntitySelection) {
    if (cliConfig.printReport) {
      logger.info(model.getEntitiesKeptReport(selection))
      logger.info(model.getRefsKeptReport(selection))
    }
    logger.info("Filter NeTEx files done in ${(System.currentTimeMillis() - startTime)/1000.0} seconds.")
  }

  private fun createNetexSaxReadHandler(file: File): BuildEntityModelSaxHandler {
      val fileNamePlugin = FileNamePlugin(
          currentFile = file,
          fileIndex = fileIndex,
      )
      val plugins = listOf<NetexPlugin>(activeDatesPlugin, fileNamePlugin)
      return BuildEntityModelSaxHandler(
          entities = model,
          skipHandler = SkipElementHandler(skipElements),
          skipElements = skipElements,
          plugins = plugins,
      )
  }

  private fun createNetexSaxWriteHandler(file: File, entitySelection: EntitySelection, refSelection: RefSelection): OutputNetexSaxHandler {
      val netexFileWriterContext = NetexFileWriterContext(
          file = file,
          useSelfClosingTagsWhereApplicable = filterConfig.useSelfClosingTagsWhereApplicable,
          removeEmptyCollections = true,
          preserveComments = filterConfig.preserveComments,
          period = filterConfig.period,
      )

      val defaultNetexFileWriter = DefaultNetexFileWriter(netexFileWriterContext)

      val inclusionPolicy = InclusionPolicy(
          entityModel = model,
          entitySelection = entitySelection,
          refSelection = refSelection,
          skipElements = filterConfig.skipElements
      )

      return OutputNetexSaxHandler(
          entityModel = model,
          fileIndex = fileIndex,
          inclusionPolicy = inclusionPolicy,
          netexFileWriter = defaultNetexFileWriter,
          outputFile = file,
      )
  }
}
