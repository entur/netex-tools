package org.entur.netex.tools.lib.sax

import org.apache.commons.lang3.StringEscapeUtils
import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.utils.Log
import org.xml.sax.Attributes
import org.xml.sax.Locator
import java.io.File

class OutputNetexSaxHandler(
    private val outFile : File,
    private val skipHandler : SkipEntityAndElementHandler
) : NetexToolsSaxHandler() {
    private val output = outFile.bufferedWriter(Charsets.UTF_8)
    private var currentElement : Element? = null
    private var whiteSpace : String? = null
    private var empty = true

    override fun setDocumentLocator(locator: Locator?) {
    }

    override fun startDocument() {
        write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
    }

    override fun endDocument() {
        output.flush()
        output.close()

        if(empty) {
            Log.info("document was empty. deleting outFile")
            outFile.delete()
        }
    }

    override fun startPrefixMapping(prefix: String?, uri: String?) {
        Log.info("startPrefixMapping - prefix: $prefix, uri: $uri")
    }

    override fun endPrefixMapping(prefix: String?) {
        Log.info("endPrefixMapping - prefix: $prefix")
    }

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        if(skipHandler.inSkipMode()) {
            return
        }
        val text = String(ch!!, start, length)
        if(text.isBlank()) {
            whiteSpace = text
        }
        else {
            write(StringEscapeUtils.escapeXml11(text))
        }
    }

    override fun processingInstruction(target: String?, data: String?) {
        Log.info("processingInstruction - target: $target")
    }

    override fun skippedEntity(name: String?) {
        Log.info("skippedEntity - name: $name")
    }

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        currentElement = Element(qName!!, currentElement)
        val id = attributes?.getValue("id") //?.let { NetexID.netexID(it) }

        if(skipHandler.startSkip(currentElement!!, id)) {
            return
        }
        if(id != null) {
            empty = false
        }
        write("<$qName")
        if(attributes != null) {
            for (i in 0..<attributes.length) {
                write(" ${attributes.getQName(i)}=\"${attributes.getValue(i)}\"")
            }
        }
        write(">")
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        val c = currentElement
        currentElement = currentElement?.parent

        if(skipHandler.endSkip(c)){
            return
        }
        write("</$qName>")
    }

    private fun write(text : CharArray, start : Int, length : Int) {
        printCachedWhiteSpace()
        output.write(text, start, length)
    }

    private fun write(text : String) {
        printCachedWhiteSpace()
        output.write(text)
    }

    private fun printCachedWhiteSpace() {
        if(whiteSpace != null) {
            output.write(whiteSpace!!)
            whiteSpace = null
        }
    }
}
