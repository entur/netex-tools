package org.entur.netex.tools.lib.plugin

data class NetexFileWriterContext(
    val useSelfClosingTagsWhereApplicable: Boolean,
    val removeEmptyCollections: Boolean,
    val preserveComments: Boolean,
)