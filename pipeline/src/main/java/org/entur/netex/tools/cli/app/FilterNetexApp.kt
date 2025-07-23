package org.entur.netex.tools.cli.app

import org.entur.netex.tools.cli.config.CliConfig
import org.entur.netex.tools.lib.io.XMLFiles.parseXmlDocuments
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.EntitySelection
import org.entur.netex.tools.lib.model.RefSelection
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesPlugin
import org.entur.netex.tools.lib.sax.*
import org.entur.netex.tools.lib.selection.ActiveDatesSelector
import org.entur.netex.tools.lib.selection.PublicEntitiesSelector
import org.entur.netex.tools.lib.selection.SkipElementsSelector
import org.entur.netex.tools.lib.selection.UnreferencedEntityPruningSelector
import org.entur.netex.tools.lib.utils.Log
import java.io.File

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
    val entitySelection = selectEntitiesToKeep(selectors)
    val refSelection = selectRefsToKeep(entitySelection)

    // Step 3: export the filtered data to XML files
    exportXmlFiles(entitySelection, refSelection)

    printReport(entitySelection)
  }

  val skipElementsSelector = { entities: Collection<Entity>, _: EntitySelection ->
    SkipElementsSelector(skipElements).selector(entities)
  }

  val publicEntitiesSelector = { entities: Collection<Entity>, _: EntitySelection ->
    PublicEntitiesSelector().selector(entities)
  }

  val activeDatesSelector = { _: Collection<Entity>, selection: EntitySelection ->
    ActiveDatesSelector(activeDatesPlugin, model, config.period!!.start, config.period!!.end).selector(selection)
  }

  val unreferencedEntityPruningSelector = { _: Collection<Entity>, selection: EntitySelection ->
    UnreferencedEntityPruningSelector(model = model).selector(selection)
  }

  private fun setupSelectors(): List<(Collection<Entity>, EntitySelection) -> EntitySelection> {
    val selectors = mutableListOf<(Collection<Entity>, EntitySelection) -> EntitySelection>()
    if (config.skipElements.isNotEmpty()) {
      selectors.add(skipElementsSelector)
    }
    if (config.removePrivateData) {
        selectors.add(publicEntitiesSelector)
    }
    if (config.period != null) {
        selectors.add(activeDatesSelector)
    }
    selectors.add(unreferencedEntityPruningSelector)
    return selectors
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

  private fun selectEntitiesToKeep(selectors: List<(Collection<Entity>, EntitySelection) -> EntitySelection>): EntitySelection {
    val allEntities = model.listAllEntities()

    // maps from type -> (id, Entity)
    val allEntitiesMap = mutableMapOf<String, MutableMap<String, Entity>>()
    allEntities.forEach { entity ->
      allEntitiesMap.computeIfAbsent(entity.type) { mutableMapOf() }[entity.id] = entity
    }

    val allEntitiesSelection = EntitySelection(allEntitiesMap)

    // Runs each selector and combines the results by intersecting the selections.
    val result = selectors
      .map { selector -> selector(allEntities, allEntitiesSelection) }
      .reduce { acc, selection -> selection.intersectWith(acc) }

    return result
  }

  private fun selectRefsToKeep(entitySelection: EntitySelection): RefSelection {
    val allRefs = model.listAllRefs()
    val allEntityIds = entitySelection.allIds()
    val refsToKeep = allRefs.filter { allEntityIds.contains(it.ref) }.toHashSet()
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
    SkipEntityAndElementHandler(skipElements, entitySelection, refSelection),
    config.preserveComments
  )
}
