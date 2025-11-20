package org.entur.netex.tools.lib.report

import org.entur.netex.tools.lib.model.Entity
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

class FileIndexTest {
    @Test
    fun testAddEntity() {
        val fileIndex = FileIndex()
        val entity1 = Entity(id = "1", type = "TestType", publication = "restricted")
        val entity2 = Entity(id = "2", type = "TestType", publication = "restricted")
        val entity3 = Entity(id = "3", type = "TestType", publication = "restricted")
        val entity4 = Entity(id = "4", type = "TestAnotherType", publication = "restricted")

        val file1 = File("file1.xml")
        val file2 = File("file2.xml")

        fileIndex.add(entity1, file1)
        fileIndex.add(entity2, file1)
        fileIndex.add(entity3, file2)
        fileIndex.add(entity4, file1)

        assertEquals(2, fileIndex.elementTypesByFile[file1]?.get("TestType"))
        assertEquals(1, fileIndex.elementTypesByFile[file2]?.get("TestType"))
        assertEquals(1, fileIndex.elementTypesByFile[file1]?.get("TestAnotherType"))
    }
}