package org.entur.netex.tools.cli.app

import org.entur.netex.tools.cli.config.CliConfig
import org.entur.netex.tools.lib.io.XMLFiles.parseXmlDocuments
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.EntitySelection
import org.entur.netex.tools.lib.model.PublicationEnumeration
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesRepository
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesPlugin
import org.entur.netex.tools.lib.plugin.activedates.ActiveDatesCalculator
import org.entur.netex.tools.lib.sax.*
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
  val selection = EntitySelection(model)

  // Plugin system
  private val activeDatesPlugin = ActiveDatesPlugin(ActiveDatesRepository())
  private val plugins = listOf(activeDatesPlugin)

  fun run() {
    setupAndLogStartupInfo()
    buildEntityModel()
    selectEntitiesToKeep()
    exportXmlFiles()
    printReport()
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

  private fun selectEntitiesToKeep() {
    selection.includeAll()
    if (config.removePrivateData) {
      selection.remove {
        it.publication != PublicationEnumeration.PUBLIC.value
      }
    }

    config.period?.let {
      val calculator = ActiveDatesCalculator(activeDatesPlugin.getCollectedData())
      val active = calculator.activeDateEntitiesInPeriod(config.period!!.start, config.period!!.end, model)
      selection.removeAllNotIn(active)
    }
    
    // Remove unreferenced entities after date-based filtering
    pruneUnreferencedEntities()
  }
  
  private fun pruneUnreferencedEntities() {
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
    selection.removeUnreferencedEntities(entityTypesToPrune)
  }

  private fun exportXmlFiles() {
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
      createNetexSaxWriteHandler(outFile)
    }
  }

  private fun printReport() {
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

  private fun createNetexSaxWriteHandler(file: File) = OutputNetexSaxHandler(
    file,
    SkipEntityAndElementHandler(skipElements, selection),
    config.preserveComments)
}
