package org.entur.netex.tools.lib.output

import org.xml.sax.ContentHandler
import java.io.File
import java.io.StringWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.sax.SAXTransformerFactory
import javax.xml.transform.stream.StreamResult

class XmlContext(
    val xmlFile: File,
    val stringWriter: StringWriter = StringWriter(),
    val xmlWriter: ContentHandler = createTransformerHandler(stringWriter)
) {

    companion object {
        fun createTransformerHandler(writer: StringWriter): ContentHandler {
            val factory = SAXTransformerFactory.newInstance() as SAXTransformerFactory
            val handler = factory.newTransformerHandler()
            val transformer = handler.transformer
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            handler.setResult(StreamResult(writer))
            return handler
        }
    }
}