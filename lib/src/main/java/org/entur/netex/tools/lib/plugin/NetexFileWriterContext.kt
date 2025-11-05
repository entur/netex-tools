package org.entur.netex.tools.lib.plugin

import org.entur.netex.tools.lib.config.TimePeriod

data class NetexFileWriterContext(
    val useSelfClosingTagsWhereApplicable: Boolean,
    val removeEmptyCollections: Boolean,
    val preserveComments: Boolean,
    val period: TimePeriod,
)