# netex-tools
Tools to parse, filter, validate (?) and transform Netex data files. This tool should not be specific to the "Nordic"
profile, but we will only implement support for elements/types used by it. If you find this tool incomplete, please
request an improvement or contribute to it through GitHub.


This tool is WORK-IN-PROGRESS

## Filter lib Wishlist / Goals

 - [x] Filter a data set and output a new dataset
   - [ ] Filter StopPlaces by coordinates using a polygon (geojson file)
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
  - A relation from an entity exists if a child element has a "ref" attribute. The "ref" value 
    should be identical to another entity id.

The main entry point is `NetexProcessor`, which supports both file-based and in-memory
(`Map<String, ByteArray>`) processing. The pipeline can be decomposed into separate
passes to allow custom logic between entity model building and export.

Note! Versioning is not supported. 

See `FilterNetexApp_README.md` for full API documentation, or the tests for usage examples.


## netex-tools-cli

A command-line tool for filtering NeTEx datasets using JSON config files.

### Prerequisites

- Java 21 or later
- Maven 3.x (only required to build from source)

### Installation

Clone the repository and build the CLI module:

```bash
git clone https://github.com/entur/netex-tools.git
cd netex-tools
./mvnw package -pl cli --also-make
```

This produces:
- `cli/target/netex-tools-cli-<version>.jar` — the CLI application
- `cli/target/dependency/` — all runtime dependencies

The shell wrapper at `bin/netex-tools` assembles the classpath automatically.
Optionally add `bin/` to your `PATH`, or create a symlink:

```bash
ln -s "$(pwd)/bin/netex-tools" /usr/local/bin/netex-tools
```

### Usage

```
netex-tools filter [options]

Options:
  --filter-config PATH   Path to filter config JSON file (required)
  --input PATH           Input directory containing NeTEx XML files (required)
  --output PATH          Output directory for filtered NeTEx XML files (required)
  --cli-config PATH      Path to CLI config JSON file (optional; uses defaults if omitted)
  -h, --help             Show help and exit
```

**Example — filter with default CLI settings:**

```bash
netex-tools filter \
  --filter-config filter-config.json \
  --input path/to/netex-input/ \
  --output path/to/filtered-output/
```

**Example — filter with custom CLI settings (log level, report, aliases):**

```bash
netex-tools filter \
  --cli-config cli-config.json \
  --filter-config filter-config.json \
  --input path/to/netex-input/ \
  --output path/to/filtered-output/
```

### Config files

**cli-config.json** (optional) — controls log level, report, and aliases:

```json
{
  "logLevel": "INFO",
  "printReport": true,
  "alias": {
    "CompositeFrame": "CF",
    "ResourceFrame": "RF",
    "ServiceCalendarFrame": "SCF",
    "ServiceFrame": "SF",
    "Network": "NW",
    "TimetableFrame": "TF",
    "SiteFrame": "SF",
    "StopPointInJourneyPattern": "SPInJP"
  }
}
```

**filter-config.json** (required) — controls what gets filtered, pruned, and transformed.
See [FilterNetexApp_README.md](FilterNetexApp_README.md) for the full `FilterConfig` reference.

Minimal example:

```json
{
  "pruneReferences": true,
  "unreferencedEntitiesToPrune": [
    "JourneyPattern", "Route", "Network", "Line",
    "Operator", "Notice", "DestinationDisplay", "ServiceLink"
  ]
}
```

### Note on distribution

The CLI is distributed as a thin JAR (published to Maven Central) alongside its
dependencies. Users build from source with `./mvnw package` to get a runnable
installation. A fat JAR (standalone executable) is not published; the shell wrapper
handles classpath assembly.

