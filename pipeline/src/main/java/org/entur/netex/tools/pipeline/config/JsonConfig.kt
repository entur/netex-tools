package org.entur.netex.tools.pipeline.config

import kotlinx.serialization.json.*
import java.io.InputStream


class JsonConfig {
    companion object {
        fun load(configFile : InputStream): CliConfig {
            val jsonParser = Json {
                isLenient = true
                ignoreUnknownKeys = true
            }

            return jsonParser.decodeFromStream<CliConfig>(configFile)
        }
    }
}
