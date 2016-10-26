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
import java.util.Date;
import java.util.function.Function;

import org.codice.alliance.transformer.nitf.common.NitfAttribute;
import org.codice.imaging.nitf.core.common.DateTime;
import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.image.ImageSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;

/**
 * NitfAttributes to represent the properties of a ImageSegment.
 */
public enum ImageAttribute implements NitfAttribute<ImageSegment> {
    FILE_PART_TYPE("filePartType", "IM", segment -> "IM"),
    IMAGE_IDENTIFIER_1("imageIdentifier1", "IID1", ImageSegment::getIdentifier),
    IMAGE_DATE_AND_TIME("imageDateAndTime",
            "IDATIM",
            segment -> convertNitfDate(segment.getImageDateTime()),
            BasicTypes.DATE_TYPE),
    TARGET_IDENTIFIER("targetIdentifier", "TGTID", segment -> getTargetId(segment)),
    IMAGE_IDENTIFIER_2("imageIdentifier2", "IID2", ImageSegment::getImageIdentifier2),
    IMAGE_SECURITY_CLASSIFICATION("imageSecurityClassification",
            "ISCLAS",
            segment -> segment.getSecurityMetadata()
                    .getSecurityClassification()
                    .name()),
    IMAGE_CLASSIFICATION_SECURITY_SYSTEM("imageClassificationSecuritySystem",
            "ISCLSY",
            segment -> segment.getSecurityMetadata()
                    .getSecurityClassificationSystem()),
    IMAGE_CODEWORDS("imageCodewords",
            "ISCODE",
            segment -> segment.getSecurityMetadata()
                    .getCodewords()),
    IMAGE_CONTROL_AND_HANDLING("imageControlandHandling",
            "ISCTLH",
            segment -> segment.getSecurityMetadata()
                    .getControlAndHandling()),
    IMAGE_RELEASING_INSTRUCTIONS("imageReleasingInstructions",
            "ISREL",
            segment -> segment.getSecurityMetadata()
                    .getReleaseInstructions()),
    IMAGE_DECLASSIFICATION_TYPE("imageDeclassificationType",
            "ISDCTP",
            segment -> segment.getSecurityMetadata()
                    .getDeclassificationType()),
    IMAGE_DECLASSIFICATION_DATE("imageDeclassificationDate",
            "ISDCDT",
            segment -> segment.getSecurityMetadata()
                    .getDeclassificationDate()),
    IMAGE_DECLASSIFICATION_EXEMPTION("imageDeclassificationExemption",
            "ISDCXM",
            segment -> segment.getSecurityMetadata()
                    .getDeclassificationExemption()),
    IMAGE_DOWNGRADE("imageDowngrade",
            "ISDG",
            segment -> segment.getSecurityMetadata()
                    .getDowngrade()),
    IMAGE_DOWNGRADE_DATE("imageDowngradeDate",
            "ISDGDT",
            segment -> segment.getSecurityMetadata()
                    .getDowngradeDate()),
    IMAGE_CLASSIFICATION_TEXT("imageClassificationText",
            "ISCLTX",
            segment -> segment.getSecurityMetadata()
                    .getClassificationText()),
    IMAGE_CLASSIFICATION_AUTHORITY_TYPE("imageClassificationAuthorityType",
            "ISCATP",
            segment -> segment.getSecurityMetadata()
                    .getClassificationAuthorityType()),
    IMAGE_CLASSIFICATION_AUTHORITY("imageClassificationAuthority",
            "ISCAUT",
            segment -> segment.getSecurityMetadata()
                    .getClassificationAuthority()),
    IMAGE_CLASSIFICATION_REASON("imageClassificationReason",
            "ISCRSN",
            segment -> segment.getSecurityMetadata()
                    .getClassificationReason()),
    IMAGE_SECURITY_SOURCE_DATE("imageSecuritySourceDate",
            "ISSRDT",
            segment -> segment.getSecurityMetadata()
                    .getSecuritySourceDate()),
    IMAGE_SECURITY_CONTROL_NUMBER("imageSecurityControlNumber",
            "ISCTLN",
            segment -> segment.getSecurityMetadata()
                    .getSecurityControlNumber()),
    IMAGE_SOURCE("imageSource", "ISORCE", ImageSegment::getImageSource),
    NUMBER_OF_SIGNIFICANT_ROWS_IN_IMAGE("numberOfSignificantRowsInImage",
            "NROWS",
            ImageSegment::getNumberOfRows,
            BasicTypes.LONG_TYPE),
    NUMBER_OF_SIGNIFICANT_COLUMNS_IN_IMAGE("numberOfSignificantColumnsInImage",
            "NCOLS",
            ImageSegment::getNumberOfColumns,
            BasicTypes.LONG_TYPE),
    PIXEL_VALUE_TYPE("pixelValueType",
            "PVTYPE",
            segment -> segment.getPixelValueType()
                    .name()),
    IMAGE_REPRESENTATION("imageRepresentation",
            "IREP",
            segment -> segment.getImageRepresentation()
                    .name()),
    IMAGE_CATEGORY("imageCategory",
            "ICAT",
            segment -> segment.getImageCategory()
                    .name()),
    ACTUAL_BITS_PER_PIXEL_PER_BAND("actualBitsPerPixelPerBand",
            "ABPP",
            ImageSegment::getActualBitsPerPixelPerBand,
            BasicTypes.INTEGER_TYPE),
    PIXEL_JUSTIFICATION("pixelJustification",
            "PJUST",
            segment -> segment.getPixelJustification()
                    .name()),
    IMAGE_COORDINATE_REPRESENTATION("imageCoordinateRepresentation",
            "ICORDS",
            segment -> segment.getImageCoordinatesRepresentation()
                    .name()),
    NUMBER_OF_IMAGE_COMMENTS("numberOfImageComments",
            "NICOM",
            segment -> segment.getImageComments()
                    .size(),
            BasicTypes.INTEGER_TYPE),
    IMAGE_COMMENT_1("imageComment1",
            "ICOM1",
            segment -> segment.getImageComments()
                    .size() > 0 ?
                    segment.getImageComments()
                            .get(0) :
                    ""),
    IMAGE_COMMENT_2("imageComment2",
            "ICOM2",
            segment -> segment.getImageComments()
                    .size() > 1 ?
                    segment.getImageComments()
                            .get(1) :
                    ""),
    IMAGE_COMMENT_3("imageComment3",
            "ICOM3",
            segment -> segment.getImageComments()
                    .size() > 2 ?
                    segment.getImageComments()
                            .get(2) :
                    ""),
    IMAGE_COMPRESSION("imageCompression",
            "IC",
            segment -> segment.getImageCompression()
                    .name()),
    NUMBER_OF_BANDS("numberOfBands", "NBANDS", ImageSegment::getNumBands, BasicTypes.INTEGER_TYPE),
    IMAGE_MODE("imageMode",
            "IMODE",
            segment -> segment.getImageMode()
                    .name()),
    NUMBER_OF_BLOCKS_PER_ROW("numberOfBlocksperRow",
            "NBPR",
            ImageSegment::getNumberOfBlocksPerRow,
            BasicTypes.INTEGER_TYPE),
    NUMBER_OF_BLOCKS_PER_COLUMN("numberOfBlocksperColumn",
            "NBPC",
            ImageSegment::getNumberOfBlocksPerColumn,
            BasicTypes.INTEGER_TYPE),
    NUMBER_OF_PIXELS_PER_BLOCK_HORIZONTAL("numberOfPixelsPerBlockHorizontal",
            "NPPBH",
            ImageSegment::getNumberOfPixelsPerBlockHorizontal,
            BasicTypes.INTEGER_TYPE),
    NUMBER_OF_PIXELS_PER_BLOCK_VERTICAL("numberOfPixelsPerBlockVertical",
            "NPPBV",
            ImageSegment::getNumberOfPixelsPerBlockVertical,
            BasicTypes.INTEGER_TYPE),
    NUMBER_OF_BITS_PER_PIXEL("numberOfBitsPerPixel",
            "NBPP",
            ImageSegment::getNumberOfBitsPerPixelPerBand,
            BasicTypes.INTEGER_TYPE),
    IMAGE_DISPLAY_LEVEL("imageDisplayLevel",
            "IDLVL",
            ImageSegment::getImageDisplayLevel,
            BasicTypes.INTEGER_TYPE),
    IMAGE_ATTACHMENT_LEVEL("imageAttachmentLevel",
            "IALVL",
            ImageSegment::getAttachmentLevel,
            BasicTypes.INTEGER_TYPE),
    IMAGE_LOCATION("imageLocation",
            "ILOC",
            segment -> segment.getImageLocationRow() + "," + segment.getImageLocationColumn()),
    IMAGE_MAGNIFICATION("imageMagnification",
            "IMAG",
            ImageSegment::getImageMagnificationAsDouble,
            BasicTypes.DOUBLE_TYPE);

    public static final String ATTRIBUTE_NAME_PREFIX = "nitf.image.";

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageAttribute.class);

    private String shortName;

    private String longName;

    private Function<ImageSegment, Serializable> accessorFunction;

    private AttributeDescriptor attributeDescriptor;

    ImageAttribute(final String lName, final String sName,
            final Function<ImageSegment, Serializable> accessor) {
        this(lName, sName, accessor, BasicTypes.STRING_TYPE);
    }

    ImageAttribute(final String lName, final String sName,
            final Function<ImageSegment, Serializable> accessor, AttributeType attributeType) {
        this.accessorFunction = accessor;
        this.shortName = sName;
        this.longName = lName;
        this.attributeDescriptor = new AttributeDescriptorImpl(ATTRIBUTE_NAME_PREFIX + longName,
                true,
                true,
                false,
                true,
                attributeType);
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
            LOGGER.warn(nfe.getMessage(), nfe);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getShortName() {
        return this.shortName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getLongName() {
        return this.longName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Function<ImageSegment, Serializable> getAccessorFunction() {
        return accessorFunction;
    }

    /**
     * Equivalent to getLongName()
     *
     * @return the attribute's long name.
     */
    @Override
    public String toString() {
        return getLongName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeDescriptor getAttributeDescriptor() {
        return this.attributeDescriptor;
    }
}
