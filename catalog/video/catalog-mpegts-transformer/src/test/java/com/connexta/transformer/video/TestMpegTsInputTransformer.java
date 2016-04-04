/**
 * Copyright (c) Connexta, LLC
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
package com.connexta.transformer.video;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.connexta.alliance.libs.klv.KlvHandler;
import com.connexta.alliance.libs.klv.KlvHandlerFactory;
import com.connexta.alliance.libs.klv.KlvProcessor;
import com.connexta.alliance.libs.klv.Stanag4609ParseException;
import com.connexta.alliance.libs.klv.Stanag4609Processor;
import com.connexta.alliance.libs.klv.StanagParserFactory;
import com.connexta.alliance.libs.stanag4609.Stanag4609TransportStreamParser;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.BasicTypes;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.transform.CatalogTransformerException;
import ddf.catalog.transform.InputTransformer;

public class TestMpegTsInputTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestMpegTsInputTransformer.class);

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
        metacard = new MetacardImpl(BasicTypes.BASIC_METACARD);
        inputTransformer = mock(InputTransformer.class);
        stanagParserFactory = mock(StanagParserFactory.class);
        klvProcessor = mock(KlvProcessor.class);
        when(inputTransformer.transform(any(), any())).thenReturn(metacard);
        when(stanagParserFactory.createParser(any())).thenReturn(() -> {
                    try {
                        return streamParser.parse();
                    } catch (Exception e) {
                        throw new Stanag4609ParseException(e);
                    }
                }

        );
    }

    @Test
    public void testCopyInputTransformerAttributes() throws Exception {

        metacard.setContentTypeName("some/thing");
        metacard.setMetadata("the metadata");

        when(streamParser.parse()).thenReturn(Collections.emptyMap());

        MpegTsInputTransformer t = new MpegTsInputTransformer(inputTransformer,
                metacardTypes,
                stanag4609Processor,
                klvHandlerFactory,
                defaultKlvHandler,
                stanagParserFactory,
                klvProcessor);

        try (InputStream inputStream = new ByteArrayInputStream(new byte[] {})) {

            Metacard finalMetacard = t.transform(inputStream);

            assertThat(finalMetacard.getContentTypeName(), is(MpegTsInputTransformer.CONTENT_TYPE));
            assertThat(finalMetacard.getMetadata(), is("the metadata"));

        }

    }

    @Test(expected = CatalogTransformerException.class)
    public void testStanagParseError() throws Exception {

        when(streamParser.parse()).thenThrow(new RuntimeException());

        MpegTsInputTransformer t = new MpegTsInputTransformer(inputTransformer,
                metacardTypes,
                stanag4609Processor,
                klvHandlerFactory,
                defaultKlvHandler,
                stanagParserFactory,
                klvProcessor);

        try (InputStream inputStream = new ByteArrayInputStream(new byte[] {})) {
            t.transform(inputStream);
        }

    }

    @Test(expected = CatalogTransformerException.class)
    public void testInputStreamReadError() throws Exception {

        MpegTsInputTransformer t = new MpegTsInputTransformer(inputTransformer,
                metacardTypes,
                stanag4609Processor,
                klvHandlerFactory,
                defaultKlvHandler,
                stanagParserFactory,
                klvProcessor);

        InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any())).thenThrow(new IOException());

        t.transform(inputStream);

    }
}