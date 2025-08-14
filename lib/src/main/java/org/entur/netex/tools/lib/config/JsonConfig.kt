package org.entur.netex.tools.lib.config

import kotlinx.serialization.json.*
import java.io.InputStream

class JsonConfig {
    companion object {
        fun loadCliConfig(configFile : InputStream): CliConfig {
            val jsonParser = Json {
                isLenient = true
                ignoreUnknownKeys = true
            }

            return jsonParser.decodeFromStream<CliConfig>(configFile)
        }

        fun loadFilterConfig(configFile : InputStream): FilterConfig {
            val jsonParser = Json {
                isLenient = true
                ignoreUnknownKeys = true
            }

            return jsonParser.decodeFromStream<FilterConfig>(configFile)
        }
    }
}
