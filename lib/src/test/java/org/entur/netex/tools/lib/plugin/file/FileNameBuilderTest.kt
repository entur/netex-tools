package org.entur.netex.tools.lib.plugin.file

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FileNameBuilderTest {
    private val fileNameBuilder = FileNameBuilder()

    @Test
    fun withCodespace() {
        val codespace = "TestCodespace"
        fileNameBuilder.withCodespace(codespace)
        assertEquals(codespace, fileNameBuilder.codespace)
    }

    @Test
    fun withLineType() {
        val lineType = "TestLineType"
        fileNameBuilder.withLineType(lineType)
        assertEquals(lineType, fileNameBuilder.lineType)
    }

    @Test
    fun withLineName() {
        val lineName = "TestLineName"
        fileNameBuilder.withLineName(lineName)
        assertEquals(lineName, fileNameBuilder.lineName)
    }

    @Test
    fun withLinePublicCode() {
        val publicCode = "TestPublicCode"
        fileNameBuilder.withLinePublicCode(publicCode)
        assertEquals(publicCode, fileNameBuilder.linePublicCode)
    }

    @Test
    fun withLinePrivateCode() {
        val privateCode = "TestPrivateCode"
        fileNameBuilder.withLinePrivateCode(privateCode)
        assertEquals(privateCode, fileNameBuilder.linePrivateCode)
    }

    @Test
    fun build() {
        val codespace = "TestCodespace"
        val lineType = "TestLineType"
        val lineName = "TestLineName"
        val publicCode = "TestPublicCode"
        val privateCode = "TestPrivateCode"

        val expectedFileName = "TESTCODESPACE_TESTCODESPACE-TestLineType-TestPrivateCode_TestPublicCode_TestLineName.xml"

        val fileName = fileNameBuilder
            .withCodespace(codespace)
            .withLineType(lineType)
            .withLineName(lineName)
            .withLinePublicCode(publicCode)
            .withLinePrivateCode(privateCode)
            .build()

        assertEquals(expectedFileName, fileName)
    }

}