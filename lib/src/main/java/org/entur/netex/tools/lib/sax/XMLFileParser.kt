package org.entur.netex.tools.lib.sax

import java.io.File
import javax.xml.parsers.SAXParserFactory

object XMLFileParser {
    fun parseXMLFile(file: File, saxHandler : NetexToolsSaxHandler) {
        val saxFactory = SAXParserFactory.newInstance()
        val parser = saxFactory.newSAXParser()
        parser.parse(file.inputStream(), saxHandler)
    }
}