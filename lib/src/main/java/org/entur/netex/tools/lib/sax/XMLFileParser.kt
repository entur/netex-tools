package org.entur.netex.tools.lib.sax

import org.xml.sax.InputSource
import org.xml.sax.ext.LexicalHandler
import java.io.File
import java.io.InputStream
import javax.xml.parsers.SAXParserFactory

object XMLFileParser {
    fun parseXMLFile(file: File, saxHandler : NetexToolsSaxHandler) {
        file.inputStream().use { inputStream ->
            parseXml(inputStream, saxHandler)
        }
    }

    fun parseXml(inputStream: InputStream, saxHandler: NetexToolsSaxHandler) {
        val saxFactory = SAXParserFactory.newInstance().apply {
            // NeTEx is XSD-based and never uses DTDs. Rejecting any DOCTYPE
            // eliminates XML external entity (XXE) resolution outright, so a
            // malicious document cannot read local files or trigger outbound
            // requests during parsing.
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        }
        val parser = saxFactory.newSAXParser()
        val xmlReader = parser.xmlReader

        if (saxHandler is LexicalHandler) {
            xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", saxHandler)
        }

        xmlReader.contentHandler = saxHandler
        xmlReader.parse(InputSource(inputStream))
    }
}