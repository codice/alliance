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
package org.codice.alliance.transformer.nitf;

import java.io.Serializable;
import java.util.function.Function;

import org.codice.imaging.nitf.core.symbol.SymbolSegment;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;

/**
 * NitfAttributes to represent the properties of a SymbolSegment.
 */
enum SymbolAttribute implements NitfAttribute<SymbolSegment> {
    FILE_PART_TYPE("filePartType", "SY", segment -> "SY"),
    SYMBOL_ID("symbolID", "SID", SymbolSegment::getIdentifier),
    SYMBOL_NAME("symbolName", "SNAME", SymbolSegment::getSymbolName),
    SYMBOL_SECURITY_CLASSIFICATION("symbolSecurityClassification", "SSCLAS",
            segment -> segment.getSecurityMetadata().getSecurityClassification().name()),
    SYMBOL_CODEWORDS("symbolCodewords", "SSCODE",
            segment -> segment.getSecurityMetadata().getCodewords()),
    SYMBOL_CONTROL_AND_HANDLING("symbolControlandHandling", "SSCTLH",
            segment -> segment.getSecurityMetadata().getControlAndHandling()),
    SYMBOL_RELEASING_INSTRUCTIONS("symbolReleasingInstructions", "SSREL",
            segment -> segment.getSecurityMetadata().getReleaseInstructions()),
    SYMBOL_CLASSIFICATION_AUTHORITY("symbolClassificationAuthority", "SSCAUT",
            segment -> segment.getSecurityMetadata().getClassificationAuthority()),
    SYMBOL_SECURITY_CONTROL_NUMBER("symbolSecurityControlNumber", "SSCTLN",
            segment -> segment.getSecurityMetadata().getSecurityControlNumber()),
    SYMBOL_SECURITY_DOWNGRADE("symbolSecurityDowngrade", "SSDWNG",
            segment -> segment.getSecurityMetadata().getDowngrade()),
    SYMBOL_DOWNGRADING_EVENT("symbolDowngradingEvent", "SSDEVT",
            segment -> segment.getSecurityMetadata().getDowngradeEvent()),
    SYMBOL_TYPE("symbolType", "STYPE", segment -> segment.getSymbolType().name()),
    NUMBER_OF_LINES_PER_SYMBOL("numberOfLinesPerSymbol", "NLIPS",
            SymbolSegment::getNumberOfLinesPerSymbol, BasicTypes.INTEGER_TYPE),
    NUMBER_OF_PIXELS_PER_LINE("numberOfPixelsPerLine", "NPIXPL",
            SymbolSegment::getNumberOfPixelsPerLine, BasicTypes.INTEGER_TYPE),
    LINE_WIDTH("lineWidth", "NWDTH", SymbolSegment::getLineWidth, BasicTypes.INTEGER_TYPE),
    NUMBER_OF_BITS_PER_PIXEL("numberOfBitsPerPixel", "NBPP",
            SymbolSegment::getNumberOfBitsPerPixel, BasicTypes.INTEGER_TYPE),
    DISPLAY_LEVEL("displayLevel", "SDLVL", SymbolSegment::getSymbolDisplayLevel, BasicTypes.INTEGER_TYPE),
    ATTACHMENT_LEVEL("attachmentLevel", "SALVL", SymbolSegment::getAttachmentLevel, BasicTypes.INTEGER_TYPE),
    SYMBOL_LOCATION("symbolLocation", "SLOC", segment -> String
            .format("%s,%s", segment.getSymbolLocationRow(), segment.getSymbolLocationColumn())),
    SECOND_SYMBOL_LOCATION("secondSymbolLocation", "SLOC2", segment -> String
            .format("%s,%s", segment.getSymbolLocation2Row(),
                    segment.getSymbolLocation2Column())),
    SYMBOL_COLOR("symbolColor", "SCOLOR", segment -> segment.getSymbolColour().toString()),
    SYMBOL_NUMBER("symbolNumber", "SNUM", SymbolSegment::getSymbolNumber),
    SYMBOL_ROTATION("symbolRotation", "SROT", SymbolSegment::getSymbolRotation, BasicTypes.INTEGER_TYPE),
    EXTENDED_SUBHEADER_DATA_LENGTH("extendedSubheaderDataLength", "SXSHDL",
            SymbolSegment::getExtendedHeaderDataOverflow, BasicTypes.INTEGER_TYPE);

    public static final String ATTRIBUTE_NAME_PREFIX = "nitf.symbol.";

    private String shortName;

    private String longName;

    private Function<SymbolSegment, Serializable> accessorFunction;

    private AttributeDescriptor attributeDescriptor;

    private SymbolAttribute(final String lName, final String sName,
            final Function<SymbolSegment, Serializable> accessor) {
        this(lName, sName, accessor, BasicTypes.STRING_TYPE);
    }

    private SymbolAttribute(final String lName, final String sName,
            final Function<SymbolSegment, Serializable> accessor, AttributeType attributeType) {
        this.accessorFunction = accessor;
        this.shortName = sName;
        this.longName = lName;
        this.attributeDescriptor = new AttributeDescriptorImpl(
                ATTRIBUTE_NAME_PREFIX + longName,
                true,
                true,
                false,
                true,
                attributeType
        );
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
    public Function<SymbolSegment, Serializable> getAccessorFunction() {
        return accessorFunction;
    }

    /**
     * Equivalent to getLongName()
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
