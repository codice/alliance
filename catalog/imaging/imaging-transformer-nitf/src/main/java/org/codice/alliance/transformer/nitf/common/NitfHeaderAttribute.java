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
package org.codice.alliance.transformer.nitf.common;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.function.Function;

import org.codice.imaging.nitf.core.common.DateTime;
import org.codice.imaging.nitf.core.header.NitfHeader;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;

/**
 * NitfAttributes to represent the properties of a NitfFileHeader.
 */
public enum NitfHeaderAttribute implements NitfAttribute<NitfHeader> {
    FILE_PROFILE_NAME("fileProfileName",
            "FHDR",
            header -> header.getFileType()
                    .name()),
    FILE_VERSION("fileVersion",
            "FVER",
            header -> header.getFileType()
                    .name()),
    COMPLEXITY_LEVEL("complexityLevel",
            "CLEVEL",
            NitfHeader::getComplexityLevel,
            BasicTypes.INTEGER_TYPE),
    STANDARD_TYPE("standardType", "STYPE", NitfHeader::getStandardType),
    ORIGINATING_STATION_ID("originatingStationId", "OSTAID", NitfHeader::getOriginatingStationId),
    FILE_DATE_AND_TIME("fileDateAndTime",
            "FDT",
            header -> convertNitfDate(header.getFileDateTime()),
            BasicTypes.DATE_TYPE),
    FILE_TITLE("fileTitle", "FTITLE", NitfHeader::getFileTitle),
    FILE_SECURITY_CLASSIFICATION("fileSecurityClassification",
            "FSCLAS",
            header -> header.getFileSecurityMetadata()
                    .getSecurityClassification()
                    .name()),
    FILE_CLASSIFICATION_SECURITY_SYSTEM("fileClassificationSecuritySystem",
            "FSCLSY",
            header -> header.getFileSecurityMetadata()
                    .getSecurityClassificationSystem()),
    FILE_CODE_WORDS("fileCodewords",
            "FSCODE",
            header -> header.getFileSecurityMetadata()
                    .getCodewords()),
    FILE_CONTROL_AND_HANDLING("fileControlAndHandling",
            "FSCTLH",
            header -> header.getFileSecurityMetadata()
                    .getControlAndHandling()),
    FILE_RELEASING_INSTRUCTIONS("fileReleasingInstructions",
            "FSREL",
            header -> header.getFileSecurityMetadata()
                    .getReleaseInstructions()),
    FILE_DECLASSIFICATION_TYPE("fileDeclassificationType",
            "FSDCTP",
            header -> header.getFileSecurityMetadata()
                    .getDeclassificationType()),
    FILE_DECLASSIFICATION_DATE("fileDeclassificationDate",
            "FSDCDT",
            header -> header.getFileSecurityMetadata()
                    .getDeclassificationDate()),
    FILE_DECLASSIFICATION_EXEMPTION("fileDeclassificationExemption",
            "FSDCXM",
            header -> header.getFileSecurityMetadata()
                    .getDeclassificationExemption()),
    FILE_DOWNGRADE("fileDowngrade",
            "FSDG",
            header -> header.getFileSecurityMetadata()
                    .getDowngrade()),
    FILE_DOWNGRADE_DATE("fileDowngradeDate",
            "FSDGDT",
            header -> header.getFileSecurityMetadata()
                    .getDowngradeDate()),
    FILE_CLASSIFICATION_TEXT("fileClassificationText",
            "FSCLTX",
            header -> header.getFileSecurityMetadata()
                    .getClassificationText()),
    FILE_CLASSIFICATION_AUTHORITY_TYPE("fileClassificationAuthorityType",
            "FSCATP",
            header -> header.getFileSecurityMetadata()
                    .getClassificationAuthorityType()),
    FILE_CLASSIFICATION_AUTHORITY("fileClassificationAuthority",
            "FSCAUT",
            header -> header.getFileSecurityMetadata()
                    .getClassificationAuthority()),
    FILE_CLASSIFICATION_REASON("fileClassificationReason",
            "FSCRSN",
            header -> header.getFileSecurityMetadata()
                    .getClassificationReason()),
    FILE_SECURITY_SOURCE_DATE("fileSecuritySourceDate",
            "FSSRDT",
            header -> header.getFileSecurityMetadata()
                    .getSecuritySourceDate()),
    FILE_SECURITY_CONTROL_NUMBER("fileSecurityControlNumber",
            "FSCTLN",
            header -> header.getFileSecurityMetadata()
                    .getSecurityControlNumber()),
    FILE_COPY_NUMBER("fileCopyNumber",
            "FSCOP",
            header -> header.getFileSecurityMetadata()
                    .getFileCopyNumber()),
    FILE_NUMBER_OF_COPIES("fileNumberOfCopies",
            "FSCPYS",
            header -> header.getFileSecurityMetadata()
                    .getFileNumberOfCopies()),
    FILE_BACKGROUND_COLOR("fileBackgroundColor",
            "FBKGC",
            header -> header.getFileBackgroundColour() != null ?
                    header.getFileBackgroundColour()
                            .toString() :
                    ""),
    ORIGINATORS_NAME("originatorsName", "ONAME", NitfHeader::getOriginatorsName),
    ORIGINATORS_PHONE_NUMBER("originatorsPhoneNumber",
            "OPHONE",
            NitfHeader::getOriginatorsPhoneNumber);

    public static final String ATTRIBUTE_NAME_PREFIX = "nitf.";

    private String shortName;

    private String longName;

    private Function<NitfHeader, Serializable> accessorFunction;

    private AttributeDescriptor attributeDescriptor;

    private NitfHeaderAttribute(final String lName, final String sName,
            final Function<NitfHeader, Serializable> function) {
        this(lName, sName, function, BasicTypes.STRING_TYPE);
    }

    private NitfHeaderAttribute(final String lName, final String sName,
            final Function<NitfHeader, Serializable> function, AttributeType attributeType) {
        this.shortName = sName;
        this.longName = lName;
        this.accessorFunction = function;
        this.attributeDescriptor = new AttributeDescriptorImpl(ATTRIBUTE_NAME_PREFIX + longName,
                true,
                true,
                false,
                false,
                attributeType);
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
    public final String getShortName() {
        return this.shortName;
    }

    /**
     * {@inheritDoc}
     */
    public final String getLongName() {
        return this.longName;
    }

    /**
     * {@inheritDoc}
     */
    public Function<NitfHeader, Serializable> getAccessorFunction() {
        return accessorFunction;
    }

    /**
     * Equivalent to getLongName().
     *
     * @return the attribute's long name.
     */
    public String toString() {
        return getLongName();
    }

    @Override
    public AttributeDescriptor getAttributeDescriptor() {
        return this.attributeDescriptor;
    }
}
