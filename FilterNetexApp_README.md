# FilterNetexApp - NeTEx Tools Documentation

A Kotlin library and CLI for parsing, filtering, and transforming
NeTEx (Network and Timetable Exchange) XML datasets.
NeTEx is the EU standard format for exchanging public transport data including
routes, schedules, stops, and operator information.

NeTEx datasets are large and highly interconnected -- entities reference each other
extensively. Manually extracting subsets is error-prone because removing elements can
leave broken references. netex-tools automates this through a three-phase pipeline.

---

## Modules

| Module | Artifact | Purpose |
|--------|----------|---------|
| `lib` | `netex-tools-lib` | Core library -- filtering engine, model, selectors, plugins |
| `cli` | `netex-tools-cli` | Command-line interface for JSON-configured filtering |
| `pipeline` | `netex-tools-pipeline` | Pre-configured filtering pipelines (e.g. timetable import) |
| `parent` | `netex-tools-parent` | Shared Maven build configuration |

---

## Three-Phase Pipeline

The filtering process consists of three phases:

```
INPUT XML FILES
       |
       v
  Phase 1: Build Entity Model
  (SAX-parse all files, extract entities and references)
       |
       v
  Phase 2: Select Entities & References
  (Apply filter rules, prune broken refs, remove unreferenced entities)
       |
       v
  Phase 3: Export Filtered XML
  (Re-parse input, write only selected entities/refs to output)
       |
       v
OUTPUT XML FILES
```

### Phase 1 -- Build Entity Model

SAX-parses all input XML files in streaming mode (memory-efficient). Extracts
**entities** (elements with an `id` attribute) and **references** (elements with
a `ref` attribute) into a lightweight in-memory graph (`EntityModel`). Only IDs,
types, and relationships are stored -- not the full XML content.

Plugins registered in the config receive SAX events during this phase, allowing
custom data collection without altering the model.

### Phase 2 -- Select Entities and References

Applies filter rules to determine which entities and references to keep:

1. Start with all entities
2. Optionally remove non-public entities (`removePrivateData`)
3. Optionally prune unreferenced entities of specified types
4. Apply custom `EntitySelector`s from config
5. Iterate steps 3-4 until the selection stabilizes (max 5 iterations), because removing an entity may leave others unreferenced
6. Select references, optionally pruning those pointing to excluded entities
7. Apply custom `RefSelector`s from config

### Phase 3 -- Export Filtered XML

Re-parses the original input XML, writing only selected entities and references to
output files. Optional behaviors:

- Convert empty elements to self-closing tags (`<Foo/>` instead of `<Foo></Foo>`)
- Remove empty collection elements
- Preserve or strip XML comments
- Apply custom `XMLElementHandler`s for element transformation
- Skip parent elements missing required children (`elementsRequiredChildren`)

---

## Configuration

### CliConfig

Controls CLI behavior and reporting. Loaded from JSON or constructed with `CliConfigBuilder`.

```json
{
  "logLevel": "INFO",
  "printReport": true,
  "alias": {
    "CompositeFrame": "CF",
    "ResourceFrame": "RF",
    "ServiceCalendarFrame": "SCF",
    "ServiceFrame": "SF",
    "TimetableFrame": "TF",
    "SiteFrame": "SF",
    "StopPointInJourneyPattern": "SPInJP"
  }
}
```

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `logLevel` | string | `"INFO"` | Log verbosity: `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`, `FATAL` |
| `printReport` | boolean | `true` | Print entity/reference statistics after filtering |
| `alias` | object | *(see above)* | Short names for entity types in console reports |

### FilterConfig

Controls what gets filtered, pruned, and transformed.

```json
{
  "preserveComments": true,
  "removePrivateData": true,
  "skipElements": [
    "/PublicationDelivery/dataObjects/CompositeFrame/frames/VehicleScheduleFrame",
    "/PublicationDelivery/dataObjects/CompositeFrame/frames/TimetableFrame/vehicleJourneys/DeadRun"
  ],
  "unreferencedEntitiesToPrune": [
    "JourneyPattern", "Route", "Network", "Line",
    "Operator", "Notice", "DestinationDisplay", "ServiceLink"
  ],
  "pruneReferences": true,
  "referencesToExcludeFromPruning": ["QuayRef"],
  "useSelfClosingTagsWhereApplicable": true,
  "fileNameMap": {},
  "elementsRequiredChildren": {
    "NoticeAssignment": ["NoticeRef", "NoticedObjectRef"]
  }
}
```

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `preserveComments` | boolean | `true` | Keep XML comments in output |
| `removePrivateData` | boolean | `false` | Remove entities where `publication` is explicitly set to anything other than `"public"` |
| `skipElements` | list | `[]` | Full XPath-like element paths to exclude (children are also skipped) |
| `unreferencedEntitiesToPrune` | set | `[]` | Entity types to remove if no other entity references them |
| `pruneReferences` | boolean | `false` | Remove references pointing to non-existent or excluded entities |
| `referencesToExcludeFromPruning` | set | `[]` | Reference types to keep even if their target is missing |
| `useSelfClosingTagsWhereApplicable` | boolean | `true` | Write self-closing tags for empty elements |
| `fileNameMap` | object | `{}` | Rename output files: `{ "input.xml": "output.xml" }` |
| `elementsRequiredChildren` | object | `{}` | Only include a parent element if it contains all listed child element types |
| `plugins` | list | `[]` | `NetexPlugin` instances for custom data collection (programmatic only) |
| `entitySelectors` | list | `[]` | Custom `EntitySelector` implementations (programmatic only) |
| `refSelectors` | list | `[]` | Custom `RefSelector` implementations (programmatic only) |
| `customElementHandlers` | object | `{}` | Map of element path to `XMLElementHandler` (programmatic only) |

---

## Programmatic Usage

### Basic Filtering

```kotlin
import org.entur.netex.tools.cli.app.FilterNetexApp
import org.entur.netex.tools.lib.config.FilterConfigBuilder
import java.io.File

val filterConfig = FilterConfigBuilder()
    .withSkipElements(listOf(
        "/PublicationDelivery/dataObjects/CompositeFrame/frames/VehicleScheduleFrame"
    ))
    .withPruneReferences(true)
    .withUnreferencedEntitiesToPrune(setOf("Route", "Network", "Line"))
    .build()

val report = FilterNetexApp(
    filterConfig = filterConfig,
    input = File("/path/to/netex-input"),
    target = File("/path/to/netex-output")
).run()
```

### Remove Private Data

```kotlin
val filterConfig = FilterConfigBuilder()
    .withRemovePrivateData(true)
    .build()

FilterNetexApp(
    filterConfig = filterConfig,
    input = File("input/"),
    target = File("output/")
).run()
```

### Strip Comments and Optimize Tags

```kotlin
val filterConfig = FilterConfigBuilder()
    .withPreserveComments(false)
    .withUseSelfClosingTagsWhereApplicable(true)
    .build()
```

---

## Extension Points

### EntitySelector

Implement `EntitySelector` to create custom logic for which entities to keep.
The returned `EntitySelection` is intersected with the results of other selectors,
meaning any entity NOT in the returned selection will be excluded from the output.

```kotlin
import org.entur.netex.tools.lib.selectors.entities.EntitySelector
import org.entur.netex.tools.lib.selectors.entities.EntitySelectorContext
import org.entur.netex.tools.lib.selections.EntitySelection

class KeepOnlyLinesSelector(private val lineIds: Set<String>) : EntitySelector {
    override fun selectEntities(context: EntitySelectorContext): EntitySelection {
        val model = context.entityModel
        val allByType = model.getEntitesByTypeAndId().toMutableMap()

        // Filter Line entities to only keep specified IDs
        allByType["Line"] = allByType["Line"]
            ?.filterKeys { it in lineIds }
            ?.toMutableMap() ?: mutableMapOf()

        return EntitySelection(allByType, model)
    }
}

// Usage
val filterConfig = FilterConfigBuilder()
    .withEntitySelectors(listOf(
        KeepOnlyLinesSelector(setOf("ENT:Line:100", "ENT:Line:200"))
    ))
    .build()
```

### RefSelector

Implement `RefSelector` for custom reference filtering. Like entity selectors,
the returned `RefSelection` is intersected with other selectors' results.

```kotlin
import org.entur.netex.tools.lib.selectors.refs.RefSelector
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.selections.RefSelection

class ExcludeRefTypeSelector(private val excludedTypes: Set<String>) : RefSelector {
    override fun selectRefs(model: EntityModel): RefSelection {
        val selected = model.listAllRefs()
            .filter { it.type !in excludedTypes }
            .toSet()
        return RefSelection(selected)
    }
}

// Usage
val filterConfig = FilterConfigBuilder()
    .withRefSelectors(listOf(
        ExcludeRefTypeSelector(setOf("NoticeRef", "BrandingRef"))
    ))
    .build()
```

### NetexPlugin

Plugins hook into SAX parsing during Phase 1 (entity model building) to collect
custom data. They do not alter the filtering logic.

Extend `AbstractNetexPlugin` to get no-op defaults for all methods, then override
only the callbacks you need:

```kotlin
import org.entur.netex.tools.lib.plugin.AbstractNetexPlugin
import org.entur.netex.tools.lib.model.Entity
import org.xml.sax.Attributes
import java.io.File

class ServiceJourneyCounter : AbstractNetexPlugin() {
    private var count = 0

    override fun getName() = "ServiceJourneyCounter"
    override fun getDescription() = "Counts ServiceJourney elements across all files"
    override fun getSupportedElementTypes() = setOf("ServiceJourney")

    override fun startElement(
        elementName: String, attributes: Attributes?, currentEntity: Entity?
    ) {
        if (elementName == "ServiceJourney") count++
    }

    override fun endDocument(file: File) {
        println("Processed ${file.name}, running total: $count ServiceJourneys")
    }

    override fun getCollectedData(): Int = count
}

// Usage
val filterConfig = FilterConfigBuilder()
    .withPlugins(listOf(ServiceJourneyCounter()))
    .build()

val report = FilterNetexApp(
    filterConfig = filterConfig,
    input = File("input/"),
    target = File("output/")
).run()

// Access collected data after run
val counter = filterConfig.plugins[0] as ServiceJourneyCounter
println("Total ServiceJourneys: ${counter.getCollectedData()}")
```

**Plugin lifecycle per file:**
1. `startElement()` / `characters()` / `endElement()` -- called for each element matching `getSupportedElementTypes()`
2. `endDocument(file)` -- called when the file has been fully parsed

### XMLElementHandler

Custom element handlers let you transform specific XML elements during Phase 3
(output writing). Register them by the full path of the element to intercept.

```kotlin
import org.entur.netex.tools.lib.output.XMLElementHandler
import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.xml.sax.Attributes

class MyElementHandler : XMLElementHandler {
    override fun startElement(
        uri: String?, localName: String?, qName: String?,
        attributes: Attributes?, writer: DelegatingXMLElementWriter
    ) {
        // Modify or pass through to default writer
        writer.defaultStartElement(uri, localName, qName, attributes)
    }

    override fun characters(
        ch: CharArray?, start: Int, length: Int,
        writer: DelegatingXMLElementWriter
    ) {
        writer.defaultCharacters(ch, start, length)
    }

    override fun endElement(
        uri: String?, localName: String?, qName: String?,
        writer: DelegatingXMLElementWriter
    ) {
        writer.defaultEndElement(uri, localName, qName)
    }
}

// Register by full element path
val filterConfig = FilterConfigBuilder()
    .withCustomElementHandlers(mapOf(
        "/PublicationDelivery/dataObjects/CompositeFrame" to MyElementHandler()
    ))
    .build()
```

---

## Core API Reference

### FilterNetexApp

The main entry point. Available in both `cli` and `pipeline` modules.

```kotlin
data class FilterNetexApp(
    val cliConfig: CliConfig = CliConfig(),
    val filterConfig: FilterConfig = FilterConfig(),
    val input: File,   // Input directory with NeTEx XML files
    val target: File,  // Output directory for filtered files
)
```

| Method | Returns | Description |
|--------|---------|-------------|
| `run()` | `FilterReport` | Execute the full three-phase pipeline |

The returned `FilterReport` contains:
- `entitiesByFile` -- map of output files to the set of entities they contain
- `elementTypesByFile` -- map of output files to element type counts

### EntityModel

In-memory graph of entities and their references, built during Phase 1.

| Method | Returns | Description |
|--------|---------|-------------|
| `addEntity(entity)` | `Boolean` | Register an entity |
| `getEntity(id)` | `Entity?` | Look up entity by ID |
| `getEntitiesOfType(type)` | `List<Entity>` | All entities of a given type |
| `getEntitiesReferringTo(entity)` | `Set<Entity>` | Entities that reference the given entity |
| `getEntitiesReferringTo(id)` | `Set<Entity>` | Entities that reference the given ID |
| `getRefsOfTypeFrom(sourceId, type)` | `List<Ref>` | References of a type originating from a source |
| `listAllEntities()` | `Collection<Entity>` | All entities in the model |
| `listAllRefs()` | `List<Ref>` | All references in the model |
| `getEntitiesKeptReport(selection)` | `String` | Formatted report of selected vs. total entities |
| `getRefsKeptReport(selection)` | `String` | Formatted report of selected vs. total references |

### Entity and Ref

```kotlin
class Entity(
    val id: String,            // NeTEx entity ID (e.g. "ENT:Line:100")
    val type: String,          // Element type (e.g. "Line", "ServiceJourney")
    val publication: String,   // "public", "restricted", or "private"
    val parent: Entity? = null // Parent entity in the XML hierarchy
)

class Ref(
    val type: String,    // Reference element type (e.g. "LineRef")
    val source: Entity,  // Entity containing this reference
    val ref: String,     // Target entity ID being referenced
)
```

### EntitySelection and RefSelection

Immutable sets representing the result of Phase 2 filtering.

```kotlin
class EntitySelection(
    val selection: Map<String, Map<String, Entity>>,  // Type -> (ID -> Entity)
    val model: EntityModel
) {
    fun isSelected(entity: Entity): Boolean
    fun includes(id: String): Boolean
    fun intersectWith(other: EntitySelection): EntitySelection
    fun hasEntitiesReferringTo(entity: Entity): Boolean
}

class RefSelection(val selection: Set<Ref>) {
    fun includes(ref: String): Boolean
    fun intersectWith(other: RefSelection): RefSelection
}
```

### FilterConfigBuilder

Fluent builder for constructing `FilterConfig` instances.

```kotlin
FilterConfigBuilder()
    .withPreserveComments(false)
    .withRemovePrivateData(true)
    .withSkipElements(listOf(...))
    .withUnreferencedEntitiesToPrune(setOf(...))
    .withPruneReferences(true)
    .withReferencesToExcludeFromPruning(setOf(...))
    .withUseSelfClosingTagsWhereApplicable(true)
    .withFileNameMap(mapOf(...))
    .withPlugins(listOf(...))
    .withEntitySelectors(listOf(...))
    .withRefSelectors(listOf(...))
    .withElementsRequiredChildren(mapOf(...))
    .withCustomElementHandlers(mapOf(...))
    .build()
```

Convert an existing config to a builder with `filterConfig.toBuilder()`.

---

## Building

Requires **Java 21** and **Maven 3.x**.

```bash
# Full build
mvn clean install

# Run tests
mvn test
```

### Maven Dependency

```xml
<dependency>
    <groupId>org.entur.ror</groupId>
    <artifactId>netex-tools-lib</artifactId>
    <version>0.0.39</version>
</dependency>
```
