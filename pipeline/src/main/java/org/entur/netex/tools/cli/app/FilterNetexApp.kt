package org.entur.netex.tools.cli.app

import org.entur.netex.tools.cli.config.CliConfig
import org.entur.netex.tools.lib.io.XMLFiles.parseXmlDocuments
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.EntitySelection
import org.entur.netex.tools.lib.model.PublicationEnumeration
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
  val activeDatesModel: ActiveDatesModel = ActiveDatesModel()

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
    selection.remove {
      it.publication != PublicationEnumeration.PUBLIC.value
    }
    config.period?.let {
      selection.removeAll(activeDatesModel.getEntitiesInactiveAfter(it.end))
    }
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

  private fun createNetexSaxReadHandler() = BuildEntityModelSaxHandler(model, SkipElementHandler(skipElements), activeDatesModel)

  private fun createNetexSaxWriteHandler(file: File) = OutputNetexSaxHandler(file, SkipEntityAndElementHandler(skipElements, selection), config.preserveComments)

}
