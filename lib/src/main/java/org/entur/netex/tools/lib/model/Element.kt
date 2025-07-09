package org.entur.netex.tools.lib.model

import org.xml.sax.Attributes

data class Element(
    val name : String,
    val parent : Element? = null,
    val attributes: Attributes? = null,
)