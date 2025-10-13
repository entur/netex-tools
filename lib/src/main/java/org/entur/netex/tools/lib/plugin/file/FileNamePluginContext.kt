package org.entur.netex.tools.lib.plugin.file

class FileNamePluginContext{
    var codespace: StringBuilder = StringBuilder()
    var lineType: String = ""
    var lineName: StringBuilder = StringBuilder()
    var linePublicCode: StringBuilder = StringBuilder()
    var linePrivateCode: StringBuilder = StringBuilder()

    fun reset() {
        codespace.clear()
        lineType = ""
        lineName.clear()
        linePublicCode.clear()
        linePrivateCode.clear()
    }
}