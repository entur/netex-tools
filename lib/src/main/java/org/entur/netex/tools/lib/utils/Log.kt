package org.entur.netex.tools.lib.utils

import kotlinx.serialization.Serializable

object Log {
    @Serializable
    enum class Level {
        INFO, WARN, ERROR, FATAL
    }

    val UNKNOWN = "UNKNOWN"
    var printLevel = Level.INFO

    fun info(message : String?) {
        if(Level.INFO.ordinal >= printLevel.ordinal) {
            println("${Level.INFO}  ${message ?: UNKNOWN}")
        }
    }
    fun warn(message : String?, t : Throwable? = null) = print(Level.WARN, message, t)
    fun error(message : String?, t : Throwable? = null) = print(Level.ERROR, message, t)
    fun fatal(message : String?, t : Throwable? = null) = print(Level.FATAL, message, t)

    private fun print(level : Level, message : String?, t : Throwable? = null) {
        if(level.ordinal >= printLevel.ordinal) {
            System.err.println("$level  ${message ?: UNKNOWN}")
            t?.printStackTrace(System.err)
        }
    }
}
