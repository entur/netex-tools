package org.entur.netex.tools.lib.sax

import org.apache.commons.lang3.StringEscapeUtils
import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.utils.Log
import org.xml.sax.Attributes
import org.xml.sax.Locator
import org.xml.sax.ext.LexicalHandler
import java.io.File

class OutputNetexSaxHandler(
    private val outFile : File,
    private val skipHandler : SkipEntityAndElementHandler
) : NetexToolsSaxHandler(), LexicalHandler {
    private val output = outFile.bufferedWriter(Charsets.UTF_8)
    private var currentElement : Element? = null
    private var whiteSpace : String? = null
    private var empty = true
    private val outputBuffer = StringBuilder()
    private var elementStartPos = 0
    private var hasContentBetweenTags = false

    override fun setDocumentLocator(locator: Locator?) {
    }

    override fun startDocument() {
        write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
    }

    override fun endDocument() {
        // Process the buffer to convert empty reference elements to self-closing
        val processedOutput = convertEmptyReferencesToSelfClosing(outputBuffer.toString())
        output.write(processedOutput)
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
            hasContentBetweenTags = true
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
        
        // Reset content tracking for this element
        hasContentBetweenTags = false
        
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
        outputBuffer.append(text, start, length)
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
    
    override fun comment(ch: CharArray?, start: Int, length: Int) {
        if(skipHandler.inSkipMode()) {
            return
        }
        val commentText = String(ch!!, start, length)
        write("<!--$commentText-->")
    }
    
    private fun convertEmptyReferencesToSelfClosing(xmlContent: String): String {
        // Pattern to match empty reference elements that should be self-closing
        // Matches elements ending with "Ref" that have only whitespace (including newlines) between opening and closing tags
        val emptyRefPattern = Regex("""<(\w*Ref)(\s+[^>]*?)>\s*</\1>""", RegexOption.MULTILINE)
        
        return emptyRefPattern.replace(xmlContent) { matchResult ->
            val tagName = matchResult.groupValues[1]
            val attributes = matchResult.groupValues[2]
            "<$tagName$attributes/>"
        }
    }
}
