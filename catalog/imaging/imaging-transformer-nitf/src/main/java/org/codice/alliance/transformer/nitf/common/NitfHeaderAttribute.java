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
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.codice.alliance.catalog.core.api.impl.types.IsrAttributes;
import org.codice.alliance.catalog.core.api.impl.types.SecurityAttributes;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.catalog.core.api.types.Security;
import org.codice.imaging.nitf.core.common.DateTime;
import org.codice.imaging.nitf.core.header.NitfHeader;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;
import ddf.catalog.data.impl.types.ContactAttributes;
import ddf.catalog.data.impl.types.CoreAttributes;
import ddf.catalog.data.impl.types.MediaAttributes;
import ddf.catalog.data.types.Contact;
import ddf.catalog.data.types.Core;
import ddf.catalog.data.types.Media;

public class NitfHeaderAttribute extends NitfAttributeImpl<NitfHeader> {

    private static final List<NitfAttribute<NitfHeader>> ATTRIBUTES = new LinkedList<>();

    /*
     * Normalized attributes. These taxonomy terms will be duplicated by `ext.nitf.*` when
     * appropriate
     */

    public static final NitfHeaderAttribute FILE_PROFILE_NAME =
            new NitfHeaderAttribute(Media.FORMAT,
                    "FHDR",
                    header -> header.getFileType()
                            .name(),
                    new MediaAttributes().getAttributeDescriptor(Media.FORMAT),
                    "fileProfileName",
                    "");

    public static final NitfHeaderAttribute FILE_VERSION =
            new NitfHeaderAttribute(Media.FORMAT_VERSION,
                    "FVER",
                    header -> header.getFileType()
                            .name(),
                    new MediaAttributes().getAttributeDescriptor(Media.FORMAT),
                    "fileVersion",
                    "");

    public static final NitfHeaderAttribute ORIGINATING_STATION_ID =
            new NitfHeaderAttribute(Isr.ORGANIZATIONAL_UNIT,
                    "OSTAID",
                    NitfHeader::getOriginatingStationId,
                    new IsrAttributes().getAttributeDescriptor(Isr.ORGANIZATIONAL_UNIT),
                    "originatingStationId",
                    "");

    public static final NitfHeaderAttribute FILE_TITLE = new NitfHeaderAttribute(Core.TITLE,
            "FTITLE",
            NitfHeader::getFileTitle,
            new CoreAttributes().getAttributeDescriptor(Core.TITLE),
            "fileTitle",
            "");

    public static final NitfHeaderAttribute FILE_DATE_AND_TIME_CREATED = new NitfHeaderAttribute(
            Core.CREATED,
            "FDT",
            header -> convertNitfDate(header.getFileDateTime()),
            new CoreAttributes().getAttributeDescriptor(Core.CREATED),
            "fileDateAndTime",
            "");

    public static final NitfHeaderAttribute FILE_DATE_AND_TIME_MODIFIED = new NitfHeaderAttribute(
            Core.MODIFIED,
            "FDT",
            header -> convertNitfDate(header.getFileDateTime()),
            new CoreAttributes().getAttributeDescriptor(Core.MODIFIED),
            "",
            "");

    public static final NitfHeaderAttribute FILE_DATE_AND_TIME_EFFECTIVE = new NitfHeaderAttribute(
            Metacard.EFFECTIVE,
            "FDT",
            header -> convertNitfDate(header.getFileDateTime()),
            new AttributeDescriptorImpl(Metacard.EFFECTIVE, true, /* indexed */
                    true, /* stored */
                    false, /* tokenized */
                    false, /* multivalued */
                    BasicTypes.DATE_TYPE),
            "",
            "");

    public static final NitfHeaderAttribute FILE_SECURITY_CLASSIFICATION = new NitfHeaderAttribute(
            Security.CLASSIFICATION,
            "FSCLAS",
            header -> header.getFileSecurityMetadata()
                    .getSecurityClassification()
                    .name(),
            new SecurityAttributes().getAttributeDescriptor(Security.CLASSIFICATION),
            "fileSecurityClassification",
            "");

    public static final NitfHeaderAttribute FILE_CLASSIFICATION_SECURITY_SYSTEM =
            new NitfHeaderAttribute(Security.CLASSIFICATION_SYSTEM,
                    "FSCLSY",
                    header -> header.getFileSecurityMetadata()
                            .getSecurityClassificationSystem(),
                    new SecurityAttributes().getAttributeDescriptor(Security.CLASSIFICATION_SYSTEM),
                    "fileClassificationSecuritySystem",
                    "");

    public static final NitfHeaderAttribute FILE_CODE_WORDS =
            new NitfHeaderAttribute(Security.CODEWORDS,
                    "FSCODE",
                    header -> header.getFileSecurityMetadata()
                            .getCodewords(),
                    new SecurityAttributes().getAttributeDescriptor(Security.CODEWORDS),
                    "fileCodewords",
                    "");

    public static final NitfHeaderAttribute FILE_CONTROL_AND_HANDLING = new NitfHeaderAttribute(
            Security.DISSEMINATION_CONTROLS,
            "FSCTLH",
            header -> header.getFileSecurityMetadata()
                    .getControlAndHandling(),
            new SecurityAttributes().getAttributeDescriptor(Security.DISSEMINATION_CONTROLS),
            "fileControlAndHandling",
            "");

    public static final NitfHeaderAttribute FILE_RELEASING_INSTRUCTIONS = new NitfHeaderAttribute(
            Security.RELEASABILITY,
            "FSREL",
            header -> header.getFileSecurityMetadata()
                    .getReleaseInstructions(),
            new SecurityAttributes().getAttributeDescriptor(Security.RELEASABILITY),
            "fileReleasingInstructions",
            "");

    public static final NitfHeaderAttribute ORIGINATORS_NAME =
            new NitfHeaderAttribute(Contact.CREATOR_NAME,
                    "ONAME",
                    NitfHeader::getOriginatorsName,
                    new ContactAttributes().getAttributeDescriptor(Contact.CREATOR_NAME),
                    "originatorsName",
                    "");

    public static final NitfHeaderAttribute ORIGINATORS_PHONE_NUMBER = new NitfHeaderAttribute(
            Contact.CREATOR_PHONE,
            "OPHONE",
            NitfHeader::getOriginatorsPhoneNumber,
            new ContactAttributes().getAttributeDescriptor(Contact.CREATOR_PHONE),
            "originatorsPhoneNumber",
            "");

    /*
     * Non normalized attributes
     */

    public static final NitfHeaderAttribute COMPLEXITY_LEVEL = new NitfHeaderAttribute(
            "complexityLevel",
            "CLEVEL",
            NitfHeader::getComplexityLevel,
            BasicTypes.INTEGER_TYPE,
            "");

    public static final NitfHeaderAttribute FILE_DATE_AND_TIME = new NitfHeaderAttribute(
            "fileDateAndTime",
            "FDT",
            header -> convertNitfDate(header.getFileDateTime()),
            BasicTypes.DATE_TYPE,
            "");

    public static final NitfHeaderAttribute STANDARD_TYPE = new NitfHeaderAttribute("standardType",
            "STYPE",
            NitfHeader::getStandardType,
            BasicTypes.STRING_TYPE,
            "");

    public static final NitfHeaderAttribute FILE_DECLASSIFICATION_EXEMPTION =
            new NitfHeaderAttribute("fileDeclassificationExemption",
                    "FSDCXM",
                    header -> header.getFileSecurityMetadata()
                            .getDeclassificationExemption(),
                    BasicTypes.STRING_TYPE,
                    "");

    public static final NitfHeaderAttribute FILE_DECLASSIFICATION_TYPE = new NitfHeaderAttribute(
            "fileDeclassificationType",
            "FSDCTP",
            header -> header.getFileSecurityMetadata()
                    .getDeclassificationType(),
            BasicTypes.STRING_TYPE,
            "");

    public static final NitfHeaderAttribute FILE_DECLASSIFICATION_DATE = new NitfHeaderAttribute(
            "fileDeclassificationDate",
            "FSDCDT",
            header -> header.getFileSecurityMetadata()
                    .getDeclassificationDate(),
            BasicTypes.STRING_TYPE,
            "");

    public static final NitfHeaderAttribute FILE_DOWNGRADE =
            new NitfHeaderAttribute("fileDowngrade",
                    "FSDG",
                    header -> header.getFileSecurityMetadata()
                            .getDowngrade(),
                    BasicTypes.STRING_TYPE,
                    "");

    public static final NitfHeaderAttribute FILE_DOWNGRADE_DATE = new NitfHeaderAttribute(
            "fileDowngradeDate",
            "FSDGDT",
            header -> header.getFileSecurityMetadata()
                    .getDowngradeDate(),
            BasicTypes.STRING_TYPE,
            "");

    public static final NitfHeaderAttribute FILE_CLASSIFICATION_TEXT = new NitfHeaderAttribute(
            "fileClassificationText",
            "FSCLTX",
            header -> header.getFileSecurityMetadata()
                    .getClassificationText(),
            BasicTypes.STRING_TYPE,
            "");

    public static final NitfHeaderAttribute FILE_CLASSIFICATION_AUTHORITY_TYPE =
            new NitfHeaderAttribute("fileClassificationAuthorityType",
                    "FSCATP",
                    header -> header.getFileSecurityMetadata()
                            .getClassificationAuthorityType(),
                    BasicTypes.STRING_TYPE,
                    "");

    public static final NitfHeaderAttribute FILE_CLASSIFICATION_AUTHORITY = new NitfHeaderAttribute(
            "fileClassificationAuthority",
            "FSCAUT",
            header -> header.getFileSecurityMetadata()
                    .getClassificationAuthority(),
            BasicTypes.STRING_TYPE,
            "");

    public static final NitfHeaderAttribute FILE_CLASSIFICATION_REASON = new NitfHeaderAttribute(
            "fileClassificationReason",
            "FSCRSN",
            header -> header.getFileSecurityMetadata()
                    .getClassificationReason(),
            BasicTypes.STRING_TYPE,
            "");

    public static final NitfHeaderAttribute FILE_SECURITY_SOURCE_DATE = new NitfHeaderAttribute(
            "fileSecuritySourceDate",
            "FSSRDT",
            header -> header.getFileSecurityMetadata()
                    .getSecuritySourceDate(),
            BasicTypes.STRING_TYPE,
            "");

    public static final NitfHeaderAttribute FILE_SECURITY_CONTROL_NUMBER = new NitfHeaderAttribute(
            "fileSecurityControlNumber",
            "FSCTLN",
            header -> header.getFileSecurityMetadata()
                    .getSecurityControlNumber(),
            BasicTypes.STRING_TYPE,
            "");

    public static final NitfHeaderAttribute FILE_COPY_NUMBER = new NitfHeaderAttribute(
            "fileCopyNumber",
            "FSCOP",
            header -> header.getFileSecurityMetadata()
                    .getFileCopyNumber(),
            BasicTypes.STRING_TYPE,
            "");

    public static final NitfHeaderAttribute FILE_NUMBER_OF_COPIES = new NitfHeaderAttribute(
            "fileNumberOfCopies",
            "FSCPYS",
            header -> header.getFileSecurityMetadata()
                    .getFileNumberOfCopies(),
            BasicTypes.STRING_TYPE,
            "");

    public static final NitfHeaderAttribute FILE_BACKGROUND_COLOR = new NitfHeaderAttribute(
            "fileBackgroundColor",
            "FBKGC",
            header -> header.getFileBackgroundColour() != null ?
                    header.getFileBackgroundColour()
                            .toString() :
                    "",
            BasicTypes.STRING_TYPE,
            "");

    private NitfHeaderAttribute(String longName, String shortName,
            Function<NitfHeader, Serializable> accessorFunction, AttributeType attributeType,
            String prefix) {
        super(longName, shortName, accessorFunction, attributeType, prefix);
        ATTRIBUTES.add(this);
    }

    private NitfHeaderAttribute(String longName, String shortName,
            Function<NitfHeader, Serializable> accessorFunction,
            AttributeDescriptor attributeDescriptor, String extNitfName, String prefix) {
        super(longName, shortName, accessorFunction, attributeDescriptor, extNitfName, prefix);
        ATTRIBUTES.add(this);
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

    public static List<NitfAttribute<NitfHeader>> getAttributes() {
        return Collections.unmodifiableList(ATTRIBUTES);
    }
}
