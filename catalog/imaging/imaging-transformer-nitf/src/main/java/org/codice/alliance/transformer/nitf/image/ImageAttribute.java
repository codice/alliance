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

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.BasicTypes;
import ddf.catalog.data.impl.types.LocationAttributes;
import ddf.catalog.data.impl.types.MediaAttributes;
import ddf.catalog.data.types.Location;
import ddf.catalog.data.types.Media;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.codice.alliance.catalog.core.api.impl.types.IsrAttributes;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.transformer.nitf.ExtNitfUtility;
import org.codice.alliance.transformer.nitf.NitfAttributeConverters;
import org.codice.alliance.transformer.nitf.common.NitfAttribute;
import org.codice.alliance.transformer.nitf.common.NitfAttributeImpl;
import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.image.ImageSegment;
import org.codice.imaging.nitf.core.image.TargetId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** NitfAttributes to represent the properties of a ImageSegment. */
public class ImageAttribute extends NitfAttributeImpl<ImageSegment> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageAttribute.class);

  private static final List<NitfAttribute<ImageSegment>> ATTRIBUTES = new LinkedList<>();

  private static final String PREFIX = ExtNitfUtility.EXT_NITF_PREFIX + "image.";

  public static final String IMAGE_DATE_AND_TIME = PREFIX + "image-date-and-time";

  public static final String IMAGE_IDENTIFIER_2 = PREFIX + "image-identifier-2";

  public static final String IMAGE_SOURCE = PREFIX + "image-source";

  public static final String NUMBER_OF_SIGNIFICANT_ROWS_IN_IMAGE =
      PREFIX + "number-of-significant-rows-in-image";

  public static final String NUMBER_OF_SIGNIFICANT_COLUMNS_IN_IMAGE =
      PREFIX + "number-of-significant-columns-in-image";

  public static final String IMAGE_REPRESENTATION = PREFIX + "image-representation";

  public static final String IMAGE_CATEGORY = PREFIX + "image-category";

  public static final String IMAGE_COMPRESSION = PREFIX + "image-compression";

  public static final String FILE_PART_TYPE = PREFIX + "file-part-type";

  public static final String IMAGE_IDENTIFIER_1 = PREFIX + "image-identifier-1";

  public static final String IMAGE_SECURITY_CLASSIFICATION =
      PREFIX + "image-security-classification";

  public static final String IMAGE_CLASSIFICATION_SECURITY_SYSTEM =
      PREFIX + "image-classification-security-system";

  public static final String IMAGE_CODEWORDS = PREFIX + "image-codewords";

  public static final String IMAGE_CONTROL_AND_HANDLING = PREFIX + "image-control-and-handling";

  public static final String IMAGE_RELEASING_INSTRUCTIONS = PREFIX + "image-releasing-instructions";

  public static final String IMAGE_DECLASSIFICATION_TYPE = PREFIX + "image-declassification-type";

  public static final String IMAGE_DECLASSIFICATION_DATE = PREFIX + "image-declassification-date";

  public static final String IMAGE_DECLASSIFICATION_EXEMPTION =
      PREFIX + "image-declassification-exemption";

  public static final String IMAGE_DOWNGRADE = PREFIX + "image-downgrade";

  public static final String IMAGE_DOWNGRADE_DATE = PREFIX + "image-downgrade-date";

  public static final String IMAGE_CLASSIFICATION_TEXT = PREFIX + "image-classification-text";

  public static final String IMAGE_CLASSIFICATION_AUTHORITY_TYPE =
      PREFIX + "image-classification-authority-type";

  public static final String IMAGE_CLASSIFICATION_AUTHORITY =
      PREFIX + "image-classification-authority";

  public static final String IMAGE_CLASSIFICATION_REASON = PREFIX + "image-classification-reason";

  public static final String IMAGE_SECURITY_SOURCE_DATE = PREFIX + "image-security-source-date";

  public static final String IMAGE_SECURITY_CONTROL_NUMBER =
      PREFIX + "image-security-control-number";

  public static final String PIXEL_VALUE_TYPE = PREFIX + "pixel-value-type";

  public static final String ACTUAL_BITS_PER_PIXEL_PER_BAND =
      PREFIX + "actual-bits-per-pixel-per-band";

  public static final String PIXEL_JUSTIFICATION = PREFIX + "pixel-justification";

  public static final String IMAGE_COORDINATE_REPRESENTATION =
      PREFIX + "image-coordinate-representation";

  public static final String NUMBER_OF_IMAGE_COMMENTS = PREFIX + "number-of-image-comments";

  public static final String IMAGE_COMMENT_1 = PREFIX + "image-comment-1";

  public static final String IMAGE_COMMENT_2 = PREFIX + "image-comment-2";

  public static final String IMAGE_COMMENT_3 = PREFIX + "image-comment-3";

  public static final String IMAGE_COMMENT_4 = PREFIX + "image-comment-4";

  public static final String IMAGE_COMMENT_5 = PREFIX + "image-comment-5";

  public static final String IMAGE_COMMENT_6 = PREFIX + "image-comment-6";

  public static final String IMAGE_COMMENT_7 = PREFIX + "image-comment-7";

  public static final String IMAGE_COMMENT_8 = PREFIX + "image-comment-8";

  public static final String IMAGE_COMMENT_9 = PREFIX + "image-comment-9";

  public static final String NUMBER_OF_BANDS = PREFIX + "number-of-bands";

  public static final String IMAGE_MODE = PREFIX + "image-mode";

  public static final String NUMBER_OF_BLOCKS_PER_ROW = PREFIX + "number-of-blocks-per-row";

  public static final String NUMBER_OF_BLOCKS_PER_COLUMN = PREFIX + "number-of-blocks-per-column";

  public static final String NUMBER_OF_PIXELS_PER_BLOCK_HORIZONTAL =
      PREFIX + "number-of-pixels-per-block-horizontal";

  public static final String NUMBER_OF_PIXELS_PER_BLOCK_VERTICAL =
      PREFIX + "number-of-pixels-per-block-vertical";

  public static final String NUMBER_OF_BITS_PER_PIXEL = PREFIX + "number-of-bits-per-pixel";

  public static final String IMAGE_DISPLAY_LEVEL = PREFIX + "image-display-level";

  public static final String IMAGE_ATTACHMENT_LEVEL = PREFIX + "image-attachment-level";

  public static final String IMAGE_LOCATION = PREFIX + "image-location";

  public static final String IMAGE_MAGNIFICATION = PREFIX + "image-magnification";

  public static final String TARGET_IDENTIFIER = PREFIX + "target-identifier";

  /*
   * Normalized attributes. These taxonomy terms will be duplicated by `ext.nitf.image.*` when
   * appropriate.
   */

  public static final ImageAttribute MISSION_ID_ATTRIBUTE =
      new ImageAttribute(
          Isr.MISSION_ID,
          "IID2",
          ImageSegment::getImageIdentifier2,
          new IsrAttributes().getAttributeDescriptor(Isr.MISSION_ID),
          "");

  public static final ImageAttribute IMAGE_IDENTIFIER_2_ATTRIBUTE =
      new ImageAttribute(
          Isr.IMAGE_ID,
          "IID2",
          ImageSegment::getImageIdentifier2,
          new IsrAttributes().getAttributeDescriptor(Isr.IMAGE_ID),
          IMAGE_IDENTIFIER_2);

  public static final ImageAttribute IMAGE_SOURCE_ATTRIBUTE =
      new ImageAttribute(
          Isr.ORIGINAL_SOURCE,
          "ISORCE",
          ImageSegment::getImageSource,
          new IsrAttributes().getAttributeDescriptor(Isr.ORIGINAL_SOURCE),
          IMAGE_SOURCE);

  public static final ImageAttribute NUMBER_OF_SIGNIFICANT_ROWS_IN_IMAGE_ATTRIBUTE =
      new ImageAttribute(
          Media.HEIGHT,
          "NROWS",
          ImageSegment::getNumberOfRows,
          new MediaAttributes().getAttributeDescriptor(Media.HEIGHT),
          NUMBER_OF_SIGNIFICANT_ROWS_IN_IMAGE);

  public static final ImageAttribute NUMBER_OF_SIGNIFICANT_COLUMNS_IN_IMAGE_ATTRIBUTE =
      new ImageAttribute(
          Media.WIDTH,
          "NCOLS",
          ImageSegment::getNumberOfColumns,
          new MediaAttributes().getAttributeDescriptor(Media.WIDTH),
          NUMBER_OF_SIGNIFICANT_COLUMNS_IN_IMAGE);

  public static final ImageAttribute IMAGE_REPRESENTATION_ATTRIBUTE =
      new ImageAttribute(
          Media.ENCODING,
          "IREP",
          segment -> segment.getImageRepresentation().name(),
          new MediaAttributes().getAttributeDescriptor(Media.ENCODING),
          IMAGE_REPRESENTATION);

  public static final ImageAttribute IMAGE_CATEGORY_ATTRIBUTE =
      new ImageAttribute(
          Isr.CATEGORY,
          "ICAT",
          segment -> segment.getImageCategory().name(),
          segment -> segment.getImageCategory().getTextEquivalent(),
          new IsrAttributes().getAttributeDescriptor(Isr.CATEGORY),
          IMAGE_CATEGORY);

  public static final ImageAttribute IMAGE_COMPRESSION_ATTRIBUTE =
      new ImageAttribute(
          Media.COMPRESSION,
          "IC",
          segment -> segment.getImageCompression().name(),
          new MediaAttributes().getAttributeDescriptor(Media.COMPRESSION),
          IMAGE_COMPRESSION);

  public static final ImageAttribute ISR_TARGET_IDENTIFIER_ATTRIBUTE =
      new ImageAttribute(
          Isr.TARGET_ID,
          "TGTID",
          imageSegment -> StringUtils.trimToNull(getTargetId(imageSegment)),
          new IsrAttributes().getAttributeDescriptor(Isr.TARGET_ID),
          "");

  public static final ImageAttribute TARGET_IDENTIFIER_COUNTRY_CODE_ATTRIBUTE =
      new ImageAttribute(
          Location.COUNTRY_CODE,
          "COUNTRY",
          imageSegment ->
              NitfAttributeConverters.fipsToStandardCountryCode(
                  getTargetIdCountryCode(imageSegment)),
          new LocationAttributes().getAttributeDescriptor(Location.COUNTRY_CODE),
          "");

  /*
   * Non-normalized attributes
   */

  public static final ImageAttribute FILE_PART_TYPE_ATTRIBUTE =
      new ImageAttribute(FILE_PART_TYPE, "IM", segment -> "IM", BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_DATE_AND_TIME_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_DATE_AND_TIME,
          "IDATIM",
          segment -> NitfAttributeConverters.nitfDate(segment.getImageDateTime()),
          BasicTypes.DATE_TYPE);

  public static final ImageAttribute IMAGE_IDENTIFIER_1_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_IDENTIFIER_1, "IID1", ImageSegment::getIdentifier, BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_SECURITY_CLASSIFICATION_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_SECURITY_CLASSIFICATION,
          "ISCLAS",
          segment -> segment.getSecurityMetadata().getSecurityClassification().name(),
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_CLASSIFICATION_SECURITY_SYSTEM_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_CLASSIFICATION_SECURITY_SYSTEM,
          "ISCLSY",
          segment -> segment.getSecurityMetadata().getSecurityClassificationSystem(),
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_CODEWORDS_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_CODEWORDS,
          "ISCODE",
          segment -> segment.getSecurityMetadata().getCodewords(),
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_CONTROL_AND_HANDLING_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_CONTROL_AND_HANDLING,
          "ISCTLH",
          segment -> segment.getSecurityMetadata().getControlAndHandling(),
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_RELEASING_INSTRUCTIONS_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_RELEASING_INSTRUCTIONS,
          "ISREL",
          segment -> segment.getSecurityMetadata().getReleaseInstructions(),
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_DECLASSIFICATION_TYPE_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_DECLASSIFICATION_TYPE,
          "ISDCTP",
          segment -> segment.getSecurityMetadata().getDeclassificationType(),
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_DECLASSIFICATION_DATE_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_DECLASSIFICATION_DATE,
          "ISDCDT",
          segment -> segment.getSecurityMetadata().getDeclassificationDate(),
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_DECLASSIFICATION_EXEMPTION_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_DECLASSIFICATION_EXEMPTION,
          "ISDCXM",
          segment -> segment.getSecurityMetadata().getDeclassificationExemption(),
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_DOWNGRADE_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_DOWNGRADE,
          "ISDG",
          segment -> segment.getSecurityMetadata().getDowngrade(),
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_DOWNGRADE_DATE_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_DOWNGRADE_DATE,
          "ISDGDT",
          segment -> segment.getSecurityMetadata().getDowngradeDate(),
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_CLASSIFICATION_TEXT_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_CLASSIFICATION_TEXT,
          "ISCLTX",
          segment -> segment.getSecurityMetadata().getClassificationText(),
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_CLASSIFICATION_AUTHORITY_TYPE_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_CLASSIFICATION_AUTHORITY_TYPE,
          "ISCATP",
          segment -> segment.getSecurityMetadata().getClassificationAuthorityType(),
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_CLASSIFICATION_AUTHORITY_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_CLASSIFICATION_AUTHORITY,
          "ISCAUT",
          segment -> segment.getSecurityMetadata().getClassificationAuthority(),
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_CLASSIFICATION_REASON_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_CLASSIFICATION_REASON,
          "ISCRSN",
          segment -> segment.getSecurityMetadata().getClassificationReason(),
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_SECURITY_SOURCE_DATE_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_SECURITY_SOURCE_DATE,
          "ISSRDT",
          segment -> segment.getSecurityMetadata().getSecuritySourceDate(),
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_SECURITY_CONTROL_NUMBER_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_SECURITY_CONTROL_NUMBER,
          "ISCTLN",
          segment -> segment.getSecurityMetadata().getSecurityControlNumber(),
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute PIXEL_VALUE_TYPE_ATTRIBUTE =
      new ImageAttribute(
          PIXEL_VALUE_TYPE,
          "PVTYPE",
          segment -> segment.getPixelValueType().name(),
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute ACTUAL_BITS_PER_PIXEL_PER_BAND_ATTRIBUTE =
      new ImageAttribute(
          ACTUAL_BITS_PER_PIXEL_PER_BAND,
          "ABPP",
          ImageSegment::getActualBitsPerPixelPerBand,
          BasicTypes.INTEGER_TYPE);

  public static final ImageAttribute PIXEL_JUSTIFICATION_ATTRIBUTE =
      new ImageAttribute(
          PIXEL_JUSTIFICATION,
          "PJUST",
          segment -> segment.getPixelJustification().name(),
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_COORDINATE_REPRESENTATION_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_COORDINATE_REPRESENTATION,
          "ICORDS",
          segment -> segment.getImageCoordinatesRepresentation().name(),
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute NUMBER_OF_IMAGE_COMMENTS_ATTRIBUTE =
      new ImageAttribute(
          NUMBER_OF_IMAGE_COMMENTS,
          "NICOM",
          segment -> segment.getImageComments().size(),
          BasicTypes.INTEGER_TYPE);

  public static final ImageAttribute IMAGE_COMMENT_1_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_COMMENT_1,
          "ICOM1",
          segment -> !segment.getImageComments().isEmpty() ? segment.getImageComments().get(0) : "",
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_COMMENT_2_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_COMMENT_2,
          "ICOM2",
          segment -> segment.getImageComments().size() > 1 ? segment.getImageComments().get(1) : "",
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_COMMENT_3_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_COMMENT_3,
          "ICOM3",
          segment -> segment.getImageComments().size() > 2 ? segment.getImageComments().get(2) : "",
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_COMMENT_4_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_COMMENT_4,
          "ICOM4",
          segment -> segment.getImageComments().size() > 3 ? segment.getImageComments().get(3) : "",
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_COMMENT_5_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_COMMENT_5,
          "ICOM5",
          segment -> segment.getImageComments().size() > 4 ? segment.getImageComments().get(4) : "",
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_COMMENT_6_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_COMMENT_6,
          "ICOM6",
          segment -> segment.getImageComments().size() > 5 ? segment.getImageComments().get(5) : "",
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_COMMENT_7_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_COMMENT_7,
          "ICOM7",
          segment -> segment.getImageComments().size() > 6 ? segment.getImageComments().get(6) : "",
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_COMMENT_8_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_COMMENT_8,
          "ICOM8",
          segment -> segment.getImageComments().size() > 7 ? segment.getImageComments().get(7) : "",
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_COMMENT_9_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_COMMENT_9,
          "ICOM9",
          segment -> segment.getImageComments().size() > 8 ? segment.getImageComments().get(8) : "",
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute NUMBER_OF_BANDS_ATTRIBUTE =
      new ImageAttribute(
          NUMBER_OF_BANDS, "NBANDS", ImageSegment::getNumBands, BasicTypes.INTEGER_TYPE);

  public static final ImageAttribute IMAGE_MODE_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_MODE, "IMODE", segment -> segment.getImageMode().name(), BasicTypes.STRING_TYPE);

  public static final ImageAttribute NUMBER_OF_BLOCKS_PER_ROW_ATTRIBUTE =
      new ImageAttribute(
          NUMBER_OF_BLOCKS_PER_ROW,
          "NBPR",
          ImageSegment::getNumberOfBlocksPerRow,
          BasicTypes.INTEGER_TYPE);

  public static final ImageAttribute NUMBER_OF_BLOCKS_PER_COLUMN_ATTRIBUTE =
      new ImageAttribute(
          NUMBER_OF_BLOCKS_PER_COLUMN,
          "NBPC",
          ImageSegment::getNumberOfBlocksPerColumn,
          BasicTypes.INTEGER_TYPE);

  public static final ImageAttribute NUMBER_OF_PIXELS_PER_BLOCK_HORIZONTAL_ATTRIBUTE =
      new ImageAttribute(
          NUMBER_OF_PIXELS_PER_BLOCK_HORIZONTAL,
          "NPPBH",
          ImageSegment::getNumberOfPixelsPerBlockHorizontal,
          BasicTypes.INTEGER_TYPE);

  public static final ImageAttribute NUMBER_OF_PIXELS_PER_BLOCK_VERTICAL_ATTRIBUTE =
      new ImageAttribute(
          NUMBER_OF_PIXELS_PER_BLOCK_VERTICAL,
          "NPPBV",
          ImageSegment::getNumberOfPixelsPerBlockVertical,
          BasicTypes.INTEGER_TYPE);

  public static final ImageAttribute NUMBER_OF_BITS_PER_PIXEL_ATTRIBUTE =
      new ImageAttribute(
          NUMBER_OF_BITS_PER_PIXEL,
          "NBPP",
          ImageSegment::getNumberOfBitsPerPixelPerBand,
          BasicTypes.INTEGER_TYPE);

  public static final ImageAttribute IMAGE_DISPLAY_LEVEL_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_DISPLAY_LEVEL,
          "IDLVL",
          ImageSegment::getImageDisplayLevel,
          BasicTypes.INTEGER_TYPE);

  public static final ImageAttribute IMAGE_ATTACHMENT_LEVEL_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_ATTACHMENT_LEVEL,
          "IALVL",
          ImageSegment::getAttachmentLevel,
          BasicTypes.INTEGER_TYPE);

  public static final ImageAttribute IMAGE_LOCATION_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_LOCATION,
          "ILOC",
          segment -> segment.getImageLocationRow() + "," + segment.getImageLocationColumn(),
          BasicTypes.STRING_TYPE);

  public static final ImageAttribute IMAGE_MAGNIFICATION_ATTRIBUTE =
      new ImageAttribute(
          IMAGE_MAGNIFICATION,
          "IMAG",
          ImageSegment::getImageMagnificationAsDouble,
          BasicTypes.DOUBLE_TYPE);

  public static final ImageAttribute NITF_TARGET_IDENTIFIER_ATTRIBUTE =
      new ImageAttribute(
          TARGET_IDENTIFIER, "TGTID", ImageAttribute::getTargetId, BasicTypes.STRING_TYPE);

  private ImageAttribute(
      final String longName,
      final String shortName,
      final Function<ImageSegment, Serializable> accessorFunction,
      AttributeDescriptor attributeDescriptor,
      String extNitfName) {
    super(longName, shortName, accessorFunction, attributeDescriptor, extNitfName);
    ATTRIBUTES.add(this);
  }

  private ImageAttribute(
      final String longName,
      final String shortName,
      final Function<ImageSegment, Serializable> accessorFunction,
      final Function<ImageSegment, Serializable> extAccessorFunction,
      AttributeDescriptor attributeDescriptor,
      String extNitfName) {
    super(
        longName,
        shortName,
        accessorFunction,
        extAccessorFunction,
        attributeDescriptor,
        extNitfName);
    ATTRIBUTES.add(this);
  }

  private ImageAttribute(
      String longName,
      String shortName,
      Function<ImageSegment, Serializable> accessorFunction,
      AttributeType attributeType) {
    super(longName, shortName, accessorFunction, attributeType);
    ATTRIBUTES.add(this);
  }

  private static String getTargetId(ImageSegment imageSegment) {
    try {
      final TargetId targetId = imageSegment.getImageTargetId();
      if (targetId != null) {
        final String targetIdValue = targetId.textValue();
        if (StringUtils.isNotBlank(targetIdValue)) {
          return targetIdValue;
        }
      }
    } catch (NitfFormatException e) {
      LOGGER.debug(
          "Unable to get valid target id from image segment id={}", imageSegment.getIdentifier());
    }

    return null;
  }

  private static String getTargetIdCountryCode(ImageSegment imageSegment) {
    final TargetId imageTargetId = imageSegment.getImageTargetId();

    if (imageTargetId != null) {
      final String countryCode = imageTargetId.getCountryCode();

      if (StringUtils.isNotBlank(countryCode)) {
        return countryCode;
      }
    }

    return null;
  }

  public static List<NitfAttribute<ImageSegment>> getAttributes() {
    return Collections.unmodifiableList(ATTRIBUTES);
  }
}
