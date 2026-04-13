package org.entur.netex.tools.cli.moduletest.basic


import org.entur.netex.tools.cli.app.FilterNetexApp
import org.entur.netex.tools.lib.config.CliConfig
import org.entur.netex.tools.lib.config.FilterConfig
import org.entur.netex.tools.lib.config.JsonConfig
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
        val filterConfig = FilterConfig()
        val netexInput = netexInputUri()
        val app = FilterNetexApp(config, filterConfig, netexInput, File("target", "moduletest-basic"))
        app.run()
    }

    private fun netexInputUri(): File {
        val netexInput = requireNotNull(this.javaClass.getResource("netex")) {
            "Test resource 'netex' not found on classpath"
        }
        return File(netexInput.toURI())
    }

    private fun openConfigStream(): CliConfig {
        val configFile = requireNotNull(this.javaClass.getResourceAsStream("netex-cli.json")) {
            "Test resource 'netex-cli.json' not found on classpath"
        }
        val jsonConfig = JsonConfig.loadCliConfig(configFile)
        println(jsonConfig)
        return jsonConfig
    }
}
