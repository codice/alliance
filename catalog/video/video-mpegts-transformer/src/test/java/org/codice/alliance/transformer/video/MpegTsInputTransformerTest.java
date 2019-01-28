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
package org.codice.alliance.transformer.video;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.types.Core;
import ddf.catalog.data.types.Media;
import ddf.catalog.transform.CatalogTransformerException;
import ddf.catalog.transform.InputTransformer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.codice.alliance.catalog.core.internal.api.classification.SecurityClassificationService;
import org.codice.alliance.libs.klv.KlvHandler;
import org.codice.alliance.libs.klv.KlvHandlerFactory;
import org.codice.alliance.libs.klv.KlvProcessor;
import org.codice.alliance.libs.klv.SecurityClassificationKlvProcessor;
import org.codice.alliance.libs.klv.Stanag4609ParseException;
import org.codice.alliance.libs.klv.Stanag4609Processor;
import org.codice.alliance.libs.klv.StanagParserFactory;
import org.codice.alliance.libs.stanag4609.Stanag4609TransportStreamParser;
import org.junit.Before;
import org.junit.Test;

public class MpegTsInputTransformerTest {

  private static final String CLASSIFICATION = "foo";

  private static final Double DISTANCE_TOLERANCE = 0.0001;

  private List<MetacardType> metacardTypes;

  private Stanag4609Processor stanag4609Processor;

  private KlvHandlerFactory klvHandlerFactory;

  private KlvHandler defaultKlvHandler;

  private Stanag4609TransportStreamParser streamParser;

  private MetacardImpl metacard;

  private InputTransformer inputTransformer;

  private StanagParserFactory stanagParserFactory;

  private KlvProcessor klvProcessor;

  @Before
  public void setup() throws IOException, CatalogTransformerException {
    metacardTypes = Collections.singletonList(mock(MetacardType.class));
    stanag4609Processor = mock(Stanag4609Processor.class);
    klvHandlerFactory = mock(KlvHandlerFactory.class);
    defaultKlvHandler = mock(KlvHandler.class);
    streamParser = mock(Stanag4609TransportStreamParser.class);
    metacard = new MetacardImpl();
    inputTransformer = mock(InputTransformer.class);
    stanagParserFactory = mock(StanagParserFactory.class);
    klvProcessor = mock(KlvProcessor.class);
    when(inputTransformer.transform(any(), any())).thenReturn(metacard);
    when(stanagParserFactory.createParser(any()))
        .thenReturn(
            () -> {
              try {
                return streamParser.parse();
              } catch (Exception e) {
                throw new Stanag4609ParseException(e);
              }
            });
  }

  @Test
  public void testSetSecurityClassificationCode1() {
    assertSecurityClassificationCode(
        mpegTsInputTransformer ->
            mpegTsInputTransformer.setSecurityClassificationCode1(CLASSIFICATION),
        (short) 1);
  }

  @Test
  public void testSetSecurityClassificationCode2() {
    assertSecurityClassificationCode(
        mpegTsInputTransformer ->
            mpegTsInputTransformer.setSecurityClassificationCode2(CLASSIFICATION),
        (short) 2);
  }

  @Test
  public void testSetSecurityClassificationCode3() {
    assertSecurityClassificationCode(
        mpegTsInputTransformer ->
            mpegTsInputTransformer.setSecurityClassificationCode3(CLASSIFICATION),
        (short) 3);
  }

  @Test
  public void testSetSecurityClassificationCode4() {
    assertSecurityClassificationCode(
        mpegTsInputTransformer ->
            mpegTsInputTransformer.setSecurityClassificationCode4(CLASSIFICATION),
        (short) 4);
  }

  @Test
  public void testSetSecurityClassificationCode5() {
    assertSecurityClassificationCode(
        mpegTsInputTransformer ->
            mpegTsInputTransformer.setSecurityClassificationCode5(CLASSIFICATION),
        (short) 5);
  }

  @Test
  public void testSetSecurityClassificationCodeDefault() {
    SecurityClassificationKlvProcessor processor =
        spy(
            new SecurityClassificationKlvProcessor(
                mock(SecurityClassificationService.class), Collections.emptyMap(), ""));

    MpegTsInputTransformer transformer =
        new MpegTsInputTransformer(
            inputTransformer,
            metacardTypes,
            stanag4609Processor,
            klvHandlerFactory,
            defaultKlvHandler,
            stanagParserFactory,
            processor,
            DISTANCE_TOLERANCE);

    transformer.setSecurityClassificationDefault(CLASSIFICATION);

    verify(processor).setDefaultSecurityClassification(CLASSIFICATION);
  }

  @Test
  public void testCopyInputTransformerAttributes() throws Exception {

    metacard.setContentTypeName("some/thing");
    metacard.setMetadata("the metadata");

    when(streamParser.parse()).thenReturn(Collections.emptyMap());

    MpegTsInputTransformer t =
        new MpegTsInputTransformer(
            inputTransformer,
            metacardTypes,
            stanag4609Processor,
            klvHandlerFactory,
            defaultKlvHandler,
            stanagParserFactory,
            klvProcessor,
            DISTANCE_TOLERANCE);

    try (InputStream inputStream = new ByteArrayInputStream(new byte[] {})) {

      Metacard finalMetacard = t.transform(inputStream);

      assertThat(finalMetacard.getAttribute(Media.TYPE), is(notNullValue()));
      assertThat(
          finalMetacard.getAttribute(Media.TYPE).getValue(),
          is(MpegTsInputTransformer.CONTENT_TYPE));
      assertThat(finalMetacard.getContentTypeName(), is(MpegTsInputTransformer.CONTENT_TYPE));
      assertThat(finalMetacard.getMetadata(), is("the metadata"));
    }
  }

  @Test
  public void testDataTypeField() throws Exception {

    MpegTsInputTransformer t =
        new MpegTsInputTransformer(
            inputTransformer,
            metacardTypes,
            stanag4609Processor,
            klvHandlerFactory,
            defaultKlvHandler,
            stanagParserFactory,
            klvProcessor,
            DISTANCE_TOLERANCE);

    try (InputStream inputStream = new ByteArrayInputStream(new byte[] {})) {

      Metacard finalMetacard = t.transform(inputStream);

      assertThat(
          finalMetacard.getAttribute(Core.DATATYPE),
          is(new AttributeImpl(Core.DATATYPE, MpegTsInputTransformer.DATA_TYPE)));
    }
  }

  @Test(expected = CatalogTransformerException.class)
  public void testStanagParseError() throws Exception {

    when(streamParser.parse()).thenThrow(new RuntimeException());

    MpegTsInputTransformer t =
        new MpegTsInputTransformer(
            inputTransformer,
            metacardTypes,
            stanag4609Processor,
            klvHandlerFactory,
            defaultKlvHandler,
            stanagParserFactory,
            klvProcessor,
            DISTANCE_TOLERANCE);

    try (InputStream inputStream = new ByteArrayInputStream(new byte[] {})) {
      t.transform(inputStream);
    }
  }

  @Test(expected = CatalogTransformerException.class)
  public void testInputStreamReadError() throws Exception {

    MpegTsInputTransformer t =
        new MpegTsInputTransformer(
            inputTransformer,
            metacardTypes,
            stanag4609Processor,
            klvHandlerFactory,
            defaultKlvHandler,
            stanagParserFactory,
            klvProcessor,
            DISTANCE_TOLERANCE);

    InputStream inputStream = mock(InputStream.class);
    when(inputStream.read(any())).thenThrow(new IOException());

    t.transform(inputStream);
  }

  @Test
  public void testSetDistanceTolerance() {

    MpegTsInputTransformer t =
        new MpegTsInputTransformer(
            inputTransformer,
            metacardTypes,
            stanag4609Processor,
            klvHandlerFactory,
            defaultKlvHandler,
            stanagParserFactory,
            klvProcessor,
            DISTANCE_TOLERANCE);
    double value = 10;
    t.setDistanceTolerance(value);
    assertThat(t.getDistanceTolerance(), closeTo(value, 0.1));
  }

  private void assertSecurityClassificationCode(Consumer<MpegTsInputTransformer> c, short code) {
    SecurityClassificationKlvProcessor processor =
        spy(
            new SecurityClassificationKlvProcessor(
                mock(SecurityClassificationService.class), Collections.emptyMap(), ""));

    MpegTsInputTransformer transformer =
        new MpegTsInputTransformer(
            inputTransformer,
            metacardTypes,
            stanag4609Processor,
            klvHandlerFactory,
            defaultKlvHandler,
            stanagParserFactory,
            processor,
            DISTANCE_TOLERANCE);

    c.accept(transformer);

    verify(processor).setSecurityClassification(code, CLASSIFICATION);
  }
}
