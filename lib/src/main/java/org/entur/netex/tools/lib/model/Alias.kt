package org.entur.netex.tools.lib.model

import java.util.regex.Pattern


/**
 * Used to abbreviate Element names in the report. It is not used for anything else.
 */
class Alias(private val nameAndAlias: Map<String, String>) {
    fun abbreviate(name : String) : String {
        var tmp = name
        nameAndAlias.forEach {
            tmp = tmp.replace(it.key, it.value)
        }
        return tmp
    }

    companion object Factory {
        fun of(nameAndAlias : Map<String, String>) : Alias = Alias(nameAndAlias)
    }
}
