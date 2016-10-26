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
import org.codice.imaging.nitf.core.label.LabelSegment;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;

/**
 * NitfAttributes to represent the properties of a LabelSegmentHeader.
 */
public enum LabelAttribute implements NitfAttribute<LabelSegment> {
    FILE_PART_TYPE("file-part-type", "LA", segment -> "LA"),
    LABEL_ID("label-id", "LID", LabelSegment::getIdentifier),
    LABEL_SECURITY_CLASSIFICATION("label-security-classification",
            "LSCLAS",
            segment -> segment.getSecurityMetadata()
                    .getSecurityClassification()
                    .name()),
    LABEL_CODEWORDS("label-codewords",
            "LSCODE",
            segment -> segment.getSecurityMetadata()
                    .getCodewords()),
    LABEL_CONTROL_AND_HANDLING("label-control-and-handling",
            "LSCTLH",
            segment -> segment.getSecurityMetadata()
                    .getControlAndHandling()),
    LABEL_RELEASING_INSTRUCTIONS("label-releasing-instructions",
            "LSREL",
            segment -> segment.getSecurityMetadata()
                    .getReleaseInstructions()),
    LABEL_CLASSIFICATION_AUTHORITY("label-classification-authority",
            "LSCAUT",
            segment -> segment.getSecurityMetadata()
                    .getClassificationAuthority()),
    LABEL_SECURITY_CONTROL_NUMBER("label-security-control-number",
            "LSCTLN",
            segment -> segment.getSecurityMetadata()
                    .getSecurityControlNumber()),
    LABEL_SECURITY_DOWNGRADE("label-security-downgrade",
            "LSDWNG",
            segment -> segment.getSecurityMetadata()
                    .getDowngrade()),
    LABEL_DOWNGRADING_EVENT("label-downgrading-event",
            "LSDEVT",
            segment -> segment.getSecurityMetadata()
                    .getDowngradeEvent()),
    LABEL_CELL_WIDTH("label-cell-width",
            "LCW",
            LabelSegment::getLabelCellWidth,
            Collections.singletonList(BasicTypes.INTEGER_TYPE)),
    LABEL_CELL_HEIGHT("label-cell-height",
            "LCH",
            LabelSegment::getLabelCellHeight,
            Collections.singletonList(BasicTypes.INTEGER_TYPE)),
    LABEL_DISPLAY_LEVEL("label-display-level",
            "LDLVL",
            LabelSegment::getLabelDisplayLevel,
            Collections.singletonList(BasicTypes.INTEGER_TYPE)),
    ATTACHMENT_LEVEL("attachment-level",
            "LALVL",
            LabelSegment::getAttachmentLevel,
            Collections.singletonList(BasicTypes.INTEGER_TYPE)),
    LABEL_LOCATION("label-location",
            "LLOC",
            segment -> String.format("%s,%s",
                    segment.getLabelLocationRow(),
                    segment.getLabelLocationColumn())),
    LABEL_TEXT_COLOR("label-text-color",
            "LTC",
            segment -> segment.getLabelTextColour()
                    .toString()),
    LABEL_BACKGROUND_COLOR("label-background-color",
            "LBC",
            segment -> segment.getLabelBackgroundColour()
                    .toString()),
    EXTENDED_SUBHEADER_DATA_LENGTH("extended-subheader-data-length",
            "LXSHDL",
            LabelSegment::getExtendedHeaderDataOverflow,
            Collections.singletonList(BasicTypes.INTEGER_TYPE));

    private static final String ATTRIBUTE_NAME_PREFIX = "label.";

    private String shortName;

    private String longName;

    private Function<LabelSegment, Serializable> accessorFunction;

    private Set<AttributeDescriptor> attributeDescriptors;

    LabelAttribute(final String lName, final String sName,
            final Function<LabelSegment, Serializable> accessor) {
        this(lName, sName, accessor, Collections.singletonList(BasicTypes.STRING_TYPE));
    }

    LabelAttribute(final String lName, final String sName,
            final Function<LabelSegment, Serializable> accessor,
            List<AttributeType> attributeTypes) {
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
    public Function<LabelSegment, Serializable> getAccessorFunction() {
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
