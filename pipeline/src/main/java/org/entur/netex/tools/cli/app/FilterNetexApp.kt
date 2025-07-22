package org.entur.netex.tools.cli.app

import org.entur.netex.tools.cli.config.CliConfig
import org.entur.netex.tools.lib.io.XMLFiles.parseXmlDocuments
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.PublicationEnumeration
import org.entur.netex.tools.lib.model.SimpleEntitySelection
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesPlugin
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesCalculator
import org.entur.netex.tools.lib.sax.*
import org.entur.netex.tools.lib.utils.EntitySelectionUtils
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
    val selection = selectEntitiesToKeep()

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

  private fun selectEntitiesToKeep(): SimpleEntitySelection {
    val allEntities = model.listAllEntities()

    // mapping from type -> (id, entity)
    val allEntitiesMap = mutableMapOf<String, MutableMap<String, Entity>>()
    allEntities.forEach { entity ->
      allEntitiesMap.computeIfAbsent(entity.type) { mutableMapOf() }[entity.id] = entity
    }

    // mapping from type -> (id, entity)
    val publicEntitiesMap = mutableMapOf<String, MutableMap<String, Entity>>()
    if (config.removePrivateData) {
      allEntities.filter { it.publication == PublicationEnumeration.PUBLIC.value }.forEach { entity ->
        publicEntitiesMap.computeIfAbsent(entity.type) { mutableMapOf() }[entity.id] = entity
      }
    } else {
      publicEntitiesMap.putAll(allEntitiesMap)
    }

    val activeEntitiesMap = mutableMapOf<String, MutableMap<String, Entity>>()
    // mapping from type -> Set of IDs to keep
    val activeEntities = if (config.period != null) {
      val calculator = ActiveDatesCalculator(activeDatesPlugin.getCollectedData())
      calculator.activeDateEntitiesInPeriod(config.period!!.start, config.period!!.end, model)
    } else {
      // If no period is specified, keep all entities
      emptyMap()
    }

    allEntitiesMap.forEach { (type, entities) ->
      if (activeEntities.containsKey(type)) {
        val idsOfActiveEntitiesWithType = activeEntities[type]
        val entitiesToKeep = entities.filter { idsOfActiveEntitiesWithType?.contains(it.key) == true  }
        if (entitiesToKeep.isNotEmpty()) {
          activeEntitiesMap.put(type, entitiesToKeep.toMutableMap())
        }
      } else {
        // If no active entities for this type, keep all entities of this type
        activeEntitiesMap[type] = entities
      }
    }

    val activeEntitiesSelection = SimpleEntitySelection(activeEntitiesMap)
    val publicEntitiesSelection = SimpleEntitySelection(publicEntitiesMap)

    return activeEntitiesSelection.intersectWith(publicEntitiesSelection)
//    pruneUnreferencedEntities(mergedEntities, model)
  }
  
  private fun pruneUnreferencedEntities(selection: MutableMap<String, MutableMap<String, Entity>>, model: EntityModel) {
    // Define entity types that should be removed if not referenced
    // Order matters: remove from most dependent to least dependent
    val entityTypesToPrune = listOf(
      "Route",           // Routes that aren't used by any JourneyPattern
      "Line",            // Lines that aren't used by any ServiceJourney or Route  
      "JourneyPattern",  // JourneyPatterns that aren't used by any ServiceJourney
      "DestinationDisplay", // DestinationDisplays not used by any journey pattern or service journey
      "RoutePoint",      // RoutePoints not used by any Route
      "PointOnRoute",    // PointOnRoute not used by any Route
      "OperatingPeriod"  // OperatingPeriods not used by any DayTypeAssignment
    )
    
    Log.info("Pruning unreferenced entities...")
    EntitySelectionUtils.removeUnreferencedEntities(entityTypesToPrune, selection, model)
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
