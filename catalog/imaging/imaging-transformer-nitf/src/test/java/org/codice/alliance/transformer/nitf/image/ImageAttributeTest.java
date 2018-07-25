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
package org.codice.alliance.transformer.nitf.image;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.codice.imaging.nitf.core.image.ImageSegment;
import org.junit.Before;
import org.junit.Test;

public class ImageAttributeTest {

  private static final String COMMENT_1 = "comment 1";

  private static final String COMMENT_2 = "comment 2";

  private static final String COMMENT_3 = "comment 3";

  private static final String COMMENT_4 = "comment 4";

  private static final String COMMENT_5 = "comment 5";

  private static final String COMMENT_6 = "comment 6";

  private static final String COMMENT_7 = "comment 7";

  private static final String COMMENT_8 = "comment 8";

  private static final String COMMENT_9 = "comment 9";

  private ImageSegment imageSegment;

  @Before
  public void setUp() {
    this.imageSegment = mock(ImageSegment.class);
    List<String> imageSegmentComments = new ArrayList<>();
    imageSegmentComments.add(COMMENT_1);
    imageSegmentComments.add(COMMENT_2);
    imageSegmentComments.add(COMMENT_3);
    imageSegmentComments.add(COMMENT_4);
    imageSegmentComments.add(COMMENT_5);
    imageSegmentComments.add(COMMENT_6);
    imageSegmentComments.add(COMMENT_7);
    imageSegmentComments.add(COMMENT_8);
    imageSegmentComments.add(COMMENT_9);
    when(imageSegment.getImageComments()).thenReturn(imageSegmentComments);
  }

  @Test
  public void testImageAttributes() {
    ImageAttribute.getAttributes()
        .forEach(
            attribute ->
                assertThat(attribute.getShortName(), is(org.hamcrest.Matchers.notNullValue())));

    assertThat(
        ImageAttribute.IMAGE_COMMENT_1_ATTRIBUTE.getAccessorFunction().apply(imageSegment),
        is(COMMENT_1));
    assertThat(
        ImageAttribute.IMAGE_COMMENT_2_ATTRIBUTE.getAccessorFunction().apply(imageSegment),
        is(COMMENT_2));
    assertThat(
        ImageAttribute.IMAGE_COMMENT_3_ATTRIBUTE.getAccessorFunction().apply(imageSegment),
        is(COMMENT_3));
    assertThat(
        ImageAttribute.IMAGE_COMMENT_4_ATTRIBUTE.getAccessorFunction().apply(imageSegment),
        is(COMMENT_4));
    assertThat(
        ImageAttribute.IMAGE_COMMENT_5_ATTRIBUTE.getAccessorFunction().apply(imageSegment),
        is(COMMENT_5));
    assertThat(
        ImageAttribute.IMAGE_COMMENT_6_ATTRIBUTE.getAccessorFunction().apply(imageSegment),
        is(COMMENT_6));
    assertThat(
        ImageAttribute.IMAGE_COMMENT_7_ATTRIBUTE.getAccessorFunction().apply(imageSegment),
        is(COMMENT_7));
    assertThat(
        ImageAttribute.IMAGE_COMMENT_8_ATTRIBUTE.getAccessorFunction().apply(imageSegment),
        is(COMMENT_8));
    assertThat(
        ImageAttribute.IMAGE_COMMENT_9_ATTRIBUTE.getAccessorFunction().apply(imageSegment),
        is(COMMENT_9));

    assertThat(
        ImageAttribute.NITF_TARGET_IDENTIFIER_ATTRIBUTE.getAccessorFunction().apply(imageSegment),
        is(nullValue()));
    assertThat(
        ImageAttribute.ISR_TARGET_IDENTIFIER_ATTRIBUTE.getAccessorFunction().apply(imageSegment),
        is(nullValue()));
    assertThat(
        ImageAttribute.TARGET_IDENTIFIER_COUNTRY_CODE_ATTRIBUTE
            .getAccessorFunction()
            .apply(imageSegment),
        is(nullValue()));
    assertThat(
        ImageAttribute.TARGET_IDENTIFIER_COUNTRY_CODE_ATTRIBUTE
            .getAccessorFunction()
            .apply(imageSegment),
        is(nullValue()));
    assertThat(
        ImageAttribute.IMAGE_DATE_AND_TIME_ATTRIBUTE.getAccessorFunction().apply(imageSegment),
        is(nullValue()));
    assertThat(
        ImageAttribute.IMAGE_IDENTIFIER_2_ATTRIBUTE.getAccessorFunction().apply(imageSegment),
        is(nullValue()));
    assertThat(
        ImageAttribute.NUMBER_OF_SIGNIFICANT_ROWS_IN_IMAGE_ATTRIBUTE
            .getAccessorFunction()
            .apply(imageSegment),
        is(0L));
    assertThat(
        ImageAttribute.NUMBER_OF_SIGNIFICANT_COLUMNS_IN_IMAGE_ATTRIBUTE
            .getAccessorFunction()
            .apply(imageSegment),
        is(0L));
    assertThat(
        ImageAttribute.IMAGE_SOURCE_ATTRIBUTE.getAccessorFunction().apply(imageSegment),
        is(nullValue()));
  }
}
