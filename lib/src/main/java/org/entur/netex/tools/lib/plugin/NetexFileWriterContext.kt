package org.entur.netex.tools.lib.plugin

import org.entur.netex.tools.lib.config.TimePeriod
import java.io.File

data class NetexFileWriterContext(
    val file: File,
    val useSelfClosingTagsWhereApplicable: Boolean,
    val removeEmptyCollections: Boolean,
    val preserveComments: Boolean,
    val period: TimePeriod,
)