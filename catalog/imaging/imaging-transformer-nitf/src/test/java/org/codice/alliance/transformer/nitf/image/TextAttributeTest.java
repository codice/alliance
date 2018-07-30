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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import org.codice.imaging.nitf.core.common.DateTime;
import org.codice.imaging.nitf.core.common.impl.DateTimeImpl;
import org.codice.imaging.nitf.core.security.SecurityMetadata;
import org.codice.imaging.nitf.core.text.TextFormat;
import org.codice.imaging.nitf.core.text.TextSegment;
import org.junit.Before;
import org.junit.Test;

public class TextAttributeTest {

  private static final int TEXT_ATTACHMENT_LEVEL = 0;

  private static final int EXTENDED_HEADER_DATA_OVERFLOW = 0;

  private static final String TEXT_TITLE = "TEXT_TITLE";

  private static final TextFormat TEXT_FORMAT = TextFormat.USMTF;

  private static final String TEXT_IDENTIFIER = "101";

  private TextSegment textSegment;

  @Before
  public void setUp() {
    this.textSegment = mock(TextSegment.class);
    final DateTime currentDateTime = DateTimeImpl.getNitfDateTimeForNow();
    when(textSegment.getIdentifier()).thenReturn(TEXT_IDENTIFIER);
    when(textSegment.getSecurityMetadata()).thenReturn(mock(SecurityMetadata.class));
    when(textSegment.getTextDateTime()).thenReturn(currentDateTime);
    when(textSegment.getTextFormat()).thenReturn(TEXT_FORMAT);
    when(textSegment.getTextTitle()).thenReturn(TEXT_TITLE);
    when(textSegment.getAttachmentLevel()).thenReturn(TEXT_ATTACHMENT_LEVEL);
    when(textSegment.getExtendedHeaderDataOverflow()).thenReturn(EXTENDED_HEADER_DATA_OVERFLOW);
  }

  @Test
  public void testLongAndShortNames() {
    Stream.of(TextAttribute.values())
        .forEach(attribute -> assertThat(attribute.getShortName(), is(notNullValue())));

    Stream.of(TextAttribute.values())
        .forEach(attribute -> assertThat(attribute.getLongName(), is(notNullValue())));

    Stream.of(TextAttribute.values())
        .forEach(attribute -> assertThat(attribute.toString(), is(attribute.getLongName())));

    assertThat(
        TextAttribute.TEXT_ATTACHMENT_LEVEL.getAccessorFunction().apply(textSegment),
        is(TEXT_ATTACHMENT_LEVEL));
    assertThat(
        TextAttribute.TEXT_IDENTIFIER.getAccessorFunction().apply(textSegment),
        is(TEXT_IDENTIFIER));
    assertThat(
        TextAttribute.TEXT_DATE_AND_TIME.getAccessorFunction().apply(textSegment), notNullValue());
    assertThat(TextAttribute.TEXT_TITLE.getAccessorFunction().apply(textSegment), is(TEXT_TITLE));
    assertThat(
        TextAttribute.TEXT_EXTENDED_SUBHEADER_DATA_LENGTH.getAccessorFunction().apply(textSegment),
        is(EXTENDED_HEADER_DATA_OVERFLOW));

    assertThat(
        TextAttribute.TEXT_SECURITY_CLASSIFICATION.getAccessorFunction().apply(textSegment),
        nullValue());
    assertThat(
        TextAttribute.TEXT_CLASSIFICATION_SECURITY_SYSTEM.getAccessorFunction().apply(textSegment),
        nullValue());
    assertThat(TextAttribute.TEXT_CODEWORDS.getAccessorFunction().apply(textSegment), nullValue());
    assertThat(
        TextAttribute.TEXT_CONTROL_AND_HANDLING.getAccessorFunction().apply(textSegment),
        nullValue());
    assertThat(
        TextAttribute.TEXT_RELEASING_INSTRUCTIONS.getAccessorFunction().apply(textSegment),
        nullValue());
    assertThat(
        TextAttribute.TEXT_DECLASSIFICATION_DATE.getAccessorFunction().apply(textSegment),
        nullValue());
    assertThat(
        TextAttribute.TEXT_DECLASSIFICATION_TYPE.getAccessorFunction().apply(textSegment),
        nullValue());
    assertThat(
        TextAttribute.TEXT_DECLASSIFICATION_EXEMPTION.getAccessorFunction().apply(textSegment),
        nullValue());
    assertThat(TextAttribute.TEXT_DOWNGRADE.getAccessorFunction().apply(textSegment), nullValue());
    assertThat(
        TextAttribute.TEXT_DOWNGRADE_DATE.getAccessorFunction().apply(textSegment), nullValue());
    assertThat(
        TextAttribute.TEXT_CLASSIFICATION_TEXT.getAccessorFunction().apply(textSegment),
        nullValue());
    assertThat(
        TextAttribute.TEXT_CLASSIFICATION_AUTHORITY_TYPE.getAccessorFunction().apply(textSegment),
        nullValue());
    assertThat(
        TextAttribute.TEXT_CLASSIFICATION_AUTHORITY.getAccessorFunction().apply(textSegment),
        nullValue());
    assertThat(
        TextAttribute.TEXT_CLASSIFICATION_REASON.getAccessorFunction().apply(textSegment),
        nullValue());
    assertThat(
        TextAttribute.TEXT_SECURITY_SOURCE_DATE.getAccessorFunction().apply(textSegment),
        nullValue());
    assertThat(
        TextAttribute.TEXT_CLASSIFICATION_SECURITY_SYSTEM.getAccessorFunction().apply(textSegment),
        nullValue());
    assertThat(
        TextAttribute.TEXT_SECURITY_CONTROL_NUMBER.getAccessorFunction().apply(textSegment),
        nullValue());
  }
}
