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
    
    // Stack to track potential collection elements and their start positions
    private val collectionElementStack = mutableListOf<CollectionElementInfo>()
    
    // Data class to track collection element information
    private data class CollectionElementInfo(
        val element: Element,
        val startPosition: Int,
        var hasSelectedChildren: Boolean = false
    )

    override fun setDocumentLocator(locator: Locator?) {
    }
    
    // Check if an element name represents a collection (plural form, lowercase start)
    private fun isCollectionElement(elementName: String): Boolean {
        return elementName.length > 1 && 
               elementName[0].isLowerCase() && 
               elementName.endsWith("s") &&
               !elementName.endsWith("ss") // Avoid false positives like "class"
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
            // Mark any parent collection as having content
            if(collectionElementStack.isNotEmpty()) {
                collectionElementStack.last().hasSelectedChildren = true
            }
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
        
        // Check if this is a collection element - track it for potential empty collection removal
        if(isCollectionElement(qName)) {
            collectionElementStack.add(CollectionElementInfo(currentElement!!, outputBuffer.length))
        }
        
        // Mark non-collection elements with IDs as having selected children for parent collections
        if(id != null) {
            empty = false
            // Mark any parent collection as having selected children
            if(collectionElementStack.isNotEmpty()) {
                collectionElementStack.last().hasSelectedChildren = true
            }
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
        
        // Check if we're ending a collection element
        if(collectionElementStack.isNotEmpty() && collectionElementStack.last().element === c) {
            val collectionInfo = collectionElementStack.removeAt(collectionElementStack.size - 1)
            
            // If the collection has no selected children, remove it from the output buffer
            if(!collectionInfo.hasSelectedChildren) {
                // Remove everything from the collection start position to the current position
                outputBuffer.delete(collectionInfo.startPosition, outputBuffer.length)
                return // Don't write the closing tag
            }
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
        // Mark any parent collection as having content
        if(collectionElementStack.isNotEmpty()) {
            collectionElementStack.last().hasSelectedChildren = true
        }
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
