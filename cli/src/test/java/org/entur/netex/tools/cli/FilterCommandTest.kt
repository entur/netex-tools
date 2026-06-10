package org.entur.netex.tools.cli

import com.github.ajalt.clikt.core.MissingOption
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File

class FilterCommandTest {

    @TempDir
    lateinit var tmpDir: File

    private lateinit var filterConfig: File
    private lateinit var inputDir: File
    private lateinit var outputDir: File

    @BeforeEach
    fun setUp() {
        filterConfig = File(tmpDir, "filter-config.json").apply { writeText("{}") }
        inputDir = File(tmpDir, "input").apply { mkdir() }
        outputDir = File(tmpDir, "output")
    }

    @Test
    fun `filter command requires --filter-config`() {
        assertThrows<MissingOption> {
            NetexToolsCli().parse(listOf("filter", "--input", inputDir.absolutePath, "--output", outputDir.absolutePath))
        }
    }

    @Test
    fun `filter command requires --input`() {
        assertThrows<MissingOption> {
            NetexToolsCli().parse(listOf("filter", "--filter-config", filterConfig.absolutePath, "--output", outputDir.absolutePath))
        }
    }

    @Test
    fun `filter command requires --output`() {
        assertThrows<MissingOption> {
            NetexToolsCli().parse(listOf("filter", "--filter-config", filterConfig.absolutePath, "--input", inputDir.absolutePath))
        }
    }

    @Test
    fun `filter command runs end-to-end with an optional --cli-config and creates the output directory`() {
        // printReport=false avoids the report path; the empty input dir exercises the
        // full parse -> select -> export flow and the --cli-config loading branch.
        val cliConfig = File(tmpDir, "cli-config.json").apply {
            writeText("""{ "logLevel": "WARN", "printReport": false }""")
        }

        NetexToolsCli().parse(
            listOf(
                "filter",
                "--cli-config", cliConfig.absolutePath,
                "--filter-config", filterConfig.absolutePath,
                "--input", inputDir.absolutePath,
                "--output", outputDir.absolutePath,
            )
        )

        assertTrue(outputDir.isDirectory, "Expected the output directory to be created")
    }
}
