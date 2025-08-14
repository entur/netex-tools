package org.entur.netex.tools.lib.config

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CliConfigTest {
    @Test
    fun alias() {
        val aliases = mapOf(
            "entity1" to "alias1",
            "entity2" to "alias2"
        )
        val config = CliConfig(alias = aliases)
        assertEquals(aliases.get("entity1"), config.alias.get("entity1"))
        assertEquals(aliases.get("entity2"), config.alias.get("entity2"))
    }
}