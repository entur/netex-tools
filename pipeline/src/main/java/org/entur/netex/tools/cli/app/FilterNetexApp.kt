package org.entur.netex.tools.cli.app

import org.entur.netex.tools.cli.config.CliConfig
import org.entur.netex.tools.lib.io.XMLFiles.parseXmlDocuments
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.SimpleEntitySelection
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesPlugin
import org.entur.netex.tools.lib.sax.*
import org.entur.netex.tools.lib.selection.ActiveDatesSelector
import org.entur.netex.tools.lib.selection.PublicEntitiesSelector
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
    val selectors = listOf(
      { entities: Collection<Entity>, entitySelection: SimpleEntitySelection -> PublicEntitiesSelector().selector(entities) },
      { entities: Collection<Entity>, entitySelection: SimpleEntitySelection -> ActiveDatesSelector(activeDatesPlugin, model, config.period!!.start, config.period!!.end).selector(entitySelection) },
      { entities: Collection<Entity>, entitySelection: SimpleEntitySelection -> UnreferencedEntityPruningSelector(model = model).selector(entitySelection) }
    )
    val selection = selectEntitiesToKeep(selectors)

    // Step 3: export the filtered data to XML files
    exportXmlFiles(selection)

    printReport(selection)
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

  private fun selectEntitiesToKeep(selectors: List<(Collection<Entity>, SimpleEntitySelection) -> SimpleEntitySelection>): SimpleEntitySelection {
    val allEntities = model.listAllEntities()
    val allEntitiesMap = mutableMapOf<String, MutableMap<String, Entity>>()
    allEntities.forEach { entity ->
      allEntitiesMap.computeIfAbsent(entity.type) { mutableMapOf() }[entity.id] = entity
    }
    val allEntitiesSelection = SimpleEntitySelection(allEntitiesMap)

    val result = selectors.map { selector -> selector(allEntities, allEntitiesSelection) }
      .reduce { acc, selection -> selection.intersectWith(acc) }
    return result
  }

  private fun exportXmlFiles(selection : SimpleEntitySelection) {
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
      createNetexSaxWriteHandler(outFile, selection)
    }
  }

  private fun printReport(selection: SimpleEntitySelection) {
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

  private fun createNetexSaxWriteHandler(file: File, selection: SimpleEntitySelection) = OutputNetexSaxHandler(
    file,
    SkipEntityAndElementHandler(skipElements, selection),
    config.preserveComments)
}
