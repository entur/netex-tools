package org.entur.netex.tools.lib.plugin

import org.xml.sax.Attributes

abstract class AbstractNetexFileWriter {
    abstract fun startDocument()
    abstract fun endDocument()
    abstract fun writeComments(ch: CharArray?, start: Int, length: Int)
    abstract fun writeStartElement(qName: String?)
    abstract fun writeStartElement(qName: String?, attributes: Attributes?)
    abstract fun writeCharacters(ch: CharArray?, start: Int, length: Int)
    abstract fun writeEndElement(qName: String?)
}