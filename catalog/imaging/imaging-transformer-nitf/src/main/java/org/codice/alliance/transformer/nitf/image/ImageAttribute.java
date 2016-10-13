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
package org.codice.alliance.transformer.nitf.image;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.codice.alliance.catalog.core.api.impl.types.IsrAttributes;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.transformer.nitf.common.NitfAttribute;
import org.codice.alliance.transformer.nitf.common.NitfAttributeImpl;
import org.codice.imaging.nitf.core.common.DateTime;
import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.image.ImageSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.BasicTypes;
import ddf.catalog.data.impl.types.DateTimeAttributes;
import ddf.catalog.data.impl.types.MediaAttributes;
import ddf.catalog.data.types.Media;

/**
 * NitfAttributes to represent the properties of a ImageSegment.
 */
public class ImageAttribute extends NitfAttributeImpl<ImageSegment> {

    private static final List<NitfAttribute<ImageSegment>> ATTRIBUTES = new LinkedList<>();

    private static final String ATTRIBUTE_NAME_PREFIX = "image.";

    /*
     * Normalized attributes. These taxonomy terms will be duplicated by `ext.nitf.image.*` when
     * appropriate
     */

    public static final ImageAttribute IMAGE_DATE_AND_TIME =
            new ImageAttribute(ddf.catalog.data.types.DateTime.START,
                    "IDATIM",
                    segment -> convertNitfDate(segment.getImageDateTime()),
                    new DateTimeAttributes().getAttributeDescriptor(ddf.catalog.data.types.DateTime.START),
                    "imageDateAndTime",
                    ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_IDENTIFIER_2 = new ImageAttribute(Isr.IMAGE_ID,
            "IID2",
            ImageSegment::getImageIdentifier2,
            new IsrAttributes().getAttributeDescriptor(Isr.IMAGE_ID),
            "imageIdentifier2",
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_SOURCE = new ImageAttribute(Isr.ORIGINAL_SOURCE,
            "ISORCE",
            ImageSegment::getImageSource,
            new IsrAttributes().getAttributeDescriptor(Isr.ORIGINAL_SOURCE),
            "imageSource",
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute NUMBER_OF_SIGNIFICANT_ROWS_IN_IMAGE = new ImageAttribute(
            Media.HEIGHT,
            "NROWS",
            ImageSegment::getNumberOfRows,
            new MediaAttributes().getAttributeDescriptor(Media.HEIGHT),
            "numberOfSignificantRowsInImage",
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute NUMBER_OF_SIGNIFICANT_COLUMNS_IN_IMAGE = new ImageAttribute(
            Media.WIDTH,
            "NCOLS",
            ImageSegment::getNumberOfColumns,
            new MediaAttributes().getAttributeDescriptor(Media.WIDTH),
            "numberOfSignificantColumnsInImage",
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_REPRESENTATION = new ImageAttribute(Media.ENCODING,
            "IREP",
            segment -> segment.getImageRepresentation()
                    .name(),
            new MediaAttributes().getAttributeDescriptor(Media.ENCODING),
            "imageRepresentation",
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_CATEGORY = new ImageAttribute(Isr.CATEGORY,
            "ICAT",
            segment -> segment.getImageCategory()
                    .name(),
            new IsrAttributes().getAttributeDescriptor(Isr.CATEGORY),
            "imageCategory",
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_COMPRESSION = new ImageAttribute(Media.COMPRESSION,
            "IC",
            segment -> segment.getImageCompression()
                    .name(),
            new MediaAttributes().getAttributeDescriptor(Media.COMPRESSION),
            "imageCompression",
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute FILE_PART_TYPE = new ImageAttribute("filePartType",
            "IM",
            segment -> "IM",
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    /*
     * Non normalized attributes
     */

    public static final ImageAttribute IMAGE_IDENTIFIER_1 = new ImageAttribute("imageIdentifier1",
            "IID1",
            ImageSegment::getIdentifier,
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_SECURITY_CLASSIFICATION = new ImageAttribute(
            "imageSecurityClassification",
            "ISCLAS",
            segment -> segment.getSecurityMetadata()
                    .getSecurityClassification()
                    .name(),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_CLASSIFICATION_SECURITY_SYSTEM = new ImageAttribute(
            "imageClassificationSecuritySystem",
            "ISCLSY",
            segment -> segment.getSecurityMetadata()
                    .getSecurityClassificationSystem(),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_CODEWORDS = new ImageAttribute("imageCodewords",
            "ISCODE",
            segment -> segment.getSecurityMetadata()
                    .getCodewords(),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_CONTROL_AND_HANDLING = new ImageAttribute(
            "imageControlandHandling",
            "ISCTLH",
            segment -> segment.getSecurityMetadata()
                    .getControlAndHandling(),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_RELEASING_INSTRUCTIONS = new ImageAttribute(
            "imageReleasingInstructions",
            "ISREL",
            segment -> segment.getSecurityMetadata()
                    .getReleaseInstructions(),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_DECLASSIFICATION_TYPE = new ImageAttribute(
            "imageDeclassificationType",
            "ISDCTP",
            segment -> segment.getSecurityMetadata()
                    .getDeclassificationType(),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_DECLASSIFICATION_DATE = new ImageAttribute(
            "imageDeclassificationDate",
            "ISDCDT",
            segment -> segment.getSecurityMetadata()
                    .getDeclassificationDate(),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_DECLASSIFICATION_EXEMPTION = new ImageAttribute(
            "imageDeclassificationExemption",
            "ISDCXM",
            segment -> segment.getSecurityMetadata()
                    .getDeclassificationExemption(),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_DOWNGRADE = new ImageAttribute("imageDowngrade",
            "ISDG",
            segment -> segment.getSecurityMetadata()
                    .getDowngrade(),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_DOWNGRADE_DATE = new ImageAttribute(
            "imageDowngradeDate",
            "ISDGDT",
            segment -> segment.getSecurityMetadata()
                    .getDowngradeDate(),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_CLASSIFICATION_TEXT = new ImageAttribute(
            "imageClassificationText",
            "ISCLTX",
            segment -> segment.getSecurityMetadata()
                    .getClassificationText(),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_CLASSIFICATION_AUTHORITY_TYPE = new ImageAttribute(
            "imageClassificationAuthorityType",
            "ISCATP",
            segment -> segment.getSecurityMetadata()
                    .getClassificationAuthorityType(),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_CLASSIFICATION_AUTHORITY = new ImageAttribute(
            "imageClassificationAuthority",
            "ISCAUT",
            segment -> segment.getSecurityMetadata()
                    .getClassificationAuthority(),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_CLASSIFICATION_REASON = new ImageAttribute(
            "imageClassificationReason",
            "ISCRSN",
            segment -> segment.getSecurityMetadata()
                    .getClassificationReason(),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_SECURITY_SOURCE_DATE = new ImageAttribute(
            "imageSecuritySourceDate",
            "ISSRDT",
            segment -> segment.getSecurityMetadata()
                    .getSecuritySourceDate(),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_SECURITY_CONTROL_NUMBER = new ImageAttribute(
            "imageSecurityControlNumber",
            "ISCTLN",
            segment -> segment.getSecurityMetadata()
                    .getSecurityControlNumber(),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute PIXEL_VALUE_TYPE = new ImageAttribute("pixelValueType",
            "PVTYPE",
            segment -> segment.getPixelValueType()
                    .name(),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute ACTUAL_BITS_PER_PIXEL_PER_BAND = new ImageAttribute(
            "actualBitsPerPixelPerBand",
            "ABPP",
            ImageSegment::getActualBitsPerPixelPerBand,
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute PIXEL_JUSTIFICATION =
            new ImageAttribute("pixelJustification",
                    "PJUST",
                    segment -> segment.getPixelJustification()
                            .name(),
                    BasicTypes.STRING_TYPE,
                    ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_COORDINATE_REPRESENTATION = new ImageAttribute(
            "imageCoordinateRepresentation",
            "ICORDS",
            segment -> segment.getImageCoordinatesRepresentation()
                    .name(),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute NUMBER_OF_IMAGE_COMMENTS = new ImageAttribute(
            "numberOfImageComments",
            "NICOM",
            segment -> segment.getImageComments()
                    .size(),
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_COMMENT_1 = new ImageAttribute("imageComment1",
            "ICOM1",
            segment -> segment.getImageComments()
                    .size() > 0 ?
                    segment.getImageComments()
                            .get(0) :
                    "",
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_COMMENT_2 = new ImageAttribute("imageComment2",
            "ICOM2",
            segment -> segment.getImageComments()
                    .size() > 1 ?
                    segment.getImageComments()
                            .get(1) :
                    "",
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_COMMENT_3 = new ImageAttribute("imageComment3",
            "ICOM3",
            segment -> segment.getImageComments()
                    .size() > 2 ?
                    segment.getImageComments()
                            .get(2) :
                    "",
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute NUMBER_OF_BANDS = new ImageAttribute("numberOfBands",
            "NBANDS",
            ImageSegment::getNumBands,
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_MODE = new ImageAttribute("imageMode",
            "IMODE",
            segment -> segment.getImageMode()
                    .name(),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute NUMBER_OF_BLOCKS_PER_ROW = new ImageAttribute(
            "numberOfBlocksPerRow",
            "NBPR",
            ImageSegment::getNumberOfBlocksPerRow,
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute NUMBER_OF_BLOCKS_PER_COLUMN = new ImageAttribute(
            "numberOfBlocksPerColumn",
            "NBPC",
            ImageSegment::getNumberOfBlocksPerColumn,
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute NUMBER_OF_PIXELS_PER_BLOCK_HORIZONTAL = new ImageAttribute(
            "numberOfPixelsPerBlockHorizontal",
            "NPPBH",
            ImageSegment::getNumberOfPixelsPerBlockHorizontal,
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute NUMBER_OF_PIXELS_PER_BLOCK_VERTICAL = new ImageAttribute(
            "numberOfPixelsPerBlockVertical",
            "NPPBV",
            ImageSegment::getNumberOfPixelsPerBlockVertical,
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute NUMBER_OF_BITS_PER_PIXEL = new ImageAttribute(
            "numberOfBitsPerPixel",
            "NBPP",
            ImageSegment::getNumberOfBitsPerPixelPerBand,
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_DISPLAY_LEVEL = new ImageAttribute("imageDisplayLevel",
            "IDLVL",
            ImageSegment::getImageDisplayLevel,
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_ATTACHMENT_LEVEL = new ImageAttribute(
            "imageAttachmentLevel",
            "IALVL",
            ImageSegment::getAttachmentLevel,
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_LOCATION = new ImageAttribute("imageLocation",
            "ILOC",
            segment -> segment.getImageLocationRow() + "," + segment.getImageLocationColumn(),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final ImageAttribute IMAGE_MAGNIFICATION =
            new ImageAttribute("imageMagnification",
                    "IMAG",
                    segment -> segment.getImageMagnification()
                            .trim(),
                    BasicTypes.STRING_TYPE,
                    ATTRIBUTE_NAME_PREFIX);

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageAttribute.class);

    public static final ImageAttribute TARGET_IDENTIFIER = new ImageAttribute(Isr.TARGET_ID,
            "TGTID",
            ImageAttribute::getTargetId,
            new IsrAttributes().getAttributeDescriptor(Isr.TARGET_ID),
            "targetIdentifier",
            ATTRIBUTE_NAME_PREFIX);

    private ImageAttribute(final String longName, final String shortName,
            final Function<ImageSegment, Serializable> accessorFunction,
            AttributeDescriptor attributeDescriptor, String extNitfName, String prefix) {
        super(longName, shortName, accessorFunction, attributeDescriptor, extNitfName, prefix);
        ATTRIBUTES.add(this);
    }

    private ImageAttribute(String longName, String shortName,
            Function<ImageSegment, Serializable> accessorFunction, AttributeType attributeType,
            String prefix) {
        super(longName, shortName, accessorFunction, attributeType, prefix);
        ATTRIBUTES.add(this);
    }

    private static String getTargetId(ImageSegment imageSegment) {
        if (imageSegment == null || imageSegment.getImageTargetId() == null) {
            return null;
        }

        try {
            return imageSegment.getImageTargetId()
                    .textValue()
                    .trim();
        } catch (NitfFormatException nfe) {
            LOGGER.debug(nfe.getMessage(), nfe);
        }

        return null;
    }

    private static Date convertNitfDate(DateTime nitfDateTime) {
        if (nitfDateTime == null || nitfDateTime.getZonedDateTime() == null) {
            return null;
        }

        ZonedDateTime zonedDateTime = nitfDateTime.getZonedDateTime();
        Instant instant = zonedDateTime.toInstant();

        if (instant != null) {
            return Date.from(instant);
        }

        return null;
    }

    public static List<NitfAttribute<ImageSegment>> getAttributes() {
        return Collections.unmodifiableList(ATTRIBUTES);
    }
}
