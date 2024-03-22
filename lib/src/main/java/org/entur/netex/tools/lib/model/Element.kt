package org.entur.netex.tools.lib.model

data class Element(
    val name : String,
    val parent : Element? = null
)