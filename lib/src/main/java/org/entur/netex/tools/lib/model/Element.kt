package org.entur.netex.tools.lib.model

data class Element(
    val name : String,
    val parent : Element? = null,
    val attributes: Map<String, String> = mapOf(),
    val currentEntityId : String? = null
) {
    fun isEntity(): Boolean {
        return attributes["id"] != null
    }

    fun isRef(): Boolean {
        return attributes["ref"] != null
    }

    fun getAttribute(name: String): String {
        return attributes[name] ?: ""
    }
}