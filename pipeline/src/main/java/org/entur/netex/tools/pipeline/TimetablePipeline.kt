package org.entur.netex.tools.pipeline

import org.entur.netex.tools.lib.config.CliConfigBuilder
import org.entur.netex.tools.lib.config.FilterConfigBuilder
import org.entur.netex.tools.lib.utils.Log
import org.entur.netex.tools.pipeline.app.FilterNetexApp
import java.io.File

internal fun main(args : Array<String>) {
    val skipElements = listOf(
        "/PublicationDelivery/dataObjects/CompositeFrame/frames/VehicleScheduleFrame",
        "/PublicationDelivery/dataObjects/VehicleScheduleFrame",
        "/PublicationDelivery/dataObjects/CompositeFrame/frames/ServiceFrame/lines/Line/routes",
        "/PublicationDelivery/dataObjects/ServiceFrame/lines/Line/routes",
        "/PublicationDelivery/dataObjects/CompositeFrame/frames/TimetableFrame/vehicleJourneys/DeadRun",
        "/PublicationDelivery/dataObjects/TimetableFrame/vehicleJourneys/DeadRun",
        "/PublicationDelivery/dataObjects/CompositeFrame/frames/TimetableFrame/trainNumbers",
        "/PublicationDelivery/dataObjects/TimetableFrame/trainNumbers",
        "/PublicationDelivery/dataObjects/CompositeFrame/frames/TimetableFrame/serviceFacilitySets",
        "/PublicationDelivery/dataObjects/TimetableFrame/serviceFacilitySets",
        "/PublicationDelivery/dataObjects/CompositeFrame/frames/ResourceFrame/dataSources",
        "/PublicationDelivery/dataObjects/ResourceFrame/dataSources",
        "/PublicationDelivery/dataObjects/CompositeFrame/frames/ResourceFrame/vehicleTypes",
        "/PublicationDelivery/dataObjects/ResourceFrame/vehicleTypes",
        "/PublicationDelivery/dataObjects/CompositeFrame/frames/TimetableFrame/vehicleJourneys/ServiceJourney/parts",
        "/PublicationDelivery/dataObjects/TimetableFrame/vehicleJourneys/ServiceJourney/parts",
        "/PublicationDelivery/dataObjects/CompositeFrame/frames/ResourceFrame/vehicles",
        "/PublicationDelivery/dataObjects/ResourceFrame/vehicles",
        "/PublicationDelivery/dataObjects/CompositeFrame/frames/SiteFrame",
        "/PublicationDelivery/dataObjects/SiteFrame"
    )

    val filterConfig = FilterConfigBuilder()
        .withSkipElements(skipElements)
        .withPruneReferences(true)
        .withReferencesToExcludeFromPruning(setOf("QuayRef"))
        .withCustomElementHandlers(
            mapOf(

            )
        )
        .withUnreferencedEntitiesToPrune(
            setOf(
                "JourneyPattern",
                "Route",
                "Network",
                "Line",
                "Operator",
                "Notice",
                "DestinationDisplay",
                "ServiceLink",
            )
        )
        .withElementsRequiredChildren(
            mapOf(
                "NoticeAssignment" to listOf("NoticeRef", "NoticedObjectRef")
            )
        ).build()

    val cliConfig = CliConfigBuilder()
        .withLogLevel(Log.Level.INFO)
        .withPrintReport(true)
        .withAliases(
            mapOf(
                "CompositeFrame" to "CF",
                "ResourceFrame" to "RF",
                "ServiceCalendarFrame" to "SCF",
                "ServiceFrame" to "SF",
                "Network" to "NW",
                "TimetableFrame " to "TF",
                "SiteFrame" to "SF",
                "StopPointInJourneyPattern" to "SPInJP"
            )
        ).build()

    FilterNetexApp(
        cliConfig = cliConfig,
        filterConfig = filterConfig,
        input = File(args[0]),
        target = File(args[1])
    ).run()
}