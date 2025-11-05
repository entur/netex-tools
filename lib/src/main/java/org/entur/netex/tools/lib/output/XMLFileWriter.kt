package org.entur.netex.tools.lib.output

import java.io.BufferedWriter
import java.io.FileWriter

class XMLFileWriter {
    fun writeToFile(context: XmlContext) {
        val output = context.stringWriter.toString()
        val writer = BufferedWriter(FileWriter(context.xmlFile))
        writer.write(output)
        writer.flush()
        writer.close()
    }
}