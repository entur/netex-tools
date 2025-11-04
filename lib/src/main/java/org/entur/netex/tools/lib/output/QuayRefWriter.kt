package org.entur.netex.tools.lib.output

import org.xml.sax.Attributes
import org.xml.sax.helpers.AttributesImpl

class QuayRefWriter(
    outputFileContent: StringBuilder,
    bufferedWhitespace: StringBuilder
): XMLElementWriter(
    content = outputFileContent,
    bufferedWhitespace = bufferedWhitespace
) {
    override fun writeStartElement(qName: String?, attributes: Attributes?) {
        val refAttributeValue = attributes?.getValue("ref")
        val quayRefAttrs = AttributesImpl()
        quayRefAttrs.addAttribute("", "ref", "ref", "CDATA", refAttributeValue)
        super.writeStartElement(qName, quayRefAttrs)
    }
}