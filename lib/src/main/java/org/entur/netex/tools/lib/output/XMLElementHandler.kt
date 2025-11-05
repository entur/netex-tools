package org.entur.netex.tools.lib.output

import org.xml.sax.Attributes

interface XMLElementHandler {
    fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?, writer: DelegatingXMLElementWriter)
    fun characters(ch: CharArray?, start: Int, length: Int, writer: DelegatingXMLElementWriter)
    fun endElement(uri: String?, localName: String?, qName: String?, writer: DelegatingXMLElementWriter)
}
