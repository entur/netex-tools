## Using FilterNetexApp from a Kotlin/Java app

FilterNetexApp is a helper class in the netex-tools library that enable users in running a pre-defined, step-by-step
process, to filter and transform NeTEx files. This process performs the following tasks in order:

1. Building of the Entity Model: collects data on entities, refs, and the relations between them from the input XML
   files.
2. Selection of entities and refs: filters entities and refs to keep in the outputted XML.
3. Writing of new, filtered XML files, based on the selection done in step 2.

Usage example:

```kotlin
val filterReport = FilterNetexApp(
    filterConfig = filterConfig, // See docs below
    input = "/my/input/directory", // Input directory containing XML files
    target = "/my/output/directory", // Where to output filtered XML files
).run()
```

### Filter configuration

A [FilterConfig](https://github.com/entur/netex-tools/blob/main/lib/src/main/java/org/entur/netex/tools/lib/config/FilterConfig.kt) is used to configure which filters that should be applied to the XML files. A
[FilterConfigBuilder](https://github.com/entur/netex-tools/blob/main/lib/src/main/java/org/entur/netex/tools/lib/config/FilterConfigBuilder.kt) is also provided by this library to
help ease configuration and building of FilterConfigs on the consumer end.

The following config properties are available on a FilterConfig:

| Property name                     |                                                                                                         Description                                                                                                         |                           Type |
|:----------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|-------------------------------:|
| preserveComments                  |                                                                                        If set to false, all comments will be removed                                                                                        |                        Boolean |
| removePrivateData                 |                                                  If set to true, any Entity with the publication attribute explicitly set to anything other than "public", will be removed                                                  |                        Boolean |
| skipElements                      |                                                   A list containing paths of XML element to skip entirely. Their full paths must be specified, not just the element names                                                   |                   List<String> |
| unreferencedEntitiesToPrune       |                                                                                           List of unreferenced entities to prune                                                                                            |                   List<String> |
| pruneReferences                   |                                                                If set to true, any Ref pointing to a non-existing Entity within the dataset will be removed                                                                 |                        Boolean |
| referencesToExcludeFromPruning    |                                                                                        List of Ref types to exclude from Ref Pruning                                                                                        |                   List<String> |
| useSelfClosingTagsWhereApplicable |                                                                                   If set to true, tags will be self-closed if applicable                                                                                    |                        Boolean |
| fileNameMap                       |                                                                     Map of old XML file names to new XML file names (use if renaming files is needed )                                                                      |            Map<String, String> |
| plugins                           |                                                                                                     List of NetexPlugin                                                                                                     |              List<NetexPlugin> |
| entitySelectors                   |                                                                                                   List of EntitySelector                                                                                                    |           List<EntitySelector> |
| refSelectors                      |                                                                                                     List of RefSelector                                                                                                     |              List<RefSelector> |
| customElementHandlers             |                                                                      Map of (String specifying path to XML element e.g. "/A/B/C") -> XMLElementHandler                                                                      | Map<String, XMLElementHandler> |
| elementsRequiredChildren          | Map used to specify a list of required children elements for certain parent elements. If one or more required children are not present, the entire Parent element will be removed from the output. Parent -> (Type1, Type2) |      Map<String, List<String>> |

* **NetexPlugin**s hook onto SAX parsing events during the entity building step, for every element specified by your implementation of NetexPlugin.getSupportedElementTypes. This may, for example, be used to collect data you're interested in from particular elements in the XML files.
* **EntitySelector**s are used to filter the entities that you would like to keep in the outputted XML. The returned EntitySelection is intersected with the result of other selectors internally in netex-tools when using FilterNetexApp, meaning any Entity NOT in the EntitySelection returned, will not be present in the output XML.
* **RefSelector**s do the same as EntitySelectors, only for Refs.
* **XMLElementHandlers** are used to hook onto the SAX parsing events when writing XML to output files.

