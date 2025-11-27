package org.entur.netex.tools.lib.config

import kotlinx.serialization.Serializable
import org.entur.netex.tools.lib.output.XMLElementHandler
import org.entur.netex.tools.lib.plugin.NetexPlugin
import org.entur.netex.tools.lib.selectors.entities.EntitySelector
import org.entur.netex.tools.lib.selectors.refs.RefSelector

@Serializable
data class FilterConfig(
    var preserveComments : Boolean = true,
    var removePrivateData : Boolean = false,
    var skipElements : List<String> = listOf(),
    var unreferencedEntitiesToPrune : Set<String> = setOf(),
    var pruneReferences : Boolean = false,
    var referencesToExcludeFromPruning : Set<String> = setOf(),
    var useSelfClosingTagsWhereApplicable : Boolean = true,
    var fileNameMap: Map<String, String> = mapOf(),
    var plugins: List<NetexPlugin> = listOf(),
    var entitySelectors: List<EntitySelector> = listOf(),
    var refSelectors: List<RefSelector> = listOf(),
    var elementsRequiredChildren: Map<String, List<String>> = mapOf(),
    var customElementHandlers: Map<String, XMLElementHandler> = mapOf(),
) {
    fun toBuilder() : FilterConfigBuilder {
        return FilterConfigBuilder(filterConfig = this)
    }

    fun hasSpecifiedEntitiesToPrune() : Boolean = unreferencedEntitiesToPrune.isNotEmpty()
}