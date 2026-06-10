package org.entur.netex.tools.cli

import com.github.ajalt.clikt.core.CliktError
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File

class FilterCommandTest {

    @TempDir
    lateinit var tmpDir: File

    private val filterConfigJson get() = File(tmpDir, "filter-config.json").also { it.writeText("{}") }
    private val inputDir get() = File(tmpDir, "input").also { it.mkdir() }
    private val outputDir get() = File(tmpDir, "output")

    @Test
    fun `filter command requires --filter-config`() {
        assertThrows<CliktError> {
            NetexToolsCli().parse(listOf("filter", "--input", inputDir.absolutePath, "--output", outputDir.absolutePath))
        }
    }

    @Test
    fun `filter command requires --input`() {
        assertThrows<CliktError> {
            NetexToolsCli().parse(listOf("filter", "--filter-config", filterConfigJson.absolutePath, "--output", outputDir.absolutePath))
        }
    }

    @Test
    fun `filter command requires --output`() {
        assertThrows<CliktError> {
            NetexToolsCli().parse(listOf("filter", "--filter-config", filterConfigJson.absolutePath, "--input", inputDir.absolutePath))
        }
    }
}
