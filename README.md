# netex-tools
Tools to parse, filter, validate? and transform Netex data files. This tool should not be specific to the "nordic" profile, but we will only implement support for elements/types used. So, if you find this tool incompleate request an update or contribute.

This tool is WORK-IN-PROGRESS

## Filter lib Wishlist / Goals

 - [ ] Filter a data set and output a new dataset
   - [ ] Filter stops using a bounding box (coordinates)
   - [ ] Filter entities on ids
     - [ ] Lines [Pri 2]
     - [ ] ServiceJournies [Pri 2]
   - [ ] Filter on time-period
 - [ ] Validating that the data is according to a given NeTEx profile would be usefull. [Later]
 - [ ] Saving the result in specific profiles. [Later] 
     
## netex-cli
A java main that takes 3 arguments:
 - An input file with filering rules
 - An input source (zip or directory)
 - An output dir/zip 

### input file

```
modes:
  // If set, stops will be removed from JourneyPatterns and the JourneyPattern is kept as long as it
  // is valid. If not set, the default is to drop all JourneyPattern with at least one deleted stop.
  // [pri 3]
  remove-stop-from-pattern

  // Remove all fields NOT used by OTP (e.g. key-values, topographic-places)
  // [Pri 3]
  remove-otp-unused

  // Remove unused quays/stop-places. The default is to leave unused stops. These stops could be used
  // by RT-updates or other data feeds
  // [Pri 3]
  remove-unused-quays

// Keep all locations (Quays, StopPlaces ...) inside the bounding box defined by the 2 coordinates.
// All quays and its StopPlace are kept if one quay is inside the box.
location-box:
  60.23 10.00
  59.20 12.50

// Include the following stop places and all queys inside
// [Pri 2]
stop-place-ids:

// Include the following quays and the parent stop place, but remove sibling quays
// [Pri 2]
quay-ids:

// Include the following lines only
// [Pri 2]
line-ids:
  RUT:Line:5789-278597-2859-7584
  RUT:Line:5789-278597-2859-7584
  RUT:Line:5789-278597-2859-7584
  RUT:Line:5789-278597-2859-7584

// Include services witch operate in period (inclusive, exclusive)
operating-periods:
  2024-04-04 2024-04-11
// Include services witch operate on given days(list of dates) 
operating-days:
  2024-04-15
```

An entity is only kept if all fliter rules are satisfied. If no filter rule is defined for a type, 
the normal cascading delete applies.

### filtering
The input file define the set of entities to keep. In addition all entities referenced set should
be kept.

#### Algorithm outline
1. Apply all rules to the data set.
   1. For example filter quays and stop-places based on the `location-box`, `quay-ids`, and `line-ids`.
   2. Filter lines if `line-ids` is defined.
   3. Fiter day-types based on `operating-period` and `operating-days`
2. Loop over all entities and remove entities with missing/broken references, continue until no
   more entities can be removed. Note! Using indexes and following the natural order of types will
   speed up the process.
   
