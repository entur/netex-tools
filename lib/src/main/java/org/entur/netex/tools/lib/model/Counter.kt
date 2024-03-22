package org.entur.netex.tools.lib.model

class Counter {
    private var counter : Int = 0

    fun inc() {
        counter++
    }

    fun get() = counter
}