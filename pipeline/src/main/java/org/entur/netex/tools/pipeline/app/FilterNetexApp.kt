package org.entur.netex.tools.pipeline.app

import org.entur.netex.tools.lib.config.FilterConfig
import org.entur.netex.tools.lib.config.CliConfig
import org.entur.netex.tools.lib.io.XMLFiles.parseXmlDocuments
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.selections.EntitySelection
import org.entur.netex.tools.lib.selections.RefSelection
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesPlugin
import org.entur.netex.tools.lib.sax.*
import org.entur.netex.tools.lib.selectors.entities.ActiveDatesSelector
import org.entur.netex.tools.lib.selectors.entities.AllEntitiesSelector
import org.entur.netex.tools.lib.selectors.entities.EntitySelector
import org.entur.netex.tools.lib.selectors.entities.EntityPruningSelector
import org.entur.netex.tools.lib.selectors.entities.PublicEntitiesSelector
import org.entur.netex.tools.lib.selectors.entities.ServiceJourneyInterchangeSelector
import org.entur.netex.tools.lib.selectors.entities.SkipElementsSelector
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

  // Plugin system
  private val activeDatesPlugin = ActiveDatesPlugin(ActiveDatesRepository())
  private val plugins = listOf(activeDatesPlugin)

  fun run(): Pair<Set<String>, Set<String>> {
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

    return Pair(entitiesToKeep.allIds(), refSelection.selection.map { it.ref }.toSet())
  }

  val skipElementsSelector = SkipElementsSelector(skipElements)
  val publicEntitiesSelector = PublicEntitiesSelector()
  val activeDatesSelector = ActiveDatesSelector(activeDatesPlugin, filterConfig.period)

  inline fun <T> timed(block: () -> T): Pair<T, Long> {
    var result: T
    val time = measureTimeMillis { result = block() }
    return result to time
  }

  private fun setupEntitySelectors(): List<EntitySelector> {
    val selectors = mutableListOf<EntitySelector>(AllEntitiesSelector())
    if (filterConfig.skipElements.isNotEmpty()) {
      selectors.add(skipElementsSelector)
    }
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
    logger.info("\nLoad xml files for building entity model")
    parseXmlDocuments(input) { file ->
      logger.info("  << ${file.absolutePath}")
      createNetexSaxReadHandler()
    }
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

    parseXmlDocuments(input) { file ->
      val outFile = File(target, file.name)
      logger.info("  >> ${outFile.absolutePath}")
      createNetexSaxWriteHandler(outFile, entitySelection, refSelection)
    }
  }

  private fun printReport(selection: EntitySelection) {
    if (cliConfig.printReport) {
      model.printEntities(selection)
      model.printReferences(selection)
    }
    logger.info("Filter NeTEx files done in ${(System.currentTimeMillis() - startTime)/1000.0} seconds.")
  }

  private fun createNetexSaxReadHandler() = BuildEntityModelSaxHandler(
    model, 
    SkipElementHandler(skipElements), 
    plugins,
  )

  private fun createNetexSaxWriteHandler(file: File, entitySelection: EntitySelection, refSelection: RefSelection) = OutputNetexSaxHandler(
    file,
    entityModel = model,
    SkipEntityAndElementHandler(entitySelection, refSelection),
    filterConfig.preserveComments,
    filterConfig.useSelfClosingTagsWhereApplicable,
  )
}
