package org.entur.netex.tools.lib

import org.entur.netex.tools.lib.report.FilterReport

data class ExportResult(
    val documents: Map<String, ByteArray>,
    val report: FilterReport,
)
