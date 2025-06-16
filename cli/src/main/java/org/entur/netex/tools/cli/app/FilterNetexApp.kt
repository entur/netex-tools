package org.entur.netex.tools.cli.app

import org.entur.netex.tools.cli.config.CliConfig
import org.entur.netex.tools.lib.io.XMLFiles.parseXmlDocuments
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.EntitySelection
import org.entur.netex.tools.lib.model.NetexTypes.AUTHORITY
import org.entur.netex.tools.lib.model.NetexTypes.DAY_TYPE
import org.entur.netex.tools.lib.model.NetexTypes.DAY_TYPE_ASSIGNMENT
import org.entur.netex.tools.lib.model.NetexTypes.FLEX_LINE
import org.entur.netex.tools.lib.model.NetexTypes.LINE
import org.entur.netex.tools.lib.model.NetexTypes.PASSENGER_STOP_ASSIGNMENT
import org.entur.netex.tools.lib.model.NetexTypes.RESOURCE_FRAME
import org.entur.netex.tools.lib.model.NetexTypes.SCHEDULED_STOP_POINT
import org.entur.netex.tools.lib.model.NetexTypes.SERVICE_JOURNEY
import org.entur.netex.tools.lib.model.NetexTypes.STOP_POINT_IN_JOURNEY_PATTERN
import org.entur.netex.tools.lib.model.NetexTypes.TIMETABLED_PASSING_TIME
import org.entur.netex.tools.lib.sax.BuildEntityModelSaxHandler
import org.entur.netex.tools.lib.sax.OutputNetexSaxHandler
import org.entur.netex.tools.lib.sax.SkipElementHandler
import org.entur.netex.tools.lib.sax.SkipEntityAndElementHandler
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
    Log.info("\nLoad xml files")
    parseXmlDocuments(input) { file ->
      Log.info("  << ${file.absolutePath}")
      createNetexSaxReadHandler()
    }
  }

  private fun selectEntitiesToKeep() {
    selectAll(LINE, config.lines)
    selection.selectType(SERVICE_JOURNEY).ifRefTargetSelected(LINE)
    selectAll(FLEX_LINE, config.flexLines)
    selectAll(SERVICE_JOURNEY, config.serviceJourneys)

    if(config.area != null) {
      throw IllegalStateException("Not implemented")
    }
    if(config.period != null) {
      throw IllegalStateException("Not implemented")
    }

    if(config.area == null) {
      // This can be made more user-friendly by building up a navigation map, so
      // the user does not need to know the direction and type of relation. E.g.:
      // link(
      //   ServiceJourney,
      //   TimetabledPassingTime,
      //   StopPointInJourneyPattern,
      //   PassengerStopAssignment,
      //   ScheduledStopPoint
      // )
      // We should validate that such link exist as well
      selection
        .selectType(TIMETABLED_PASSING_TIME).ifParentSelected(SERVICE_JOURNEY)
        .selectType(STOP_POINT_IN_JOURNEY_PATTERN).ifRefSourceSelected(TIMETABLED_PASSING_TIME)
        .selectType(SCHEDULED_STOP_POINT).ifRefSourceSelected(STOP_POINT_IN_JOURNEY_PATTERN)
        .selectType(PASSENGER_STOP_ASSIGNMENT).ifRefTargetSelected(SCHEDULED_STOP_POINT)
    }
    if(config.period == null) {
      selection.selectType(DAY_TYPE).ifRefSourceSelected(SERVICE_JOURNEY)
      selection.selectType(DAY_TYPE_ASSIGNMENT).ifRefTargetSelected(DAY_TYPE)
    }
    selection.selectAllReferencedEntities()
    selection.selectAllParents()
    selection.selectType(AUTHORITY).ifParentSelected(RESOURCE_FRAME)
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

  private fun selectAll(type : String, ids : List<String>) {
    selection.select(type, ids)
  }

  private fun createNetexSaxReadHandler() = BuildEntityModelSaxHandler(model, SkipElementHandler(skipElements))

  private fun createNetexSaxWriteHandler(file: File) = OutputNetexSaxHandler(file, SkipEntityAndElementHandler(skipElements, selection))

}
