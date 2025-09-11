package org.entur.netex.tools.lib.sax

import org.xml.sax.InputSource
import org.xml.sax.ext.LexicalHandler
import java.io.File
import javax.xml.parsers.SAXParserFactory

object XMLFileParser {
    fun parseXMLFile(file: File, saxHandler : NetexToolsSaxHandler) {
        val saxFactory = SAXParserFactory.newInstance()
        val parser = saxFactory.newSAXParser()
        val xmlReader = parser.xmlReader
        
        // Enable comment preservation if the handler supports it
        if (saxHandler is LexicalHandler) {
            xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", saxHandler)
        }
        
        // Set content handler and parse
        xmlReader.contentHandler = saxHandler
        file.inputStream().use { inputStream ->
            xmlReader.parse(InputSource(inputStream))
        }
    }
}