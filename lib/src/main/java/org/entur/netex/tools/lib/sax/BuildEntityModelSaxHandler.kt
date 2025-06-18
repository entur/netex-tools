package org.entur.netex.tools.lib.sax

import org.entur.netex.tools.lib.model.Element
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.Entity.Companion.EMPTY
import org.entur.netex.tools.lib.model.EntityModel
import org.xml.sax.Attributes

class BuildEntityModelSaxHandler(
    val entities : EntityModel,
    val skipHandler : SkipElementHandler
) : NetexToolsSaxHandler() {
    var currentEntity : Entity? = null
    var currentElement : Element? = null

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        val type = qName!!
        currentElement = Element(type, currentElement)

        if(skipHandler.startSkip(currentElement!!)) {
            return
        }

        // Handle entity
        val id = attributes?.getValue("id")
        val publication = attributes?.getValue("publication") ?: "public"

        if (id != null) {
            val entity = Entity(id, type, publication, currentEntity)
            currentEntity = entity
            entities.addEntity(entity)
        } else {
            val ref = attributes?.getValue("ref")
            if (ref != null) {
                entities.addRef(nn(type), currentEntity!!, ref)
            }
        }
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        val c = currentElement
        currentElement = currentElement?.parent

        if(skipHandler.endSkip(c)){
            return
        }

        if (currentEntity?.type == qName) {
            currentEntity = currentEntity?.parent
        }
    }

    private fun nn(value : String?) = value ?: EMPTY
}
