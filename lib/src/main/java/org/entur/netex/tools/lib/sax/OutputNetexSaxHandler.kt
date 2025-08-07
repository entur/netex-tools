package org.entur.netex.tools.lib.sax

import org.apache.commons.lang3.StringEscapeUtils
import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.utils.Log
import org.xml.sax.Attributes
import org.xml.sax.ext.LexicalHandler
import java.io.File

class OutputNetexSaxHandler(
    outFile : File,
    private val skipHandler : SkipEntityAndElementHandler,
    private val preserveComments : Boolean = true,
) : NetexToolsSaxHandler(), LexicalHandler {
    private val output = outFile.bufferedWriter(Charsets.UTF_8)
    private var currentElement : Element? = null
    private var whiteSpace : String? = null
    private val outputBuffer = StringBuilder()

    override fun startDocument() {
        write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
    }

    override fun endDocument() {
        // Next two lines may be commented out to improve readability of diff,
        // depending on whether input dataset uses self-closing tags or not.
         val processedOutput = removeEmptyCollections(outputBuffer.toString())
         output.write(processedOutput)
        // output.write(outputBuffer.toString())

        output.flush()
        output.close()
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
        currentElement = Element(qName!!, currentElement, attributes)
        if (skipHandler.shouldSkip(currentElement!!)) {
            skipHandler.startSkip(currentElement!!)
            return
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

        if(skipHandler.inSkipMode()) {
            skipHandler.endSkip(c)
            return
        }

        write("</$qName>")
    }

    private fun write(text : String) {
        printCachedWhiteSpace()
        outputBuffer.append(text)
    }

    private fun printCachedWhiteSpace() {
        if(whiteSpace != null) {
            outputBuffer.append(whiteSpace!!)
            whiteSpace = null
        }
    }
    
    override fun comment(ch: CharArray?, start: Int, length: Int) {
        if(skipHandler.inSkipMode()) {
            return
        }
        
        if (!preserveComments) {
            return  // Skip comments when preserveComments is false
        }
        
        val commentText = String(ch!!, start, length)
        write("<!--$commentText-->")
    }

    private fun removeEmptyCollections(xmlContent: String): String {
        val collectionPattern = Regex("""<(\w+)(\s+[^>]*?|)>\s*</\1>""", RegexOption.MULTILINE)

        return collectionPattern.replace(xmlContent) { matchResult ->
            val tagName = matchResult.groupValues[1]
            val attributes = matchResult.groupValues[2]
            if (NetexUtils.isCollectionElement(tagName)) {
                "<!-- Empty collection element of type $tagName was removed -->"
            } else {
                "<$tagName$attributes/>"
            }
        }
    }

    // LexicalHandler methods for comment preservation
    override fun startDTD(name: String?, publicId: String?, systemId: String?) {
        // Not needed for NeTEx files
    }

    override fun endDTD() {
        // Not needed for NeTEx files
    }

    override fun startEntity(name: String?) {
        // Not needed for NeTEx files
    }

    override fun endEntity(name: String?) {
        // Not needed for NeTEx files
    }

    override fun startCDATA() {
        // Not needed for NeTEx files
    }

    override fun endCDATA() {
        // Not needed for NeTEx files
    }
}
