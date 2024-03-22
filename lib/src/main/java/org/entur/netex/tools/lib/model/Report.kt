package org.entur.netex.tools.lib.model

class Report<T>(
    val title : String,
    val entities : Iterable<T>,
    val alias: Alias,
    val toStr : (T) -> String,
    val include : (T) -> Boolean
) {

    fun print() {
        val countTotal = SetOfCounters()
        val countKept = SetOfCounters()

        entities.forEach {
            val e = alias.abbreviate(toStr(it))
            countTotal.inc(e)
            if (include(it)) {
                countKept.inc(e)
            }
        }
        println("\n\n$title\n")
        val n = countTotal.listElements().map { it.length }.max()

        countTotal.listElements().sorted().forEach { type ->
            val total = countTotal.get(type)
            val kept = countKept.get(type)
            val k = if(kept == 0) "·" else kept
            println(String.format("%-${n}s  %5s  %5d", type, k, total))
        }
        println()
    }
}