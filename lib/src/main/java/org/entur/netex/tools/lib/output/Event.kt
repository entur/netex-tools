package org.entur.netex.tools.lib.output


sealed class Event

data class StartElement(
    val uri: String?,
    val localName: String?,
    val qName: String?,
    val attributes: Map<String, String>?
): Event()

data class Characters(
    val ch: CharArray?,
    val start: Int,
    val length: Int
): Event()

data class EndElement(
    val uri: String?,
    val localName: String?,
    val qName: String?
): Event()

data class Comments(
    val ch: CharArray?,
    val start: Int,
    val length: Int
): Event()
