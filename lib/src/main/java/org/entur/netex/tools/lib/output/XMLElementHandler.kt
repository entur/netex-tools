package org.entur.netex.tools.lib.output

import org.xml.sax.Attributes

interface XMLElementHandler {
    fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?, writer: DelegatingXMLElementWriter)
    fun characters(ch: CharArray?, start: Int, length: Int, writer: DelegatingXMLElementWriter)
    fun endElement(uri: String?, localName: String?, qName: String?, writer: DelegatingXMLElementWriter)

    /**
     * Invoked after [startElement] has run, before any child events are processed.
     * Use this to inject XML content at the beginning of the element's body — for
     * example, to prepend a required first-child element.
     *
     * Emit content via `writer.startElement` / `writer.characters` / `writer.endElement`.
     * Emitted events are written directly to the underlying XML output; they do NOT
     * re-enter handler dispatch, so injected content will not recursively trigger
     * handlers registered on the injected elements' paths.
     *
     * Notes for NeTEx consumers:
     * - Pass the correct namespace URI (e.g. `"http://www.netex.org.uk/netex"`) —
     *   an empty URI will produce schema-invalid output when the document uses a
     *   default namespace.
     * - Respect sibling ordering as defined by the NeTEx schema. Injecting at the
     *   front of a parent whose schema requires specific first-children can produce
     *   invalid output; consider registering the handler on a schema-anchored child
     *   instead.
     *
     * Default: no-op.
     */
    fun afterStartElement(
        uri: String?,
        localName: String?,
        qName: String?,
        writer: DelegatingXMLElementWriter,
    ) {
    }

    /**
     * Invoked after all child events have been processed, before [endElement] runs.
     * Use this to inject XML content at the end of the element's body — for example,
     * to append new children to a container such as appending `<ServiceLink>` entries
     * to an existing `<serviceLinks>` section.
     *
     * Emit content via `writer.startElement` / `writer.characters` / `writer.endElement`.
     * Emitted events are written directly to the underlying XML output; they do NOT
     * re-enter handler dispatch.
     *
     * For elements subject to deferral via `ParentsWithRequiredChildrenDeferEventsRule`,
     * this hook fires when the buffered events are flushed, not during SAX parsing.
     * It does not fire for deferred elements that are discarded because their required
     * children were missing.
     *
     * Notes for NeTEx consumers:
     * - Pass the correct namespace URI — see [afterStartElement].
     * - To inject at a schema-correct position between siblings (rather than at the
     *   very end of the parent), register the handler on a preceding sibling instead
     *   and emit the new section after calling `writer.endElement(...)` in
     *   [endElement].
     * - If the source may already contain a sibling you'd otherwise create (e.g.
     *   injecting `<serviceLinks>` when it might exist), track child-element events
     *   in [startElement] / [endElement] to avoid producing duplicates.
     *
     * Default: no-op.
     */
    fun beforeEndElement(
        uri: String?,
        localName: String?,
        qName: String?,
        writer: DelegatingXMLElementWriter,
    ) {
    }
}
