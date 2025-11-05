package org.entur.netex.tools.lib.output

import org.entur.netex.tools.lib.extensions.toISO8601
import java.time.LocalDate

class ValidBetweenFromDateHandler(val fromDate: LocalDate): XMLElementHandler {
    override fun onEnterElement(
        type: String,
        attributes: Map<String, String>
    ): String {
        val startTag = XMLElementRenderer.startElement(type = "FromDate")
        val content = fromDate.toISO8601()
        val endTag = XMLElementRenderer.endElement(type = "FromDate")
        return "$startTag$content$endTag"
    }

    override fun onText(text: String): String = ""
    override fun onLeaveElement(type: String): String = ""
}