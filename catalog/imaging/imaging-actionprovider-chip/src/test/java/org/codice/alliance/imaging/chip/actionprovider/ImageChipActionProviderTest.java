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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import ddf.action.Action;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.impl.MetacardTypeImpl;
import ddf.catalog.data.impl.types.CoreAttributes;
import ddf.catalog.data.types.Core;
import java.util.Arrays;
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
        new MetacardTypeImpl(NITF_IMAGE_METACARD_TYPE, Arrays.asList(new CoreAttributes())));
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
        new MetacardTypeImpl("Non Imagery MetacardType", Arrays.asList(new CoreAttributes())));
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
        new AttributeImpl(
            Core.DERIVED_RESOURCE_URI, "content:73baa01ad925463b962084477d19fde0#NotOriginal"));
    assertThat(imagingChipActionProvider.canHandle(imageMetacard), is(false));
  }

  @Test
  public void testCanHandleOriginalDerivedResource() {
    imageMetacard.setAttribute(
        new AttributeImpl(
            Core.DERIVED_RESOURCE_URI,
            "https://example.com/download?transform=resource&qualifier=original"));
    assertThat(imagingChipActionProvider.canHandle(imageMetacard), is(true));
  }

  @Test
  public void testDoesNotHandleNonOverviewDerivedResource() {
    imageMetacard.setAttribute(
        new AttributeImpl(
            Core.DERIVED_RESOURCE_URI,
            "https://example.com/download?transform=resource&qualifier=notoriginal"));
    assertThat(imagingChipActionProvider.canHandle(imageMetacard), is(false));
  }

  @Test
  public void testCanHandleImageryMetacard() {
    assertThat(imagingChipActionProvider.canHandle(imageMetacard), is(true));
  }

  @Test
  public void testGetId() {
    assertThat(imagingChipActionProvider.getId(), is(ImagingChipActionProvider.ID));
  }

  @Test
  public void testGetActionsNullMetacard() {
    assertThat(imagingChipActionProvider.getActions(null), hasSize(0));
  }

  @Test
  public void testGetActionsCanNotHandleMetacard() {
    imageMetacard.setLocation(null);
    assertThat(imagingChipActionProvider.getActions(imageMetacard), hasSize(0));
  }

  @Test
  public void testGetActionsMetacard() {
    List<Action> actions = imagingChipActionProvider.getActions(imageMetacard);
    assertThat(actions, hasSize(1));

    Action action = actions.get(0);
    assertThat(action.getId(), is(ImagingChipActionProvider.ID));
    assertThat(action.getDescription(), is(ImagingChipActionProvider.DESCRIPTION));
    assertThat(action.getTitle(), is(ImagingChipActionProvider.TITLE));

    String url = action.getUrl().toString();
    assertThat(url, containsString(ImagingChipActionProvider.PATH));
    assertThat(url, containsString("id=" + ID));
    assertThat(url, containsString("source=" + SOURCE));
  }
}
