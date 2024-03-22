# netex-tools
Tools to parse, filter, validate (?) and transform Netex data files. This tool should not be specific to the "Nordic"
profile, but we will only implement support for elements/types used by it. If you find this tool incomplete, please
request an improvement or contribute to it through GitHub.


This tool is WORK-IN-PROGRESS

## Filter lib Wishlist / Goals

 - [x] Filter a data set and output a new dataset
   - [ ] Filter stops using a bounding box (coordinates)
   - [ ] Filter entities on ids
     - [x] Lines [Pri 2]
     - [x] ServiceJournies [Pri 2]
   - [ ] Filter on time-period
 - [ ] Validating that the data is according to a given NeTEx profile would be useful. [Later]
 - [ ] Saving the result in specific profiles. [Later] 


## netex-tools-lib

This is a Kotlin library that can be used in a Java/Kotlin app to filter NeTEx data. The lib can 
be used to build a model (`EntityModel`) and select entities from this. The model only contains
entities with id and type(xml element) and relations. This model is build using the Netex naming
conventions: 
  - Entities are elements "id" attribute
  - A relation from an entity exists, if a child element has a "ref" attribute. The "ref" value 
    should be identical to another entity id.

Note! Versioning is not supported. 

See tests and the `netex-tools-cli` main for an example on usage.


## netex-tools-cli

A java main that takes 3 arguments:
 - A config file with filtering rules
 - An input source (directory, zip not supported yet)
 - An output dir


### config file

Note! The parser does not accept comment, so you need to remove the comments
to use the example below.

```json
{
  "logLevel" : "INFO",
  
  "doc1" :  "Print a report with entities and relations witch summarize the number of selected entities and the total number of entities in the datasource.",
  "printReport" : true,
  
  "doc2" :  "NOT IMPLEMENTED!", 
  "doc3" :  "Keep all locations (Quays, StopPlaces ...) inside the polygon/bounding box defined by the coordinates. StopPlaces are kept if one quay is inside the area.",
  "area" : "60.0 10.0 60.0 10.0",

  "doc4" :  "NOT IMPLEMENTED!", 
  "doc5" :  "Include services witch operate in period (inclusive, exclusive)",
  "period" : {
    "start" : "2024-01-01",
    "end" : "2024-12-31"
  },
  
  "doc6" :  "Include the following lines, these are merged with the serviceJourneys and flex-lines",
  "lines" : [
    "NNN:Line:500"
  ],
  
  "doc7" :  "Include the following service journeys, these are merged with the lines and flex-lines",
  "serviceJourneys" : [
    "NNN:ServiceJourney:310_12"
  ],
  
  "doc8" :  "Include the following flexible lines, these are merged with the lines and service journeys",
  "flexLines" : [
    "NNN:FlexLine:1"
  ],
  
  "doc9" : "These elements are skipped, nested elements are skipped as well. The list included here is are all not parsed by OTP or optional (Notice).", 
  "skipElements" : [
    "AccessibilityAssessment",
    "AlternativeName",
    "keyList",
    "FromPointRef",
    "GroupOfStopPlaces",
    "NoticeAssignment",
    "NoticeRef",
    "NoticedObjectRef",
    "Parking",
    "ParkingProperties",
    "placeEquipments",
    "ServiceLink",
    "TopographicPlace",
    "TopographicPlaceRef",
    "TariffZone",
    "TariffZoneRef",
    "ToPointRef"
  ],
  
  "doc10" :  "The alias is used in the report only to shorten the lines",
  "alias" : [
    "CompositeFrame CF",
    "ResourceFrame RF",
    "ServiceCalendarFrame SCF",
    "ServiceFrame SF",
    "Network NW",
    "TimetableFrame TTF",
    "SiteFrame SF",
    "StopPointInJourneyPattern SPInJP"
  ]
}
```

An entity is only kept if all fliter rules are satisfied. If no filter rule is defined for a type, 
the normal cascade deleting applies.


#### Algorithm outline

1. Apply all rules to the data set.
   1. For example, filter quays and stop-places based on the `location-box`, `quay-ids`, and `line-ids`.
   2. Filter lines if `line-ids` is defined.
   3. Filter day-types and dated-service-journeys based on `period`
2. Merge and filter relations. 
3. Parse the data again and output selected entities and all child elements, but not necessarily child 
   entities.
   
This diagram might help with filtering relations:

![Netex overview](images/NetexOverview.png)

