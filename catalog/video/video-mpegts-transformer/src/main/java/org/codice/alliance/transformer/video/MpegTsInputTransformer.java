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

import static org.apache.commons.lang3.Validate.inclusiveBetween;
import static org.apache.commons.lang3.Validate.notNull;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.types.Core;
import ddf.catalog.data.types.Media;
import ddf.catalog.data.types.constants.core.DataType;
import ddf.catalog.transform.CatalogTransformerException;
import ddf.catalog.transform.InputTransformer;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.codice.alliance.libs.klv.AttributeNameConstants;
import org.codice.alliance.libs.klv.BaseKlvProcessorVisitor;
import org.codice.alliance.libs.klv.KlvHandler;
import org.codice.alliance.libs.klv.KlvHandlerFactory;
import org.codice.alliance.libs.klv.KlvProcessor;
import org.codice.alliance.libs.klv.SecurityClassificationKlvProcessor;
import org.codice.alliance.libs.klv.Stanag4609ParseException;
import org.codice.alliance.libs.klv.Stanag4609Parser;
import org.codice.alliance.libs.klv.Stanag4609Processor;
import org.codice.alliance.libs.klv.StanagParserFactory;
import org.codice.alliance.libs.mpegts.MpegStreamType;
import org.codice.alliance.libs.mpegts.PESPacket;
import org.codice.alliance.libs.mpegts.TSStream;
import org.codice.alliance.libs.stanag4609.DecodedKLVMetadataPacket;
import org.codice.ddf.platform.util.TemporaryFileBackedOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MpegTsInputTransformer implements InputTransformer {

  public static final String CONTENT_TYPE = "video/mp2t";

  public static final String DATA_TYPE = DataType.MOVING_IMAGE.toString();

  private static final Logger LOGGER = LoggerFactory.getLogger(MpegTsInputTransformer.class);

  private static final Integer DEFAULT_SUBSAMPLE_COUNT = 50;

  private static final String CLASSIFICATION_MUST_BE_NON_NULL = "classification must be non-null";

  private final InputTransformer innerTransformer;

  private final List<MetacardType> metacardTypes;

  private final Stanag4609Processor stanag4609Processor;

  private final KlvHandlerFactory klvHandlerFactory;

  private final StanagParserFactory stanagParserFactory;

  private final KlvProcessor klvProcessor;

  /**
   * The default handler is used to handle metadata elements that do not have a dedicated handler.
   * This handler simply logs a message indicating that the metadata element is unhandled.
   */
  private final KlvHandler defaultKlvHandler;

  private Integer subsampleCount = DEFAULT_SUBSAMPLE_COUNT;

  private Function<MpegStreamType, String> streamTypeToString = MpegStreamType::toString;

  private Double distanceTolerance;

  /**
   * @param inputTransformer inner input transformer (must be non-null)
   * @param metacardTypes list of usable metacard types (must be non-null)
   * @param stanag4609Processor must be non-null
   * @param klvHandlerFactory must be non-null
   * @param defaultKlvHandler must be non-null
   * @param stanagParserFactory must be non-null
   * @param klvProcessor processors to transfer klv to metacard (must be non-null)
   * @param distanceTolerance must be non-null
   */
  public MpegTsInputTransformer(
      InputTransformer inputTransformer,
      List<MetacardType> metacardTypes,
      Stanag4609Processor stanag4609Processor,
      KlvHandlerFactory klvHandlerFactory,
      KlvHandler defaultKlvHandler,
      StanagParserFactory stanagParserFactory,
      KlvProcessor klvProcessor,
      Double distanceTolerance) {

    notNull(inputTransformer, "The inputTransformer must be non-null");
    notNull(metacardTypes, "The metacardTypes must be non-null");
    notNull(stanag4609Processor, "The stanag4609Processor must be non-null");
    notNull(klvHandlerFactory, "The klvHandlerFactory must be non-null");
    notNull(defaultKlvHandler, "The defaultKlvHandler must be non-null");
    notNull(stanagParserFactory, "The stanagParserFactory must be non-null");
    notNull(klvProcessor, "The klvProcessor must be non-null");
    notNull(distanceTolerance, "The distanceTolerance must be non-null");

    this.innerTransformer = inputTransformer;
    this.metacardTypes = metacardTypes;
    this.stanag4609Processor = stanag4609Processor;
    this.klvHandlerFactory = klvHandlerFactory;
    this.stanagParserFactory = stanagParserFactory;
    this.defaultKlvHandler = defaultKlvHandler;
    this.klvProcessor = klvProcessor;
    this.distanceTolerance = distanceTolerance;
  }

  @SuppressWarnings("unused")
  public void setSubsampleCount(Integer subsampleCount) {
    this.subsampleCount = subsampleCount;
  }

  /** @param distanceTolerance may be null, must be non-negative */
  public void setDistanceTolerance(Double distanceTolerance) {
    inclusiveBetween(
        0, Double.MAX_VALUE, distanceTolerance, "distanceTolerance must be non-negative");

    this.distanceTolerance = distanceTolerance;
  }

  public Double getDistanceTolerance() {
    return distanceTolerance;
  }

  @Override
  public Metacard transform(InputStream inputStream)
      throws IOException, CatalogTransformerException {
    return transform(inputStream, null);
  }

  public void setSecurityClassificationCode1(String classification) {
    notNull(classification, CLASSIFICATION_MUST_BE_NON_NULL);
    klvProcessor.accept(new SetSecurityClassificationString((short) 1, classification));
  }

  public void setSecurityClassificationCode2(String classification) {
    notNull(classification, CLASSIFICATION_MUST_BE_NON_NULL);
    klvProcessor.accept(new SetSecurityClassificationString((short) 2, classification));
  }

  public void setSecurityClassificationCode3(String classification) {
    notNull(classification, CLASSIFICATION_MUST_BE_NON_NULL);
    klvProcessor.accept(new SetSecurityClassificationString((short) 3, classification));
  }

  public void setSecurityClassificationCode4(String classification) {
    notNull(classification, CLASSIFICATION_MUST_BE_NON_NULL);
    klvProcessor.accept(new SetSecurityClassificationString((short) 4, classification));
  }

  public void setSecurityClassificationCode5(String classification) {
    notNull(classification, CLASSIFICATION_MUST_BE_NON_NULL);
    klvProcessor.accept(new SetSecurityClassificationString((short) 5, classification));
  }

  public void setSecurityClassificationDefault(String classification) {
    notNull(classification, CLASSIFICATION_MUST_BE_NON_NULL);
    klvProcessor.accept(
        new BaseKlvProcessorVisitor() {
          @Override
          public void visit(SecurityClassificationKlvProcessor securityClassificationKlvProcessor) {
            securityClassificationKlvProcessor.setDefaultSecurityClassification(classification);
          }
        });
  }

  @Override
  public Metacard transform(InputStream inputStream, final String id)
      throws IOException, CatalogTransformerException {

    try (TemporaryFileBackedOutputStream fileBackedOutputStream =
        new TemporaryFileBackedOutputStream()) {

      populateFileBackedOutputStream(inputStream, fileBackedOutputStream);

      MetacardImpl metacard = extractInnerTransformerMetadata(id, fileBackedOutputStream);

      extractStanag4609Metadata(metacard, fileBackedOutputStream);

      extractMediaEncodings(metacard, fileBackedOutputStream);

      metacard.setAttribute(Core.DATATYPE, DATA_TYPE);

      return metacard;
    }
  }

  private void extractMediaEncodings(Metacard metacard, TemporaryFileBackedOutputStream fbos)
      throws IOException {

    List<Serializable> serializables =
        TSStream.from(fbos.asByteSource())
            .map(PESPacket::getStreamType)
            .distinct()
            .map(streamTypeToString)
            .collect(Collectors.<Serializable>toList());

    metacard.setAttribute(new AttributeImpl(AttributeNameConstants.MEDIA_ENCODING, serializables));
  }

  private void populateFileBackedOutputStream(
      InputStream inputStream, TemporaryFileBackedOutputStream fbos)
      throws CatalogTransformerException {
    try {
      int c = IOUtils.copy(inputStream, fbos);
      LOGGER.debug("copied {} bytes from input stream to file backed output stream", c);
    } catch (IOException e) {
      throw new CatalogTransformerException("Could not copy bytes of content message.", e);
    }
  }

  /**
   * Call the inner transformer with the content data and return a metacard based on {@link
   * #metacardTypes} that is populated by the inner transformer and with the content type set to
   * {@link #CONTENT_TYPE}.
   *
   * @param id metacard identifier
   * @param fileBackedOutputStream used to provide a byte source
   * @return metacard
   * @throws IOException
   * @throws CatalogTransformerException
   */
  private MetacardImpl extractInnerTransformerMetadata(
      String id, TemporaryFileBackedOutputStream fileBackedOutputStream)
      throws IOException, CatalogTransformerException {

    try (InputStream inputStream = fileBackedOutputStream.asByteSource().openStream()) {

      MetacardType metacardType =
          metacardTypes.stream()
              .findFirst()
              .orElseThrow(
                  () ->
                      new CatalogTransformerException(
                          "no matching metacard type found! id = " + id));

      Metacard innerMetacard = innerTransformer.transform(inputStream, id);

      MetacardImpl metacard = new MetacardImpl(innerMetacard, metacardType);

      metacard.setAttribute(Media.TYPE, CONTENT_TYPE);
      metacard.setContentTypeName(CONTENT_TYPE);

      return metacard;
    }
  }

  private void extractStanag4609Metadata(
      MetacardImpl metacard, TemporaryFileBackedOutputStream fbos)
      throws IOException, CatalogTransformerException {

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
    klvProcessConfiguration.getGeometryOperatorContext().setDistanceTolerance(distanceTolerance);

    klvProcessor.process(handlers, metacard, klvProcessConfiguration);
  }
}
