package org.entur.netex.tools.lib.config

import org.entur.netex.tools.lib.output.XMLElementHandler
import org.entur.netex.tools.lib.plugin.NetexPlugin
import org.entur.netex.tools.lib.selectors.entities.EntitySelector
import org.entur.netex.tools.lib.selectors.refs.RefSelector

class FilterConfigBuilder(
    private val filterConfig: FilterConfig = FilterConfig()
) {
    fun withPreserveComments(preserveComments: Boolean): FilterConfigBuilder {
        filterConfig.preserveComments = preserveComments
        return this
    }

    fun withRemovePrivateData(removePrivateData: Boolean): FilterConfigBuilder {
        filterConfig.removePrivateData = removePrivateData
        return this
    }

    fun withSkipElements(skipElements: List<String>): FilterConfigBuilder {
        filterConfig.skipElements = skipElements
        return this
    }

    fun withUnreferencedEntitiesToPrune(unreferencedEntitiesToPrune: Set<String>): FilterConfigBuilder {
        filterConfig.unreferencedEntitiesToPrune = unreferencedEntitiesToPrune
        return this
    }

    fun withPruneReferences(pruneReferences: Boolean): FilterConfigBuilder {
        filterConfig.pruneReferences = pruneReferences
        return this
    }

    fun withReferencesToExcludeFromPruning(referencesToExcludeFromPruning: Set<String>): FilterConfigBuilder {
        filterConfig.referencesToExcludeFromPruning = referencesToExcludeFromPruning
        return this
    }

    fun withUseSelfClosingTagsWhereApplicable(useSelfClosingTagsWhereApplicable: Boolean): FilterConfigBuilder {
        filterConfig.useSelfClosingTagsWhereApplicable = useSelfClosingTagsWhereApplicable
        return this
    }

    fun withFileNameMap(fileNameMap: Map<String, String>): FilterConfigBuilder {
        filterConfig.fileNameMap = fileNameMap
        return this
    }

    fun withCustomElementHandlers(customElementHandlers: Map<String, XMLElementHandler>): FilterConfigBuilder {
        filterConfig.customElementHandlers = customElementHandlers
        return this
    }

    fun withPlugins(plugins: List<NetexPlugin>): FilterConfigBuilder {
        filterConfig.plugins = plugins
        return this
    }

    fun withEntitySelectors(entitySelectors: List<EntitySelector>): FilterConfigBuilder {
        filterConfig.entitySelectors = entitySelectors
        return this
    }

    fun withRefSelectors(refSelectors: List<RefSelector>): FilterConfigBuilder {
        filterConfig.refSelectors = refSelectors
        return this
    }

    fun withElementsRequiredChildren(requiredChildren: Map<String, List<String>>): FilterConfigBuilder {
        filterConfig.elementsRequiredChildren = requiredChildren
        return this
    }

    fun build(): FilterConfig {
        return filterConfig
    }
}