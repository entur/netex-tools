package org.entur.netex.tools.cli.app

import org.entur.netex.tools.cli.config.CliConfig
import org.entur.netex.tools.lib.io.XMLFiles.parseXmlDocuments
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesPlugin
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.sax.BuildEntityModelSaxHandler
import org.entur.netex.tools.lib.sax.OutputNetexSaxHandler
import org.entur.netex.tools.lib.sax.SkipElementHandler
import org.entur.netex.tools.lib.sax.SkipEntityAndElementHandler
import org.entur.netex.tools.lib.selections.EntitySelection
import org.entur.netex.tools.lib.selections.RefSelection
import org.entur.netex.tools.lib.selectors.ActiveDatesSelector
import org.entur.netex.tools.lib.selectors.EntityPruningSelector
import org.entur.netex.tools.lib.selectors.EntitySelector
import org.entur.netex.tools.lib.selectors.PublicEntitiesSelector
import org.entur.netex.tools.lib.selectors.SkipElementsSelector
import org.entur.netex.tools.lib.utils.Log
import java.io.File
import java.time.LocalDate

data class FilterNetexApp(
  val config : CliConfig,
  val input : File,
  val target : File
) {
  val skipElements = config.skipElements.toHashSet()
  val startTime = System.currentTimeMillis()
  val model = EntityModel(config.alias())

  // Plugin system
  private val activeDatesPlugin = ActiveDatesPlugin(ActiveDatesRepository())
  private val plugins = listOf(activeDatesPlugin)

  fun run() {
    setupAndLogStartupInfo()

    // Step 1: collect data needed for filtering out entities
    buildEntityModel()

    // Step 2: filter data based on configuration and data collected in step 1
    val selectors = setupSelectors()

    val selectionOfEntitiesToKeep = selectEntitiesToKeep(selectors)
    val prunedSelectionOfEntitiesToKeep = pruneUnreferencedEntities(selectionOfEntitiesToKeep)
    val refSelection = selectRefsToKeep(prunedSelectionOfEntitiesToKeep)

    // Step 3: export the filtered data to XML files
    exportXmlFiles(prunedSelectionOfEntitiesToKeep, refSelection)

    printReport(prunedSelectionOfEntitiesToKeep)
  }

  val skipElementsSelector = SkipElementsSelector(skipElements)
  val publicEntitiesSelector = PublicEntitiesSelector()
  val activeDatesSelector = ActiveDatesSelector(activeDatesPlugin, LocalDate.parse(config.period!!.start) , LocalDate.parse(config.period!!.end))

  private fun setupSelectors(): List<EntitySelector> {
    val selectors = mutableListOf<EntitySelector>()
    if (config.skipElements.isNotEmpty()) {
      selectors.add(skipElementsSelector)
    }
    if (config.removePrivateData) {
      selectors.add(publicEntitiesSelector)
    }
    if (config.period != null) {
      selectors.add(activeDatesSelector)
    }
    return selectors
  }

  private fun pruneUnreferencedEntities(initialEntitySelection: EntitySelection): EntitySelection {
    val unreferencedEntityPruningSelector = EntityPruningSelector(initialEntitySelection)
    val prunedEntitySelection = unreferencedEntityPruningSelector
      .selectEntities(model)
      .intersectWith(initialEntitySelection)
    return prunedEntitySelection
  }

  private fun setupAndLogStartupInfo() {
    Log.printLevel = config.logLevel
    Log.info("Config:\n$config")
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

  private fun selectRefsToKeep(entitySelection: EntitySelection): RefSelection {
    val allRefs = model.listAllRefs()
    val allEntityIds = entitySelection.allIds()
    val refTypesToKeep = setOf("QuayRef")
    val refsToKeep = allRefs.filter { allEntityIds.contains(it.ref) || it.type in refTypesToKeep }.toHashSet()
    return RefSelection(refsToKeep)
  }

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
    if (config.printReport) {
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
    SkipEntityAndElementHandler(entitySelection, refSelection),
    config.preserveComments
  )
}

