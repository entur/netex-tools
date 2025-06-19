package org.entur.netex.tools.cli.app

import org.entur.netex.tools.cli.config.CliConfig
import org.entur.netex.tools.lib.io.XMLFiles.parseXmlDocuments
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.EntitySelection
import org.entur.netex.tools.lib.sax.*
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
  val selection = EntitySelection(model)

  val operatingDayToCalendarDateMap = mutableMapOf<String, LocalDate>()
  val dayTypeRefToDateMap = mutableMapOf<String, LocalDate>()
  val dayTypeRefToOperatingDayRefMap = mutableMapOf<String, String>()
  val dayTypeRefToOperatingPeriodRefMap = mutableMapOf<String, String>()

  fun run() {
    setupAndLogStartupInfo()
    buildEntityModel()
    buildActiveDatesModel()
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

  private fun buildActiveDatesModel() {
    Log.info("\nLoad xml files for collecting active dates")
    parseXmlDocuments(input) { file ->
      Log.info("Collecting operatingDay to calendarDate")
      createActiveDatesCollectionHandler()
    }
  }

  private fun selectEntitiesToKeep() {
    selection.includePublicEntities()
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

  private fun addOperatingDayToCalendarDateMapEntry(operatingDay: String, calendarDate: LocalDate) = operatingDayToCalendarDateMap.put(operatingDay, calendarDate)

  private fun addDayTypeRefToDateEntry(dayTypeRef: String, date: LocalDate) = dayTypeRefToDateMap.put(dayTypeRef, date)

  private fun addDayTypeRefToOperatingDayRefEntry(dayTypeRef: String, operatingDayRef: String) = dayTypeRefToOperatingDayRefMap.put(dayTypeRef, operatingDayRef)

  private fun addDayTypeRefToOperatingPeriodRefEntry(dayTypeRef: String, operatingPeriodRef: String) = dayTypeRefToOperatingPeriodRefMap.put(dayTypeRef, operatingPeriodRef)

  private fun createNetexSaxReadHandler() = BuildEntityModelSaxHandler(model, SkipElementHandler(skipElements))

  private fun createActiveDatesCollectionHandler() = BuildActiveDatesCollectionHandler(
    ::addOperatingDayToCalendarDateMapEntry,
    ::addDayTypeRefToDateEntry,
    ::addDayTypeRefToOperatingDayRefEntry,
    ::addDayTypeRefToOperatingPeriodRefEntry,
  )

  private fun createNetexSaxWriteHandler(file: File) = OutputNetexSaxHandler(file, SkipEntityAndElementHandler(skipElements, selection))

}
