/**
 * Copyright (c) Codice Foundation
 * <p/>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.plugin.nitf

import ddf.catalog.data.Attribute
import ddf.catalog.data.Metacard
import ddf.catalog.data.types.Core
import org.codice.ddf.catalog.async.data.api.internal.*
import org.codice.ddf.catalog.async.data.impl.ProcessRequestImpl
import org.codice.imaging.nitf.core.common.NitfFormatException
import org.codice.imaging.nitf.core.image.ImageSegment
import org.codice.imaging.nitf.fluent.NitfParserInputFlow
import org.codice.imaging.nitf.fluent.NitfParserParsingFlow
import org.codice.imaging.nitf.fluent.impl.NitfParserInputFlowImpl
import org.codice.imaging.nitf.fluent.impl.NitfParserParsingFlowImpl
import org.codice.imaging.nitf.render.NitfRenderer
import spock.lang.Specification
import spock.lang.Unroll

import java.awt.image.BufferedImage
import java.util.concurrent.Semaphore
import java.util.function.Supplier

class NitfPostProcessPluginSpec extends Specification {

    // TODO: 06/21/2018 oconnormi - need to find some addtional nitf samples to test other cases
    private static final String GEO_NITF = "/i_3001a.ntf"

    private static final long TEST_SIZE = 5L * 1024L * 1024L

    private static final double DEFAULT_MAX_SIDE_LENGTH = 1024.0

    private NitfPostProcessPlugin nitfPostProcessPlugin = null

    private ProcessRequest<ProcessCreateItem> createProcessRequest

    private ProcessRequest<ProcessUpdateItem> updateProcessRequest

    private List<ProcessCreateItem> processCreateItemList = new ArrayList<>()

    private List<ProcessUpdateItem> processUpdateItemList = new ArrayList<>()

    private Metacard metacard

    private Metacard metacard1

    private ProcessResource processResource

    private ProcessResource processResource1

    private ProcessCreateItem processCreateItem

    private ProcessCreateItem processCreateItem1

    private ProcessUpdateItem processUpdateItem

    private ProcessUpdateItem processUpdateItem1

    private Semaphore lock;

    def setup() {
        lock = Mock(Semaphore)

        nitfPostProcessPlugin = new NitfPostProcessPlugin(lock, {
            return Mock(NitfRenderer) {
                final BufferedImage bufferedImage = Mock(BufferedImage)

                render() >> bufferedImage
                renderToClosestDataModel() >> bufferedImage
            }
        }, {
            return new NitfParserInputFlowImpl()
        })

        metacard = Mock(Metacard) {
            getId() >> "0000000"
        }

        metacard1 = Mock(Metacard) {
            getId() >> "1111111"
        }

        def (resourceUri, resourceInputStream) = getTestResource(GEO_NITF)
        processResource = Mock(ProcessResource) {
            getUri() >> resourceUri
            getInputStream() >> resourceInputStream
            getMimeType() >> NitfPostProcessPlugin.NITF_MIME_TYPE.toString()
            getSize() >> TEST_SIZE
        }

        processCreateItem = Mock(ProcessCreateItem) {
            getMetacard() >> metacard
            getProcessResource() >> processResource
        }

        processCreateItemList.add(processCreateItem)

        processUpdateItem = Mock(ProcessUpdateItem) {
            getMetacard() >> metacard
            getProcessResource() >> processResource
            getOldMetacard() >> Mock(Metacard)
        }

        processUpdateItemList.add(processUpdateItem)

        createProcessRequest = Mock(ProcessRequestImpl) {
            getProcessItems() >> processCreateItemList
        }

        updateProcessRequest = Mock(ProcessRequestImpl) {
            getProcessItems() >> processUpdateItemList
        }
    }

    @Unroll
    def "Building derived image filename from \"#ftitle\"" (String ftitle, String expectedFname) {
        expect: "the derived filename should not include invalid characters"
            NitfPostProcessPlugin.buildDerivedImageTitle(ftitle, "original", "jpg") == expectedFname

        where:
            ftitle                                                     ||  expectedFname
            null                                                       ||  "original.jpg"
            ""                                                         ||  "original.jpg"
            "_"                                                        ||  "original.jpg"
            "@#\$%^&*()+-={}|[]<>?:"                                   ||  "original.jpg"
            "Too Legit To Quit"                                        ||  "original-toolegittoquit.jpg"
            "A bunch of _invalid_ characters! @#\$%^&*()+-={}|[]<>?:;" ||  "original-abunchof_invalid_characters.jpg"
    }

    def "encountering runtime errors during nitf rendering"() {
        setup:
            NitfRenderer nitfRenderer = Mock {
                render(_ as ImageSegment) >> { throw new RuntimeException() }
            }
            Supplier<NitfRenderer> nitfRendererSupplier = {
                return  nitfRenderer
            }
            NitfPostProcessPlugin plugin = new NitfPostProcessPlugin(nitfRendererSupplier, { return new NitfParserInputFlowImpl() })

        when: "the nitf renderer encounters a runtime exception"
            ProcessRequest<ProcessCreateItem> result = plugin.processCreate(createProcessRequest)

        then: "the plugin should return an unmodified process request"
            result.getProcessItems() == createProcessRequest.getProcessItems()
    }

    def "encountering nitf formatting errors during nitf parsing in create requests"() {
        setup:
            NitfParserParsingFlow nitfParserParsingFlow = Mock(NitfParserParsingFlowImpl)
            nitfParserParsingFlow.allData() >> { new NitfFormatException() }
            NitfParserInputFlow nitfParserInputFlow = Mock(NitfParserInputFlow)
            nitfParserInputFlow.inputStream(_ as InputStream) >> nitfParserParsingFlow
            NitfRenderer nitfRenderer = Mock(NitfRenderer)
            NitfPostProcessPlugin plugin = new NitfPostProcessPlugin({return nitfRenderer}, {nitfParserInputFlow})
        when: "the nitf renderer encounters a nitf format exception"
            ProcessRequest<ProcessCreateItem> result = plugin.processCreate(createProcessRequest)
        then: "the plugin should return an unmodified process request"
            result.getProcessItems() == createProcessRequest.getProcessItems()
            result.processItems.size() == 1
    }

    def "handling bad input during create requests"() {
        when: "the plugin receives a null input for a create request"
            nitfPostProcessPlugin.processCreate(null)
        then: "the plugin should throw an exception"
            thrown(NullPointerException)
    }

    def "handling bad input during update requests"() {
        when: "the plugin receives a null input for an update request"
            nitfPostProcessPlugin.processUpdate(null)
        then: "the plugin should throw an exception"
            thrown(NullPointerException)
    }

    def "handling bad input during delete requests"() {
        when: "the plugin receives a null input for a delete request"
            nitfPostProcessPlugin.processDelete(null)
        then: "the plugin should throw an exception"
            thrown(NullPointerException)
    }

    def "handling basic create requests"() {
        when: "a valid process create request is submitted"
            def result = nitfPostProcessPlugin.processCreate(createProcessRequest)
        then:
            2 * metacard.setAttribute({it.name = Core.DERIVED_RESOURCE_URI; it.value != null})
            1 * metacard.setAttribute({it.name == Core.THUMBNAIL; it.value != null})
            1 * processCreateItem.markMetacardAsModified()
            result == createProcessRequest
            result.processItems.size() == 3
        1 * lock.acquire()
        1 * lock.release()
    }

    def "handling basic update requests"() {
        when: "a valid process update request is submitted"
            def result = nitfPostProcessPlugin.processUpdate(updateProcessRequest)
        then:
            2 * metacard.setAttribute({it.name = Core.DERIVED_RESOURCE_URI; it.value != null})
            1 * metacard.setAttribute({it.name == Core.THUMBNAIL; it.value != null})
            1 * processUpdateItem.markMetacardAsModified()
            result == updateProcessRequest
            result.processItems.size() == 3
        1 * lock.acquire()
        1 * lock.release()
    }

    def "handling delete requests"() {
        setup:
            ProcessDeleteItem processDeleteItem = Mock(ProcessDeleteItem) {
                getMetacard() >> metacard
                getProcessResource() >> processResource
            }
            ProcessRequest<ProcessDeleteItem> deleteProcessRequest = Mock(ProcessRequestImpl) {
                getProcessItems() >> [processDeleteItem]
            }
        when: "a valid process delete request is submitted"
            def result = nitfPostProcessPlugin.processDelete(deleteProcessRequest)
        then: "no action should be performed"
            0 * metacard.setAttribute(_)
            result.processItems.size() == 1
    }

    def "handling processing large files in create requests"() {
        setup:
            nitfPostProcessPlugin.setMaxNitfSizeMB(generateSizeLimit())
        when: "a request contains a resource that is too large to process"
            nitfPostProcessPlugin.processCreate(createProcessRequest)
        then: "nothing should be added to the metacard"
            0 * metacard.setAttribute(_)
    }

    def "handling processing large files in update requests"() {
        setup:
            nitfPostProcessPlugin.setMaxNitfSizeMB(generateSizeLimit())
        when: "a request contains a resource that is too large to process"
            nitfPostProcessPlugin.processUpdate(updateProcessRequest)
        then:
            0 * metacard.setAttribute(_)
    }

    def "handling create requests with overview disabled"() {
        setup:
            nitfPostProcessPlugin.setCreateOverview(false)
        when:
            nitfPostProcessPlugin.processCreate(createProcessRequest)
        then:
            1 * metacard.setAttribute({it.name = Core.DERIVED_RESOURCE_URI; it.value.toString().contains("original")})
            1 * metacard.setAttribute({it.name == Core.THUMBNAIL; it.value != null})
    }

    def "handling update requests with overview disabled"() {
        setup:
            nitfPostProcessPlugin.setCreateOverview(false)
        when:
            nitfPostProcessPlugin.processUpdate(updateProcessRequest)
        then:
            1 * metacard.setAttribute({it.name = Core.DERIVED_RESOURCE_URI; it.value.toString().contains("original")})
            1 * metacard.setAttribute({it.name == Core.THUMBNAIL; it.value != null})
    }

    def "handling create requests with original disabled"() {
        setup:
            nitfPostProcessPlugin.setStoreOriginalImage(false)
        when:
            nitfPostProcessPlugin.processCreate(createProcessRequest)
        then:
            1 * metacard.setAttribute({it.name = Core.DERIVED_RESOURCE_URI; it.value.toString().contains("overview")})
            1 * metacard.setAttribute({it.name == Core.THUMBNAIL; it.value != null})
    }

    def "handling update requests with original disabled"() {
        setup:
            nitfPostProcessPlugin.setStoreOriginalImage(false)
        when:
            nitfPostProcessPlugin.processUpdate(updateProcessRequest)
        then:
            1 * metacard.setAttribute({it.name = Core.DERIVED_RESOURCE_URI; it.value.toString().contains("overview")})
            1 * metacard.setAttribute({it.name == Core.THUMBNAIL; it.value != null})
    }

    def "handling non-nitf resources in create requests"() {
        when:
            nitfPostProcessPlugin.processCreate(createProcessRequest)
        then:
            processResource.getMimeType() >> "text/xml"
            0 * metacard.setAttribute(_)
    }

    def "handling non-nitf resources in update requests"() {
        when:
            nitfPostProcessPlugin.processUpdate(updateProcessRequest)
        then:
            processResource.getMimeType() >> "text/xml"
            0 * metacard.setAttribute(_)
    }

    def "handling metacards containing resource uri attribute during create requests"() {
        setup:
            def mockAttribute = Mock(Attribute)
        when: "a metacard has an existing resource uri attribute"
            mockAttribute.name >> Core.DERIVED_RESOURCE_URI
            mockAttribute.values >> ["fake"]
            metacard.getAttribute(Core.DERIVED_RESOURCE_URI) >> mockAttribute
            nitfPostProcessPlugin.processCreate(createProcessRequest)
        then: "the existing attribute should be copied and updated"
            2 * metacard.setAttribute({it.name = Core.DERIVED_RESOURCE_URI; it.values.contains("fake")})
    }

    def "handling metacards containing resource uri attribute during update requests"() {
        setup:
            def mockAttribute = Mock(Attribute)
        when: "a metacard has an existing resource uri attribute"
            mockAttribute.name >> Core.DERIVED_RESOURCE_URI
            mockAttribute.values >> ["fake"]
            metacard.getAttribute(Core.DERIVED_RESOURCE_URI) >> mockAttribute
            nitfPostProcessPlugin.processUpdate(updateProcessRequest)
        then: "the existing attribute should be copied and updated"
            2 * metacard.setAttribute({it.name = Core.DERIVED_RESOURCE_URI; it.values.contains("fake")})
    }

    def "handle invalid max side length"() {
        when: "max side length is set to < 0"
            nitfPostProcessPlugin.setMaxSideLength(-1)
        then: "max side length should get set to the default value"
            nitfPostProcessPlugin.maxSideLength == DEFAULT_MAX_SIDE_LENGTH
    }

    def "handle zero max side length"() {
        when: "max side length is set to 0"
            nitfPostProcessPlugin.setMaxSideLength(0)
        then: "max side length should get set to the default value"
            nitfPostProcessPlugin.maxSideLength == DEFAULT_MAX_SIDE_LENGTH
    }

    def "handle valid max side length"() {
        setup:
            def maxSideLength = 1
        when: "max side length is set to > 0"
            nitfPostProcessPlugin.setMaxSideLength(maxSideLength)
        then:
            nitfPostProcessPlugin.maxSideLength == maxSideLength
    }

    def "handle multiple nitf resources in create requests"() {
        setup:
            interaction {
                multiNitfInteractions()
            }
        when: "process request contains multiple nitf resources"

            nitfPostProcessPlugin.processCreate(createProcessRequest)
        then: "resources should be added to their respective metacards"
            2 * metacard.setAttribute({it.name = Core.DERIVED_RESOURCE_URI; it.value != null})
            1 * metacard.setAttribute({it.name == Core.THUMBNAIL; it.value != null})
            1 * processCreateItem.markMetacardAsModified()
            2 * metacard1.setAttribute({it.name = Core.DERIVED_RESOURCE_URI; it.value != null})
            1 * metacard1.setAttribute({it.name == Core.THUMBNAIL; it.value != null})
            1 * processCreateItem1.markMetacardAsModified()
    }

    def "handle multiple nitf resources in update requests"() {
        setup:
            interaction {
                multiNitfInteractions()
            }
        when: "process request contains multiple nitf resources"

            nitfPostProcessPlugin.processUpdate(updateProcessRequest)
        then: "resources should be added to their respective metacards"
            2 * metacard.setAttribute({it.name = Core.DERIVED_RESOURCE_URI; it.value != null})
            1 * metacard.setAttribute({it.name == Core.THUMBNAIL; it.value != null})
            1 * processUpdateItem.markMetacardAsModified()
            2 * metacard1.setAttribute({it.name = Core.DERIVED_RESOURCE_URI; it.value != null})
            1 * metacard1.setAttribute({it.name == Core.THUMBNAIL; it.value != null})
            1 * processUpdateItem1.markMetacardAsModified()
    }

    def "handle mixed resources in create requests"() {
        setup:
            interaction {
                multiTypeInteractions()
            }
        when: "process request contains multiple mixed resources"
            nitfPostProcessPlugin.processCreate(createProcessRequest)
        then: "only nitf type resources should be processed"
            2 * metacard.setAttribute({it.name = Core.DERIVED_RESOURCE_URI; it.value != null})
            1 * metacard.setAttribute({it.name == Core.THUMBNAIL; it.value != null})
            1 * processCreateItem.markMetacardAsModified()
            0 * metacard1.setAttribute(_)
            0 * processCreateItem1.markMetacardAsModified()
    }

    def "handle mixed resources in update requests"() {
        when: "process request contains multiple mixed resources"
            interaction {
               multiTypeInteractions()
            }
            nitfPostProcessPlugin.processUpdate(updateProcessRequest)
        then: "only nitf type resources should be processed"
            2 * metacard.setAttribute({it.name = Core.DERIVED_RESOURCE_URI; it.value != null})
            1 * metacard.setAttribute({it.name == Core.THUMBNAIL; it.value != null})
            1 * processUpdateItem.markMetacardAsModified()
            0 * metacard1.setAttribute(_)
            0 * processUpdateItem1.markMetacardAsModified()
    }

    def getTestResource(String filename) {
        def resourceURI = getClass().getResource(filename).toURI()
        def resourceInputStream = getClass().getResourceAsStream(filename)
        return [resourceURI, resourceInputStream]
    }

    def multiNitfInteractions() {
        def (resUri, resStream) = getTestResource(GEO_NITF)

        processResource1 = Mock(ProcessResource) {
            getUri() >> resUri
            getInputStream() >> resStream
            getMimeType() >> NitfPostProcessPlugin.NITF_MIME_TYPE.toString()
        }

        processCreateItem1 = Mock(ProcessCreateItem) {
            getMetacard() >> metacard1
            getProcessResource() >> processResource1
        }

        processUpdateItem1 = Mock(ProcessUpdateItem) {
            getMetacard() >> metacard1
            getOldMetacard() >> Mock(Metacard)
            getProcessResource() >> processResource1
        }

        processCreateItemList.add(processCreateItem1)
        processUpdateItemList.add(processUpdateItem1)
    }

    def multiTypeInteractions() {
        def (resUri) = getTestResource(GEO_NITF)

        processResource1 = Mock(ProcessResource) {
            getUri() >> resUri
            getInputStream() >> new ByteArrayInputStream("<xml>...</xml>".getBytes())
            getMimeType() >> "text/xml"
        }

        processCreateItem1 = Mock(ProcessCreateItem) {
            getMetacard() >> metacard1
            getProcessResource() >> processResource1
        }

        processUpdateItem1 = Mock(ProcessUpdateItem) {
            getMetacard() >> metacard1
            getOldMetacard() >> Mock(Metacard)
            getProcessResource() >> processResource1
        }

        processCreateItemList.add(processCreateItem1)
        processUpdateItemList.add(processUpdateItem1)
    }

    def generateSizeLimit() {
       return Math.round(TEST_SIZE / 1024 / 1024 / 2) as int
    }
}