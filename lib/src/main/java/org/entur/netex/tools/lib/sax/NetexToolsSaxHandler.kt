package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.utils.Log
import org.xml.sax.SAXParseException
import org.xml.sax.helpers.DefaultHandler

abstract class NetexToolsSaxHandler : DefaultHandler() {
    override fun warning(e: SAXParseException?) = Log.warn(e?.message, e)

    override fun error(e: SAXParseException?) = Log.error(e?.message, e)

    override fun fatalError(e: SAXParseException?) = Log.fatal(e?.message, e)

}