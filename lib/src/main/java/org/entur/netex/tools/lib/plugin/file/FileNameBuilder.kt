package org.entur.netex.tools.lib.plugin.file

class FileNameBuilder {
    var codespace: String = ""
    var lineType: String = ""
    var lineName: String = ""
    var linePublicCode: String = ""
    var linePrivateCode: String = ""

    fun withCodespace(codespace: String): FileNameBuilder {
        this.codespace = sanitize(codespace)
        return this
    }

    fun withLineType(lineType: String): FileNameBuilder {
        this.lineType = sanitize(lineType)
        return this
    }

    fun withLineName(lineName: String): FileNameBuilder {
        this.lineName = sanitize(lineName)
        return this
    }

    fun withLinePublicCode(linePublicCode: String): FileNameBuilder {
        this.linePublicCode = sanitize(linePublicCode)
        return this
    }

    fun withLinePrivateCode(linePrivateCode: String): FileNameBuilder {
        this.linePrivateCode = sanitize(linePrivateCode)
        return this
    }

    private fun sanitize(fileNameString: String): String {
        return fileNameString
            .replace("'", "-")
            .replace(".", "-")
            .replace("/", "-")
            .replace("Æ", "E")
            .replace("Ø", "O")
            .replace("Å", "A")
            .replace("æ", "e")
            .replace("ø", "o")
            .replace("å", "a")
            .replace(Regex("[^\\x00-\\x7F]"), "-") // Replaces remaining non-ASCII characters with hyphen
            .replace(Regex("\\s"), "-")
    }

    fun build(): String {
        return "${codespace.uppercase()}_${codespace.uppercase()}-${lineType}-${linePrivateCode}_${linePublicCode}_${lineName}.xml"
    }
}