package org.entur.netex.tools.lib.output

import org.entur.netex.tools.lib.plugin.AbstractNetexFileWriter
import org.entur.netex.tools.lib.plugin.NetexFileWriterContext
import org.entur.netex.tools.lib.sax.NetexUtils

open class NetexFileWriter(
    val netexFileWriterContext: NetexFileWriterContext,
    val xmlContext: XmlContext,
): AbstractNetexFileWriter() {
    private val useSelfClosingTagsWhereApplicable = netexFileWriterContext.useSelfClosingTagsWhereApplicable
    private val removeEmptyCollections = netexFileWriterContext.removeEmptyCollections
    private val preserveComments = netexFileWriterContext.preserveComments

    override fun startDocument() {
        xmlContext.xmlWriter.characters("\n".toCharArray(), 0, 1)
    }

    override fun writeComments(ch: CharArray?, start: Int, length: Int) {
        if (!preserveComments) {
            return
        }

        val commentText = String(ch!!, start, length)
        xmlContext.stringWriter.write("<!--$commentText-->")
    }

    override fun endDocument() {
        if (useSelfClosingTagsWhereApplicable) {
            val output = xmlContext.stringWriter.toString()
            val processedOutput = removeEmptyCollections(output)
            xmlContext.stringWriter.buffer.setLength(0)
            xmlContext.stringWriter.write(processedOutput)
        }
        XMLFileWriter().writeToFile(context = xmlContext)
    }

    private fun removeEmptyCollections(xmlContent: String): String {
        val collectionPattern = Regex("""<(\w+)(\s+[^>]*)?/>""", RegexOption.MULTILINE)

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
}