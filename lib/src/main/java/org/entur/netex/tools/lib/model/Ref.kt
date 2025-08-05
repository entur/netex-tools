package org.entur.netex.tools.lib.model

import org.entur.netex.tools.lib.model.Entity.Companion.EMPTY

data class Ref(
    val type : String,
    val source : Entity,
    val ref : String
) {
    fun toString(idToStr : (v : String) -> String?): String {
        return "$type ${source.fullPath()} -> ${idToStr(ref) ?: EMPTY}"
    }
}