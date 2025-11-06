package org.entur.netex.tools.lib.sax

object NetexUtils {
    val netexCollectionElementNames = listOf(
        "dataObjects",
        "validityConditions",
        "codespaces",
        "frames",
        "typesOfValue",
        "organisations",
        "vehicleTypes",
        "capacities",
        "vehicles",
        "destinationDisplays",
        "variants",
        "vias",
        "scheduledStopPoints",
        "stopAssignments",
        "operatingDays",
        "operatingPeriods",
        "dayTypeAssignments",
        "dayTypes",
        "routePoints",
        "pointsInSequence",
        "blocks",
        "journeys",
        "dataSources",
        "additionalNetworks",
        "routes",
        "lines",
        "journeyPatterns",
        "vehicleJourneys",
        "journeyInterchanges",
        "serviceLinks",
        "notices",
        "noticeAssignments",
    )

    fun isCollectionElement(elementName: String): Boolean = elementName in netexCollectionElementNames
}