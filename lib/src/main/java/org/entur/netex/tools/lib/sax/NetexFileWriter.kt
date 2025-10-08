package org.entur.netex.tools.lib.sax

import org.apache.commons.lang3.StringEscapeUtils
import org.xml.sax.Attributes
import java.io.File

class NetexFileWriter(
    val file: File,
    val useSelfClosingTagsWhereApplicable: Boolean,
    val removeEmptyCollections: Boolean,
    val preserveComments: Boolean,
) {
    private val fileWriter = file.bufferedWriter(Charsets.UTF_8)
    private val outputFileContent = StringBuilder()
    private var whiteSpace : String? = null

    fun startDocument() {
        write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
    }

    fun writeComments(ch: CharArray?, start: Int, length: Int) {
        if (!preserveComments) {
            return
        }

        val commentText = String(ch!!, start, length)
        write("<!--$commentText-->")
    }

    fun endDocument() {
        if (useSelfClosingTagsWhereApplicable) {
            val processedOutput = removeEmptyCollections(outputFileContent.toString())
            fileWriter.write(processedOutput)
        } else {
            fileWriter.write(outputFileContent.toString())
        }
        fileWriter.flush()
        fileWriter.close()
    }

    private fun removeEmptyCollections(xmlContent: String): String {
        val collectionPattern = Regex("""<(\w+)(\s+[^>]*?|)>\s*</\1>""", RegexOption.MULTILINE)

        return collectionPattern.replace(xmlContent) { matchResult ->
            val tagName = matchResult.groupValues[1]
            val attributes = matchResult.groupValues[2]
            if (NetexUtils.isCollectionElement(tagName) && removeEmptyCollections) {
                "<!-- Empty collection element of type $tagName was removed -->"
            } else {
                "<$tagName$attributes/>"
            }
        }
    }

    fun writeStartElement(qName: String?, attributes: Attributes?) {
        write("<$qName")
        if(attributes != null) {
            for (i in 0..<attributes.length) {
                write(" ${attributes.getQName(i)}=\"${attributes.getValue(i)}\"")
            }
        }
        write(">")
    }

    fun writeCharacters(ch: CharArray?, start: Int, length: Int) {
        val text = String(ch!!, start, length)
        if(text.isBlank()) {
            whiteSpace = text
        }
        else {
            write(StringEscapeUtils.escapeXml11(text))
        }
    }

    fun writeEndElement(qName: String?) {
        write("</$qName>")
    }

    private fun write(text : String) {
        printCachedWhiteSpace()
        outputFileContent.append(text)
    }

    private fun printCachedWhiteSpace() {
        if(whiteSpace != null) {
            outputFileContent.append(whiteSpace!!)
            whiteSpace = null
        }
    }
}