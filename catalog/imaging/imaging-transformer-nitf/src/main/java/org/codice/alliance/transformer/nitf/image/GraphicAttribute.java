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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.codice.alliance.transformer.nitf.ExtNitfUtility;
import org.codice.alliance.transformer.nitf.common.NitfAttribute;
import org.codice.imaging.nitf.core.graphic.GraphicSegment;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;

/**
 * NitfAttributes to represent the properties of a GraphicSegment.
 */
public enum GraphicAttribute implements NitfAttribute<GraphicSegment> {
    FILE_PART_TYPE("filePartType", "SY", segment -> "SY"),
    GRAPHIC_IDENTIFIER("graphicIdentifier", "SID", GraphicSegment::getIdentifier),
    GRAPHIC_NAME("graphicName", "SNAME", GraphicSegment::getGraphicName),
    GRAPHIC_SECURITY_CLASSIFICATION("graphicSecurityClassification",
            "SSCLAS",
            segment -> segment.getSecurityMetadata()
                    .getSecurityClassification()
                    .name()),
    GRAPHIC_CLASSIFICATION_SECURITY_SYSTEM("graphicClassificationSecuritySystem",
            "SSCLSY",
            segment -> segment.getSecurityMetadata()
                    .getSecurityClassificationSystem()),
    GRAPHIC_CODEWORDS("graphicCodewords",
            "SSCODE",
            segment -> segment.getSecurityMetadata()
                    .getCodewords()),
    GRAPHIC_CONTROL_AND_HANDLING("graphicControlAndHandling",
            "SSCTLH",
            segment -> segment.getSecurityMetadata()
                    .getControlAndHandling()),
    GRAPHIC_RELEASING_INSTRUCTIONS("graphicReleasingInstructions",
            "SSREL",
            segment -> segment.getSecurityMetadata()
                    .getReleaseInstructions()),
    GRAPHIC_DECLASSIFICATION_TYPE("graphicDeclassificationType",
            "SSDCTP",
            segment -> segment.getSecurityMetadata()
                    .getDeclassificationType()),
    GRAPHIC_DECLASSIFICATION_DATE("graphicDeclassificationDate",
            "SSDCDT",
            segment -> segment.getSecurityMetadata()
                    .getDeclassificationDate()),
    GRAPHIC_DECLASSIFICATION_EXEMPTION("graphicDeclassificationExemption",
            "SSDCXM",
            segment -> segment.getSecurityMetadata()
                    .getDeclassificationExemption()),
    GRAPHIC_DOWNGRADE("graphicDowngrade",
            "SSDG",
            segment -> segment.getSecurityMetadata()
                    .getDowngrade()),
    GRAPHIC_DOWNGRADE_DATE("graphicDowngradeDate",
            "SSDGDT",
            segment -> segment.getSecurityMetadata()
                    .getDowngradeDate()),
    GRAPHIC_CLASSIFICATION_TEXT("graphicClassificationText",
            "SSCLTX",
            segment -> segment.getSecurityMetadata()
                    .getClassificationText()),
    GRAPHIC_CLASSIFICATION_AUTHORITY_TYPE("graphicClassificationAuthorityType",
            "SSCATP",
            segment -> segment.getSecurityMetadata()
                    .getClassificationAuthorityType()),
    GRAPHIC_CLASSIFICATION_AUTHORITY("graphicClassificationAuthority",
            "SSCAUT",
            segment -> segment.getSecurityMetadata()
                    .getClassificationAuthority()),
    GRAPHIC_CLASSIFICATION_REASON("graphicClassificationReason",
            "SSCRSN",
            segment -> segment.getSecurityMetadata()
                    .getClassificationReason()),
    GRAPHIC_SECURITY_SOURCE_DATE("graphicSecuritySourceDate",
            "SSSRDT",
            segment -> segment.getSecurityMetadata()
                    .getSecuritySourceDate()),
    GRAPHIC_SECURITY_CONTROL_NUMBER("graphicSecurityControlNumber",
            "SSCTLN",
            segment -> segment.getSecurityMetadata()
                    .getSecurityControlNumber()),
    GRAPHIC_DISPLAY_LEVEL("graphicDisplayLevel",
            "SDLVL",
            GraphicSegment::getGraphicDisplayLevel,
            Collections.singletonList(BasicTypes.INTEGER_TYPE)),
    GRAPHIC_ATTACHMENT_LEVEL("graphicAttachmentLevel",
            "SALVL",
            GraphicSegment::getAttachmentLevel,
            Collections.singletonList(BasicTypes.INTEGER_TYPE)),
    GRAPHIC_LOCATION("graphicLocation",
            "SLOC",
            segment -> segment.getGraphicLocationRow() + "," + segment.getGraphicLocationColumn()),
    GRAPHIC_COLOR("graphicColor",
            "SCOLOR",
            segment -> segment.getGraphicColour()
                    .toString()),
    GRAPHIC_EXTENDED_SUBHEADER_DATA_LENGTH("graphicExtendedSubheaderDataLength",
            "SXSHDL",
            GraphicSegment::getExtendedHeaderDataOverflow,
            Collections.singletonList(BasicTypes.INTEGER_TYPE));

    private static final String ATTRIBUTE_NAME_PREFIX = "graphic.";

    private String shortName;

    private String longName;

    private Function<GraphicSegment, Serializable> accessorFunction;

    private Set<AttributeDescriptor> attributeDescriptors;

    GraphicAttribute(final String lName, final String sName,
            final Function<GraphicSegment, Serializable> accessor) {
        this(lName, sName, accessor, Collections.singletonList(BasicTypes.STRING_TYPE));
    }

    GraphicAttribute(final String lName, final String sName,
            final Function<GraphicSegment, Serializable> accessor,
            final List<AttributeType> attributeTypes) {
        this.accessorFunction = accessor;
        this.shortName = sName;
        this.longName = lName;
        this.attributeDescriptors = createAttributeDescriptors(attributeTypes);
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
    public Function<GraphicSegment, Serializable> getAccessorFunction() {
        return this.accessorFunction;
    }

    /**
     * Equivalent to getLongName()
     *
     * @return the attribute's long name.
     */
    @Override
    public String toString() {
        return this.getLongName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<AttributeDescriptor> getAttributeDescriptors() {
        return this.attributeDescriptors;
    }

    private Set<AttributeDescriptor> createAttributeDescriptors(
            List<AttributeType> attributeTypes) {
        Set<AttributeDescriptor> attributesDescriptors = new HashSet<>();
        for (AttributeType attribute : attributeTypes) {
            attributesDescriptors.add(createAttributeDescriptor(attribute));
        }
        return attributesDescriptors;
    }

    private AttributeDescriptor createAttributeDescriptor(AttributeType attributeType) {
        return new AttributeDescriptorImpl(
                ExtNitfUtility.EXT_NITF_PREFIX + ATTRIBUTE_NAME_PREFIX + longName,
                true, /* indexed */
                true, /* stored */
                false, /* tokenized */
                true, /* multivalued */
                attributeType);
    }
}
