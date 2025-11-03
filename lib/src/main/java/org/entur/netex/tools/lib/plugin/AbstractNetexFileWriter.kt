package org.entur.netex.tools.lib.plugin

abstract class AbstractNetexFileWriter {
    abstract fun startDocument()
    abstract fun endDocument()
    abstract fun writeComments(ch: CharArray?, start: Int, length: Int)
}