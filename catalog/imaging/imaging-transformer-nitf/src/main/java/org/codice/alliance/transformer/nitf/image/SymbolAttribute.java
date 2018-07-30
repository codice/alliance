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
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.codice.alliance.transformer.nitf.ExtNitfUtility;
import org.codice.alliance.transformer.nitf.common.NitfAttribute;
import org.codice.imaging.nitf.core.symbol.SymbolSegment;

/** NitfAttributes to represent the properties of a SymbolSegment. */
public enum SymbolAttribute implements NitfAttribute<SymbolSegment> {

  /*
   * Non-normalized attributes
   */

  FILE_PART_TYPE("file-part-type", "SY", segment -> "SY"),
  SYMBOL_ID("symbol-iD", "SID", SymbolSegment::getIdentifier),
  SYMBOL_NAME("symbol-name", "SNAME", SymbolSegment::getSymbolName),
  SYMBOL_SECURITY_CLASSIFICATION(
      "symbol-security-classification",
      "SSCLAS",
      segment -> segment.getSecurityMetadata().getSecurityClassification().name()),
  SYMBOL_CODEWORDS(
      "symbol-codewords", "SSCODE", segment -> segment.getSecurityMetadata().getCodewords()),
  SYMBOL_CONTROL_AND_HANDLING(
      "symbol-control-and-handling",
      "SSCTLH",
      segment -> segment.getSecurityMetadata().getControlAndHandling()),
  SYMBOL_RELEASING_INSTRUCTIONS(
      "symbol-releasing-instructions",
      "SSREL",
      segment -> segment.getSecurityMetadata().getReleaseInstructions()),
  SYMBOL_CLASSIFICATION_AUTHORITY(
      "symbol-classification-authority",
      "SSCAUT",
      segment -> segment.getSecurityMetadata().getClassificationAuthority()),
  SYMBOL_SECURITY_CONTROL_NUMBER(
      "symbol-security-control-number",
      "SSCTLN",
      segment -> segment.getSecurityMetadata().getSecurityControlNumber()),
  SYMBOL_SECURITY_DOWNGRADE(
      "symbol-security-downgrade",
      "SSDWNG",
      segment -> segment.getSecurityMetadata().getDowngrade()),
  SYMBOL_DOWNGRADING_EVENT(
      "symbol-downgrading-event",
      "SSDEVT",
      segment -> segment.getSecurityMetadata().getDowngradeEvent()),
  SYMBOL_TYPE("symbol-type", "STYPE", segment -> segment.getSymbolType().name()),
  NUMBER_OF_LINES_PER_SYMBOL(
      "number-of-lines-per-symbol",
      "NLIPS",
      SymbolSegment::getNumberOfLinesPerSymbol,
      Collections.singletonList(BasicTypes.INTEGER_TYPE)),
  NUMBER_OF_PIXELS_PER_LINE(
      "number-of-pixels-per-line",
      "NPIXPL",
      SymbolSegment::getNumberOfPixelsPerLine,
      Collections.singletonList(BasicTypes.INTEGER_TYPE)),
  LINE_WIDTH(
      "line-width",
      "NWDTH",
      SymbolSegment::getLineWidth,
      Collections.singletonList(BasicTypes.INTEGER_TYPE)),
  NUMBER_OF_BITS_PER_PIXEL(
      "number-of-bits-per-pixel",
      "NBPP",
      SymbolSegment::getNumberOfBitsPerPixel,
      Collections.singletonList(BasicTypes.INTEGER_TYPE)),
  DISPLAY_LEVEL(
      "display-level",
      "SDLVL",
      SymbolSegment::getSymbolDisplayLevel,
      Collections.singletonList(BasicTypes.INTEGER_TYPE)),
  ATTACHMENT_LEVEL(
      "attachment-level",
      "SALVL",
      SymbolSegment::getAttachmentLevel,
      Collections.singletonList(BasicTypes.INTEGER_TYPE)),
  SYMBOL_LOCATION(
      "symbol-location",
      "SLOC",
      segment ->
          String.format(
              "%s,%s", segment.getSymbolLocationRow(), segment.getSymbolLocationColumn())),
  SECOND_SYMBOL_LOCATION(
      "second-symbol-location",
      "SLOC2",
      segment ->
          String.format(
              "%s,%s", segment.getSymbolLocation2Row(), segment.getSymbolLocation2Column())),
  SYMBOL_COLOR("symbol-color", "SCOLOR", segment -> segment.getSymbolColour().toString()),
  SYMBOL_NUMBER("symbol-number", "SNUM", SymbolSegment::getSymbolNumber),
  SYMBOL_ROTATION(
      "symbol-rotation",
      "SROT",
      SymbolSegment::getSymbolRotation,
      Collections.singletonList(BasicTypes.INTEGER_TYPE)),
  EXTENDED_SUBHEADER_DATA_LENGTH(
      "extended-subheader-data-length",
      "SXSHDL",
      SymbolSegment::getExtendedHeaderDataOverflow,
      Collections.singletonList(BasicTypes.INTEGER_TYPE));

  private static final String ATTRIBUTE_NAME_PREFIX = "symbol.";

  private final String shortName;

  private final String longName;

  private final Function<SymbolSegment, Serializable> accessorFunction;

  private final Set<AttributeDescriptor> attributeDescriptors;

  SymbolAttribute(
      final String lName,
      final String sName,
      final Function<SymbolSegment, Serializable> accessor) {
    this(lName, sName, accessor, Collections.singletonList(BasicTypes.STRING_TYPE));
  }

  SymbolAttribute(
      final String lName,
      final String sName,
      final Function<SymbolSegment, Serializable> accessor,
      List<AttributeType> attributeTypes) {
    this.accessorFunction = accessor;
    this.shortName = sName;
    this.longName = lName;
    this.attributeDescriptors = createAttributeDescriptors(attributeTypes);
  }

  /** {@inheritDoc} */
  @Override
  public final String getShortName() {
    return this.shortName;
  }

  /** {@inheritDoc} */
  @Override
  public final String getLongName() {
    return this.longName;
  }

  /** {@inheritDoc} */
  @Override
  public Function<SymbolSegment, Serializable> getAccessorFunction() {
    return accessorFunction;
  }

  /** [@inheritDoc} */
  @Override
  public Function<SymbolSegment, Serializable> getExtAccessorFunction() {
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

  /** {@inheritDoc} */
  @Override
  public Set<AttributeDescriptor> getAttributeDescriptors() {
    return this.attributeDescriptors;
  }

  private Set<AttributeDescriptor> createAttributeDescriptors(List<AttributeType> attributeTypes) {
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
