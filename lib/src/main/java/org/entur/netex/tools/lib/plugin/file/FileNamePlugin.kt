package org.entur.netex.tools.lib.plugin.file

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.plugin.AbstractNetexPlugin
import org.entur.netex.tools.lib.report.FileIndex
import org.xml.sax.Attributes
import java.io.File

class FileNamePlugin(
    private val currentFile: File,
    private val fileIndex: FileIndex,
    private val context: FileNamePluginContext = FileNamePluginContext(),
): AbstractNetexPlugin() {
    var currentEntityType: String? = null

    override fun startElement(elementName: String, attributes: Attributes?, currentEntity: Entity?) {
        if (attributes?.getValue("id") != null) {
            currentEntityType = elementName
        }
        if (elementName == "FlexibleLine" || elementName == "Line") {
            context.lineType = elementName
        }
    }

    override fun characters(elementName: String, ch: CharArray?, start: Int, length: Int) {
        if (currentEntityType == "FlexibleLine" || currentEntityType == "Line") {
            when (elementName) {
                "Name" -> context.lineName.append(String(ch!!, start, length))
                "PublicCode" -> context.linePublicCode.append(String(ch!!, start, length))
                "PrivateCode" -> context.linePrivateCode.append(String(ch!!, start, length))
            }
        }
        if (elementName == "ParticipantRef") {
            context.codespace.append(String(ch!!, start, length))
        }
    }

    override fun endElement(elementName: String, currentEntity: Entity?) {
        if (elementName == "FlexibleLine" || elementName == "Line") {
            currentEntityType = null
        }
    }

    override fun endDocument() {
        if (currentFile.name.startsWith("_")) {
            fileIndex.addFileToRename(currentFile.name, "_${context.codespace}_shared_data.xml")
            context.reset()
            return
        }

        val newFileName = FileNameBuilder()
            .withLineName(context.lineName.toString())
            .withLinePublicCode(context.linePublicCode.toString())
            .withLinePrivateCode(context.linePrivateCode.toString())
            .withLineType(context.lineType)
            .withCodespace(context.codespace.toString())
            .build()

        fileIndex.addFileToRename(currentFile.name, newFileName)
        context.reset()
    }

    override fun getName(): String {
        TODO("Not yet implemented")
    }

    override fun getDescription(): String {
        TODO("Not yet implemented")
    }

    override fun getSupportedElementTypes(): Set<String> {
        return setOf(
            NetexTypes.LINE,
            NetexTypes.FLEXIBLE_LINE,
            NetexTypes.NAME,
            NetexTypes.PUBLIC_CODE,
            NetexTypes.PRIVATE_CODE,
            NetexTypes.PARTICIPANT_REF
        )
    }
}