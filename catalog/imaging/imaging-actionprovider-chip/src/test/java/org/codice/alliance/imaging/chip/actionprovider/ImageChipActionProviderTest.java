/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.imaging.chip.actionprovider;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import ddf.action.Action;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.impl.MetacardTypeImpl;
import ddf.catalog.data.impl.types.CoreAttributes;
import ddf.catalog.data.types.Core;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class ImageChipActionProviderTest {

  private static final String ID = "12345";

  private static final String SOURCE = "alliance.distribution";

  private static final String NITF_IMAGE_METACARD_TYPE = "isr.image";

  private static final String LOCATION =
      "POLYGON ((0.1234 2.222, 0.4444 1.222, 0.1234 1.222, 0.1234 2.222, 0.1234 2.222))";

  private ImagingChipActionProvider imagingChipActionProvider;

  private MetacardImpl imageMetacard;

  @Before
  public void setUp() {
    imagingChipActionProvider = new ImagingChipActionProvider();

    imageMetacard = new MetacardImpl();
    imageMetacard.setType(
        new MetacardTypeImpl(
            NITF_IMAGE_METACARD_TYPE, Collections.singletonList(new CoreAttributes())));
    imageMetacard.setId(ID);
    imageMetacard.setSourceId(SOURCE);
    imageMetacard.setLocation(LOCATION);
    imageMetacard.setAttribute(
        new AttributeImpl(
            Core.DERIVED_RESOURCE_URI, "content:73baa01ad925463b962084477d19fde0#original"));
  }

  @Test
  public void testDoesNotHandleNullMetacard() {
    assertThat(imagingChipActionProvider.canHandle(null), is(false));
  }

  @Test
  public void testDoesNotHandleInvalidMetacard() {
    assertThat(imagingChipActionProvider.canHandle(new Object()), is(false));
  }

  @Test
  public void testDoesNotHandleNonImageryMetacard() {
    imageMetacard.setType(
        new MetacardTypeImpl(
            "Non Imagery MetacardType", Collections.singletonList(new CoreAttributes())));
    assertThat(imagingChipActionProvider.canHandle(imageMetacard), is(false));
  }

  @Test
  public void testDoesNotHandleNoLocationOnMetacard() {
    imageMetacard.setLocation(null);
    assertThat(imagingChipActionProvider.canHandle(imageMetacard), is(false));
  }

  @Test
  public void testDoesNotHandleInvalidLocationOnMetacard() {
    imageMetacard.setLocation("BADWKT (0 0)");
    assertThat(imagingChipActionProvider.canHandle(imageMetacard), is(false));
  }

  @Test
  public void testDoesNotHandleNoOriginalDerivedResource() {
    imageMetacard.setAttribute(
        new AttributeImpl(Core.DERIVED_RESOURCE_URI, "content:someMetacardId#overview"));
    assertThat(imagingChipActionProvider.canHandle(imageMetacard), is(false));
  }

  @Test
  public void testDoesNotHandleNoOverviewDerivedResource() {
    imageMetacard.setAttribute(
        new AttributeImpl(Core.DERIVED_RESOURCE_URI, "content:someMetacardId#original"));
    assertThat(imagingChipActionProvider.canHandle(imageMetacard), is(false));
  }

  @Test
  public void testGetId() {
    assertThat(imagingChipActionProvider.getId(), is(ImagingChipActionProvider.ID));
  }

  @Test
  public void testCanHandle() throws Exception {
    Metacard contentMetcard = makeMetacardWithDerivedResourceUri("content:metacardId#%s");
    boolean canHandle = imagingChipActionProvider.canHandle(contentMetcard);
    assertThat(canHandle, is(true));

    Metacard derivedResourceMetcard =
        makeMetacardWithDerivedResourceUri(
            "http://derivedResourceHost:5678/services/catalog/sources/derivedResourceSourceId/derivedResourceMetacardId?transform=resource&qualifier=%s");
    canHandle = imagingChipActionProvider.canHandle(derivedResourceMetcard);
    assertThat(canHandle, is(true));
  }

  @Test
  public void testContentAction() throws Exception {
    List<Action> actions = chippingActions("content:metacardId#%s");

    assertThat(actions, hasSize(1));

    Action action = actions.get(0);
    assertThat(action.getId(), is(ImagingChipActionProvider.ID));
    assertThat(
        action.getUrl(),
        equalTo(
            new URL(
                "https://localhost:8993/chipping/chipping.html?id=metacardId&source=metacardSourceId")));

    actions =
        chippingActions(
            "http://derivedResourceHost:5678/services/catalog/sources/derivedResourceSourceId/derivedResourceMetacardId?transform=resource&qualifier=%s");

    action = actions.get(0);
    assertThat(
        action.getUrl(),
        equalTo(
            new URL(
                "http://derivedResourceHost:5678/chipping/chipping.html?id=derivedResourceMetacardId&source=derivedResourceSourceId")));
  }

  @Test
  public void testUnsupportedDerivedResourceUris() throws Exception {
    assertThat(
        chippingActions(
            "https://:5678/services/catalog/sources/derivedResourceSourceId/derivedResourceMetacardId?transform=resource&qualifier=%s"),
        hasSize(0));
    assertThat(
        chippingActions(
            "notHttpNorHttpsProtocol://derivedResourceHost:5678/services/catalog/sources/derivedResourceSourceId/derivedResourceMetacardId?transform=resource&qualifier=%s"),
        hasSize(0));
    assertThat(
        chippingActions(
            "http://example.com/services/catalog/sources/derivedResourceSourceId/derivedResourceMetacardId?transform=resource&qualifier=%s"),
        hasSize(0));
    assertThat(
        chippingActions(
            "http://derivedResourceHost:5678/not/the/normal/path?transform=resource&qualifier=%s"),
        hasSize(0));
    assertThat(
        chippingActions(
            "http://derivedResourceHost:5678/services/catalog/sources/derivedResourceSourceId/derivedResourceMetacardId?notThe=normalQuery&qualifier=%s"),
        hasSize(0));
  }

  private List<Action> chippingActions(String derivedResourceUri) throws URISyntaxException {
    Metacard metacard = makeMetacardWithDerivedResourceUri(derivedResourceUri);
    imagingChipActionProvider.canHandle(metacard);

    return imagingChipActionProvider.getActions(metacard);
  }

  private Metacard makeMetacardWithDerivedResourceUri(String derivedResourceUriStringFormat)
      throws URISyntaxException {
    MetacardImpl metacard = new MetacardImpl();

    metacard.setType(new MetacardTypeImpl(NITF_IMAGE_METACARD_TYPE, List.of(new CoreAttributes())));
    metacard.setId("metacardId");
    metacard.setSourceId("metacardSourceId");
    metacard.setLocation(
        "POLYGON ((0.1234 2.222, 0.4444 1.222, 0.1234 1.222, 0.1234 2.222, 0.1234 2.222))");
    metacard.setResourceURI(new URI("someValidUriString"));
    String originalDerivedResourceUriString =
        String.format(derivedResourceUriStringFormat, "original");
    String overviewDerivedResourceUriString =
        String.format(derivedResourceUriStringFormat, "overview");
    metacard.setAttribute(
        new AttributeImpl(
            Core.DERIVED_RESOURCE_URI,
            List.of(originalDerivedResourceUriString, overviewDerivedResourceUriString)));

    return metacard;
  }
}
