package org.entur.netex.tools.lib.io

import org.entur.netex.tools.lib.sax.NetexToolsSaxHandler
import org.entur.netex.tools.lib.sax.XMLFileParser
import java.io.ByteArrayInputStream
import java.io.File

object XMLFiles {
    fun parseXmlDocuments(input : File, saxHandler : (File) -> NetexToolsSaxHandler) {
        val sortedListOfFiles = (input.listFiles() ?: return).toList().sorted()
        for (file in sortedListOfFiles) {

            if (file.isFile && file.name.endsWith(".xml")) {
                val xmlHandler = saxHandler(file)
                XMLFileParser.parseXMLFile(file, xmlHandler)
            }
        }
    }

    fun parseXmlDocuments(documents: Map<String, ByteArray>, saxHandler: (String) -> NetexToolsSaxHandler) {
        for ((name, bytes) in documents.toSortedMap()) {
            if (name.endsWith(".xml")) {
                val handler = saxHandler(name)
                ByteArrayInputStream(bytes).use { stream ->
                    XMLFileParser.parseXml(stream, handler)
                }
            }
        }
    }
}