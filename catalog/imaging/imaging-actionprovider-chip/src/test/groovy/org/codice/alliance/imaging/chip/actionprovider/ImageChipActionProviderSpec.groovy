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
package org.codice.alliance.imaging.chip.actionprovider

import ddf.action.Action
import ddf.catalog.data.impl.AttributeImpl
import ddf.catalog.data.impl.MetacardImpl
import ddf.catalog.data.impl.MetacardTypeImpl
import ddf.catalog.data.impl.types.CoreAttributes
import ddf.catalog.data.types.Core
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import spock.lang.Specification
import spock.lang.Unroll

import static org.codice.alliance.imaging.chip.actionprovider.ImagingChipActionProvider.NITF_IMAGE_METACARD_TYPE

@RunWith(JUnitPlatform.class)
class ImageChipActionProviderSpec extends Specification {

    public static final String METACARD_ID = "metacardId"
    public static final METACARD_SOURCE_ID = "metacardSourceId"

    @Unroll
    def 'test canHandle different derived resource URLs'(
            final String derivedResourceUriStringFormat) {
        given:
            final imagingChipActionProvider = new ImagingChipActionProvider()

            final metacard = new MetacardImpl()
            final metacardSourceId = "metacardSourceId"
        
            metacard.setType(new MetacardTypeImpl(NITF_IMAGE_METACARD_TYPE, Collections.singletonList(new CoreAttributes())))
            metacard.setId(METACARD_ID)
            metacard.setSourceId(metacardSourceId)
            metacard.setLocation("POLYGON ((0.1234 2.222, 0.4444 1.222, 0.1234 1.222, 0.1234 2.222, 0.1234 2.222))")
            metacard.setResourceURI(new URI("someValidUriString"))
            final originalDerivedResourceUriString = String.format(derivedResourceUriStringFormat, "original")
            final overviewDerivedResourceUriString = String.format(derivedResourceUriStringFormat, "overview")
            metacard.setAttribute(new AttributeImpl(Core.DERIVED_RESOURCE_URI, [originalDerivedResourceUriString, overviewDerivedResourceUriString]))

        expect:
            imagingChipActionProvider.canHandle(metacard)

        where:
            derivedResourceUriStringFormat                                                                                                                                  | _
            "content:" + METACARD_ID + "#%s"                                                                                                                                | _
            "http://derivedResourceHost:5678/services/catalog/sources/derivedResourceSourceId/derivedResourceMetacardId?transform=resource&qualifier=%s"                    | _
    }

    @Unroll
    def 'test can construct chipping URL for different derived resource URLs'(
            final String derivedResourceUriStringFormat,
            final String expectedChippingUrlString) {
        given:
            final imagingChipActionProvider = new ImagingChipActionProvider()

            final metacard = new MetacardImpl()

            metacard.setType(new MetacardTypeImpl("isr.image", Collections.singletonList(new CoreAttributes())))
            metacard.setId(METACARD_ID)
            metacard.setSourceId(METACARD_SOURCE_ID)
            metacard.setLocation("POLYGON ((0.1234 2.222, 0.4444 1.222, 0.1234 1.222, 0.1234 2.222, 0.1234 2.222))")
            metacard.setResourceURI(new URI("someValidUriString"))
            final originalDerivedResourceUriString = String.format(derivedResourceUriStringFormat, "original")
            final overviewDerivedResourceUriString = String.format(derivedResourceUriStringFormat, "overview")
            metacard.setAttribute(new AttributeImpl(Core.DERIVED_RESOURCE_URI, [originalDerivedResourceUriString, overviewDerivedResourceUriString]))

            // canHandle is always called before getActions
            imagingChipActionProvider.canHandle(metacard)

        when:
            final actions = imagingChipActionProvider.getActions(metacard)

        then:
            actions.size() == 1
            final Action action = actions.get(0)
            action.getId() == ImagingChipActionProvider.ID
            action.getDescription() == ImagingChipActionProvider.DESCRIPTION
            action.getTitle() == ImagingChipActionProvider.TITLE
            action.getUrl() == new URL(expectedChippingUrlString)

        where:
            derivedResourceUriStringFormat                                                                                                               || expectedChippingUrlString
            "content:" + METACARD_ID + "#%s"                                                                                                             || "https://localhost:8993/chipping/chipping.html?id=" + METACARD_ID + "&source=" + METACARD_SOURCE_ID
            "http://derivedResourceHost:5678/services/catalog/sources/derivedResourceSourceId/derivedResourceMetacardId?transform=resource&qualifier=%s" || "http://derivedResourceHost:5678/chipping/chipping.html?id=derivedResourceMetacardId&source=derivedResourceSourceId"
    }

    @Unroll
    def 'test cannot construct chipping URL for different derived resource URLs'(final String derivedResourceUriStringFormat) {
        given:
            final imagingChipActionProvider = new ImagingChipActionProvider()

            final metacard = new MetacardImpl()
            final metacardSourceId = "metacardSourceId"

            metacard.setType(new MetacardTypeImpl("isr.image", Collections.singletonList(new CoreAttributes())))
            metacard.setId(METACARD_ID)
            metacard.setSourceId(metacardSourceId)
            metacard.setLocation("POLYGON ((0.1234 2.222, 0.4444 1.222, 0.1234 1.222, 0.1234 2.222, 0.1234 2.222))")
            metacard.setResourceURI(new URI("someValidUriString"))
            final originalDerivedResourceUriString = String.format(derivedResourceUriStringFormat, "original")
            final overviewDerivedResourceUriString = String.format(derivedResourceUriStringFormat, "overview")
            metacard.setAttribute(new AttributeImpl(Core.DERIVED_RESOURCE_URI, [originalDerivedResourceUriString, overviewDerivedResourceUriString]))

            // canHandle is always called before getActions
            imagingChipActionProvider.canHandle(metacard)

        when:
            final actions = imagingChipActionProvider.getActions(metacard)

        then:
            actions.isEmpty()

        where:
            derivedResourceUriStringFormat                                                                                                                                  | _
            "https://:5678/services/catalog/sources/derivedResourceSourceId/derivedResourceMetacardId?transform=resource&qualifier=%s"                                      | _
            "notHttpNorHttpsProtocol://derivedResourceHost:5678/services/catalog/sources/derivedResourceSourceId/derivedResourceMetacardId?transform=resource&qualifier=%s" | _
            "http://example.com/services/catalog/sources/derivedResourceSourceId/derivedResourceMetacardId?transform=resource&qualifier=%s"                                 | _
            "http://derivedResourceHost:5678/not/the/normal/path?transform=resource&qualifier=%s"                                                                           | _
            "http://derivedResourceHost:5678/services/catalog/sources/derivedResourceSourceId/derivedResourceMetacardId?notThe=normalQuery&qualifier=%s"                    | _
    }
}