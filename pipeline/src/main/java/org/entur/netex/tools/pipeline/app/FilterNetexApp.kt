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
import org.entur.netex.tools.lib.selectors.entities.SkipElementsSelector
import org.entur.netex.tools.lib.selectors.refs.ActiveDatesRefSelector
import org.entur.netex.tools.lib.selectors.refs.AllRefsSelector
import org.entur.netex.tools.lib.selectors.refs.RefPruningSelector
import org.entur.netex.tools.lib.selectors.refs.RefSelector
import org.entur.netex.tools.lib.utils.Log
import java.io.File

data class FilterNetexApp(
  val cliConfig : CliConfig = CliConfig(),
  val filterConfig: FilterConfig = FilterConfig(),
  val input : File,
  val target : File
) {
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
      pruneUnreferencedEntities(entitiesToKeep)
    }

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
      unreferencedEntitiesToRemove = filterConfig.unreferencedEntitiesToPrune
    )
    val prunedEntitySelection = unreferencedEntityPruningSelector
      .selectEntities(model)
      .intersectWith(initialEntitySelection)
    return prunedEntitySelection
  }

  private fun setupAndLogStartupInfo() {
    Log.printLevel = cliConfig.logLevel
    Log.info("Config:\n$cliConfig")
    Log.info("Read inout from file: $input")
    Log.info("Write output to: ${target.absolutePath}")
  }

  private fun buildEntityModel() {
    Log.info("\nLoad xml files for building entity model")
    parseXmlDocuments(input) { file ->
      Log.info("  << ${file.absolutePath}")
      createNetexSaxReadHandler()
    }
  }

  private fun selectEntitiesToKeep(selectors: List<EntitySelector>): EntitySelection =
    selectors
      .map { selector -> selector.selectEntities(model) }
      .reduce { acc, selection -> selection.intersectWith(acc) }

  private fun selectRefsToKeep(selectors: List<RefSelector>): RefSelection =
    selectors
      .map { selector -> selector.selectRefs(model) }
      .reduce { acc, selection -> selection.intersectWith(acc) }

  private fun exportXmlFiles(entitySelection : EntitySelection, refSelection : RefSelection) {
    Log.info("Save xml files")
    if(!target.exists()) {
      target.mkdirs()
    }
    if(target.isFile) {
      throw IllegalArgumentException("Target file is not a directory : ${target.absolutePath}")
    }

    parseXmlDocuments(input) { file ->
      val outFile = File(target, file.name)
      Log.info("  >> ${outFile.absolutePath}")
      createNetexSaxWriteHandler(outFile, entitySelection, refSelection)
    }
  }

  private fun printReport(selection: EntitySelection) {
    if (cliConfig.printReport) {
      model.printEntities(selection)
      model.printReferences(selection)
    }
    println("Filter NeTEx files done in ${(System.currentTimeMillis() - startTime)/1000.0} seconds.")
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
