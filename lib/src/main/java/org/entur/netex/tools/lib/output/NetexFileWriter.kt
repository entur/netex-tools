package org.entur.netex.tools.lib.output

import org.entur.netex.tools.lib.plugin.AbstractNetexFileWriter
import org.entur.netex.tools.lib.plugin.NetexFileWriterContext
import org.entur.netex.tools.lib.sax.NetexUtils
import java.io.BufferedWriter

open class NetexFileWriter(
    netexFileWriterContext: NetexFileWriterContext,
    val writer: BufferedWriter,
    val outputFileContent: StringBuilder,
): AbstractNetexFileWriter() {
    private val useSelfClosingTagsWhereApplicable = netexFileWriterContext.useSelfClosingTagsWhereApplicable
    private val removeEmptyCollections = netexFileWriterContext.removeEmptyCollections
    private val preserveComments = netexFileWriterContext.preserveComments

    override fun startDocument() {
        write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
    }

    override fun writeComments(ch: CharArray?, start: Int, length: Int) {
        if (!preserveComments) {
            return
        }

        val commentText = String(ch!!, start, length)
        write("<!--$commentText-->")
    }

    override fun endDocument() {
        if (useSelfClosingTagsWhereApplicable) {
            val processedOutput = removeEmptyCollections(outputFileContent.toString())
            writer.write(processedOutput)
        } else {
            writer.write(outputFileContent.toString())
        }
        writer.flush()
        writer.close()
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

    protected fun write(text : String) {
        outputFileContent.append(text)
    }
}