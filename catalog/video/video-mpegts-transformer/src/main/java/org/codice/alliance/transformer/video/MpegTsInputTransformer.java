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
package org.codice.alliance.transformer.video;

import static org.apache.commons.lang3.Validate.inclusiveBetween;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.codice.alliance.libs.klv.KlvHandler;
import org.codice.alliance.libs.klv.KlvHandlerFactory;
import org.codice.alliance.libs.klv.KlvProcessor;
import org.codice.alliance.libs.klv.Stanag4609ParseException;
import org.codice.alliance.libs.klv.Stanag4609Parser;
import org.codice.alliance.libs.klv.Stanag4609Processor;
import org.codice.alliance.libs.klv.StanagParserFactory;
import org.codice.alliance.libs.stanag4609.DecodedKLVMetadataPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.FileBackedOutputStream;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.transform.CatalogTransformerException;
import ddf.catalog.transform.InputTransformer;

public class MpegTsInputTransformer implements InputTransformer {

    public static final String CONTENT_TYPE = "video/mp2t";

    private static final Logger LOGGER = LoggerFactory.getLogger(MpegTsInputTransformer.class);

    private static final int FILE_THRESHOLD = 1000000;

    private static final Integer DEFAULT_SUBSAMPLE_COUNT = 50;

    private final InputTransformer innerTransformer;

    private final List<MetacardType> metacardTypes;

    private final Stanag4609Processor stanag4609Processor;

    private final KlvHandlerFactory klvHandlerFactory;

    private final StanagParserFactory stanagParserFactory;

    private final KlvProcessor klvProcessor;

    /**
     * The default handler is used to handle
     * metadata elements that do not have a dedicated handler. This handler simply logs a message
     * indicating that the metadata element is unhandled.
     */
    private final KlvHandler defaultKlvHandler;

    private Integer subsampleCount = DEFAULT_SUBSAMPLE_COUNT;

    /**
     * @param inputTransformer    inner input transformer (must be non-null)
     * @param metacardTypes       list of usable metacard types (must be non-null)
     * @param stanag4609Processor must be non-null
     * @param klvHandlerFactory   must be non-null
     * @param defaultKlvHandler   must be non-null
     * @param stanagParserFactory must be non-null
     * @param klvProcessor        processors to transfer klv to metacard (must be non-null)
     */
    public MpegTsInputTransformer(InputTransformer inputTransformer,
            List<MetacardType> metacardTypes, Stanag4609Processor stanag4609Processor,
            KlvHandlerFactory klvHandlerFactory, KlvHandler defaultKlvHandler,
            StanagParserFactory stanagParserFactory, KlvProcessor klvProcessor) {

        notNull(inputTransformer, "The inputTransformer must be non-null");
        notNull(metacardTypes, "The metacardTypes must be non-null");
        notNull(stanag4609Processor, "The stanag4609Processor must be non-null");
        notNull(klvHandlerFactory, "The klvHandlerFactory must be non-null");
        notNull(defaultKlvHandler, "The defaultKlvHandler must be non-null");
        notNull(stanagParserFactory, "The stanagParserFactory must be non-null");
        notNull(klvProcessor, "The klvProcessor must be non-null");

        this.innerTransformer = inputTransformer;
        this.metacardTypes = metacardTypes;
        this.stanag4609Processor = stanag4609Processor;
        this.klvHandlerFactory = klvHandlerFactory;
        this.stanagParserFactory = stanagParserFactory;
        this.defaultKlvHandler = defaultKlvHandler;
        this.klvProcessor = klvProcessor;
    }

    public void setSubsampleCount(Integer subsampleCount) {
        this.subsampleCount = subsampleCount;
    }

    /**
     * @param distanceTolerance may be null, must be non-negative
     */
    public void setDistanceTolerance(Double distanceTolerance) {
        inclusiveBetween(0,
                Double.MAX_VALUE,
                distanceTolerance,
                "distanceTolerance must be non-negative");
        klvProcessor.accept(new SetDistanceToleranceVisitor(distanceTolerance));
    }

    @Override
    public Metacard transform(InputStream inputStream)
            throws IOException, CatalogTransformerException {
        return transform(inputStream, null);
    }

    @Override
    public Metacard transform(InputStream inputStream, final String id)
            throws IOException, CatalogTransformerException {

        LOGGER.info("processing video input for id = {}", id);

        try (FileBackedOutputStream fileBackedOutputStream = new FileBackedOutputStream(
                FILE_THRESHOLD)) {

            populateFileBackedOutputStream(inputStream, fileBackedOutputStream);

            MetacardImpl metacard = extractInnerTransformerMetadata(id, fileBackedOutputStream);

            extractStanag4609Metadata(metacard, fileBackedOutputStream);

            return metacard;
        }

    }

    private void populateFileBackedOutputStream(InputStream inputStream,
            FileBackedOutputStream fbos) throws CatalogTransformerException {
        try {
            int c = IOUtils.copy(inputStream, fbos);
            LOGGER.debug("copied {} bytes from input stream to file backed output stream", c);
        } catch (IOException e) {
            throw new CatalogTransformerException("Could not copy bytes of content message.", e);
        }
    }

    /**
     * Call the inner transformer with the content data and return a metacard based on
     * {@link #metacardTypes} that is populated by the inner transformer and with the
     * content type set to {@link #CONTENT_TYPE}.
     *
     * @param id
     * @param fileBackedOutputStream
     * @return metacard
     * @throws IOException
     * @throws CatalogTransformerException
     */
    private MetacardImpl extractInnerTransformerMetadata(String id,
            FileBackedOutputStream fileBackedOutputStream)
            throws IOException, CatalogTransformerException {

        try (InputStream inputStream = fileBackedOutputStream.asByteSource()
                .openStream()) {

            MetacardType metacardType = metacardTypes.stream()
                    .findFirst()
                    .orElseThrow(() -> new CatalogTransformerException(
                            "no matching metacard type found! id = " + id));

            Metacard innerMetacard = innerTransformer.transform(inputStream, id);

            MetacardImpl metacard = new MetacardImpl(innerMetacard, metacardType);

            metacard.setContentTypeName(CONTENT_TYPE);

            return metacard;
        }
    }

    private void extractStanag4609Metadata(MetacardImpl metacard, FileBackedOutputStream fbos)
            throws CatalogTransformerException {

        Stanag4609Parser stanag4609Parser = stanagParserFactory.createParser(fbos.asByteSource());

        Map<Integer, List<DecodedKLVMetadataPacket>> decodedMetadata;
        try {
            decodedMetadata = stanag4609Parser.parse();
        } catch (Stanag4609ParseException e) {
            throw new CatalogTransformerException("failed to extract STANAG 4609 metadata", e);
        }

        Map<String, KlvHandler> handlers = klvHandlerFactory.createStanag4609Handlers();

        stanag4609Processor.handle(handlers, defaultKlvHandler, decodedMetadata);

        KlvProcessor.Configuration klvProcessConfiguration = new KlvProcessor.Configuration();
        klvProcessConfiguration.set(KlvProcessor.Configuration.SUBSAMPLE_COUNT, subsampleCount);

        klvProcessor.process(handlers, metacard, klvProcessConfiguration);

    }

}
