package org.entur.netex.tools.lib.model

import java.util.regex.Pattern


/**
 * Used to abbreviate Element names in the report. It is not used for anything else.
 */
class Alias(private val nameAndAlias: MutableMap<String, String>) {
    fun abbreviate(name : String) : String {
        var tmp = name
        nameAndAlias.forEach {
            tmp = tmp.replace(it.key, it.value)
        }
        return tmp
    }

    companion object {
        fun of(list : Array<String>) : Alias {
            val nameAndAlias = mutableMapOf<String, String>()

            list.forEach {
                val args = it.split(Pattern.compile("[\\s,;:=]+"))
                if(args.size == 2) {
                    nameAndAlias[args[0]] = args[1]
                }
            }
            return Alias(nameAndAlias)
        }
    }
}
