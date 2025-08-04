package org.entur.netex.tools.pipeline.moduletest.basic


import org.entur.netex.tools.pipeline.app.FilterNetexApp
import org.entur.netex.tools.pipeline.config.CliConfig
import org.entur.netex.tools.pipeline.config.JsonConfig
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

class BasicTest {

    @Test
    @Disabled("""
    TODO - Make a small set of files/data to use with this integration test. Verify result.    
    This test was used during development, but the test files are a bit big to include in a source 
    repo. To make this test work we should use a minimum set of files and data. In theory this tool
    can be used to produce the test input files. 
    """
    )
    fun test() {
        val config = openConfigStream()
        val netexInput = netexInputUri()
        val app = FilterNetexApp(config, netexInput, File("target", "moduletest-basic"))
        app.run()
    }

    private fun netexInputUri(): File {
        val netexInput = this.javaClass.getResource("netex")
        Assertions.assertNotNull(netexInput)
        return File(netexInput!!.toURI())
    }

    private fun openConfigStream(): CliConfig {
        val configFile = this.javaClass.getResourceAsStream("netex-cli.json")
        val jsonConfig = JsonConfig.load(configFile!!)
        Assertions.assertNotNull(jsonConfig)
        println(jsonConfig)

        return jsonConfig
    }
}
