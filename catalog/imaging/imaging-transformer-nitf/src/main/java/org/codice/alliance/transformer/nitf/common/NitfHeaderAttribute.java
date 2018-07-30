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
package org.codice.alliance.transformer.nitf.common;

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
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.codice.alliance.catalog.core.api.impl.types.IsrAttributes;
import org.codice.alliance.catalog.core.api.impl.types.SecurityAttributes;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.catalog.core.api.types.Security;
import org.codice.alliance.transformer.nitf.ExtNitfUtility;
import org.codice.alliance.transformer.nitf.NitfAttributeConverters;
import org.codice.imaging.nitf.core.common.FileType;
import org.codice.imaging.nitf.core.header.NitfHeader;

public class NitfHeaderAttribute extends NitfAttributeImpl<NitfHeader> {

  private static final String PREFIX = ExtNitfUtility.EXT_NITF_PREFIX;

  public static final String FILE_PROFILE_NAME = PREFIX + "file-profile-name";

  public static final String FILE_VERSION = PREFIX + "file-version";

  public static final String ORIGINATING_STATION_ID = PREFIX + "originating-station-id";

  public static final String FILE_TITLE = PREFIX + "file-title";

  public static final String FILE_DATE_AND_TIME = PREFIX + "file-date-and-time";

  public static final String FILE_SECURITY_CLASSIFICATION = PREFIX + "file-security-classification";

  public static final String FILE_CLASSIFICATON_SECURITY_SYSTEM =
      PREFIX + "file-classification-security-system";

  public static final String FILE_CODEWORDS = PREFIX + "file-codewords";

  public static final String FILE_CONTROL_AND_HANDLING = PREFIX + "file-control-and-handling";

  public static final String FILE_RELEASING_INSTRUCTIONS = PREFIX + "file-releasing-instructions";

  public static final String ORIGINATORS_NAME = PREFIX + "originators-name";

  public static final String ORIGINATORS_PHONE_NUMBER = PREFIX + "originators-phone-number";

  public static final String COMPLEXITY_LEVEL = PREFIX + "complexity-level";

  public static final String STANDARD_TYPE = PREFIX + "standard-type";

  public static final String FILE_DECLASSIFICATION_EXEMPTION =
      PREFIX + "file-declassification-exemption";

  public static final String FILE_DECLASSIFICATION_TYPE = PREFIX + "file-declassification-type";

  public static final String FILE_DECLASSIFICATION_DATE = PREFIX + "file-declassification-date";

  public static final String FILE_DOWNGRADE = PREFIX + "file-downgrade";

  public static final String FILE_DOWNGRADE_DATE = PREFIX + "file-downgrade-date";

  public static final String FILE_CLASSIFICATION_TEXT = PREFIX + "file-classification-text";

  public static final String FILE_CLASSIFICATION_AUTHORITY_TYPE =
      PREFIX + "file-classification-authority-type";

  public static final String FILE_CLASSIFICATION_AUTHORITY =
      PREFIX + "file-classification-authority";

  public static final String FILE_CLASSIFICATION_REASON = PREFIX + "file-classification-reason";

  public static final String FILE_SECURITY_SOURCE_DATE = PREFIX + "file-security-source-date";

  public static final String FILE_SECURITY_CONTROL_NUMBER = PREFIX + "file-security-control-number";

  public static final String FILE_COPY_NUMBER = PREFIX + "file-copy-number";

  public static final String FILE_NUMBER_OF_COPIES = PREFIX + "file-number-of-copies";

  public static final String FILE_BACKGROUND_COLOR = PREFIX + "file-background-color";

  public static final String NITF = "NITF";

  public static final String NSIF = "NSIF";

  public static final String TWO_ONE = "2.1";

  public static final String TWO_ZERO = "2.0";

  public static final String ONE_ZERO = "1.0";

  private static final List<NitfAttribute<NitfHeader>> ATTRIBUTES = new LinkedList<>();

  /*
   * Normalized attributes. These taxonomy terms will be duplicated by `ext.nitf.*` when appropriate.
   */

  public static final NitfHeaderAttribute FILE_PROFILE_NAME_ATTRIBUTE =
      new NitfHeaderAttribute(
          Media.FORMAT,
          "FHDR",
          header -> convertFormat(header.getFileType()),
          new MediaAttributes().getAttributeDescriptor(Media.FORMAT),
          FILE_PROFILE_NAME);

  public static final NitfHeaderAttribute FILE_VERSION_ATTRIBUTE =
      new NitfHeaderAttribute(
          Media.FORMAT_VERSION,
          "FVER",
          header -> convertFormatVersion(header.getFileType()),
          new MediaAttributes().getAttributeDescriptor(Media.FORMAT_VERSION),
          FILE_VERSION);

  public static final NitfHeaderAttribute ORIGINATING_STATION_ID_ATTRIBUTE =
      new NitfHeaderAttribute(
          Isr.ORGANIZATIONAL_UNIT,
          "OSTAID",
          NitfHeader::getOriginatingStationId,
          new IsrAttributes().getAttributeDescriptor(Isr.ORGANIZATIONAL_UNIT),
          ORIGINATING_STATION_ID);

  public static final NitfHeaderAttribute FILE_TITLE_ATTRIBUTE =
      new NitfHeaderAttribute(
          Core.TITLE,
          "FTITLE",
          NitfHeader::getFileTitle,
          new CoreAttributes().getAttributeDescriptor(Core.TITLE),
          FILE_TITLE);

  public static final NitfHeaderAttribute FILE_DATE_AND_TIME_CREATED_ATTRIBUTE =
      new NitfHeaderAttribute(
          Core.CREATED,
          "FDT",
          header -> NitfAttributeConverters.nitfDate(header.getFileDateTime()),
          new CoreAttributes().getAttributeDescriptor(Core.CREATED),
          FILE_DATE_AND_TIME);

  public static final NitfHeaderAttribute FILE_DATE_AND_TIME_MODIFIED_ATTRIBUTE =
      new NitfHeaderAttribute(
          Core.MODIFIED,
          "FDT",
          header -> NitfAttributeConverters.nitfDate(header.getFileDateTime()),
          new CoreAttributes().getAttributeDescriptor(Core.MODIFIED),
          "");

  public static final NitfHeaderAttribute FILE_DATE_AND_TIME_EFFECTIVE_ATTRIBUTE =
      new NitfHeaderAttribute(
          Metacard.EFFECTIVE,
          "FDT",
          header -> NitfAttributeConverters.nitfDate(header.getFileDateTime()),
          new AttributeDescriptorImpl(
              Metacard.EFFECTIVE,
              true, /* indexed */
              true, /* stored */
              false, /* tokenized */
              false, /* multivalued */
              BasicTypes.DATE_TYPE));

  public static final NitfHeaderAttribute FILE_SECURITY_CLASSIFICATION_ATTRIBUTE =
      new NitfHeaderAttribute(
          Security.CLASSIFICATION,
          "FSCLAS",
          header -> header.getFileSecurityMetadata().getSecurityClassification().name(),
          new SecurityAttributes().getAttributeDescriptor(Security.CLASSIFICATION),
          FILE_SECURITY_CLASSIFICATION);

  public static final NitfHeaderAttribute FILE_CLASSIFICATION_SECURITY_SYSTEM_ATTRIBUTE =
      new NitfHeaderAttribute(
          Security.CLASSIFICATION_SYSTEM,
          "FSCLSY",
          header ->
              NitfAttributeConverters.fipsToStandardCountryCode(
                  header.getFileSecurityMetadata().getSecurityClassificationSystem()),
          new SecurityAttributes().getAttributeDescriptor(Security.CLASSIFICATION_SYSTEM));

  public static final NitfHeaderAttribute FILE_CODE_WORDS_ATTRIBUTE =
      new NitfHeaderAttribute(
          Security.CODEWORDS,
          "FSCODE",
          header -> header.getFileSecurityMetadata().getCodewords(),
          new SecurityAttributes().getAttributeDescriptor(Security.CODEWORDS),
          FILE_CODEWORDS);

  public static final NitfHeaderAttribute FILE_CONTROL_AND_HANDLING_ATTRIBUTE =
      new NitfHeaderAttribute(
          Security.DISSEMINATION_CONTROLS,
          "FSCTLH",
          header -> header.getFileSecurityMetadata().getControlAndHandling(),
          new SecurityAttributes().getAttributeDescriptor(Security.DISSEMINATION_CONTROLS),
          FILE_CONTROL_AND_HANDLING);

  public static final NitfHeaderAttribute FILE_RELEASING_INSTRUCTIONS_ATTRIBUTE =
      new NitfHeaderAttribute(
          Security.RELEASABILITY,
          "FSREL",
          header -> handleReleasability(header.getFileSecurityMetadata().getReleaseInstructions()),
          new SecurityAttributes().getAttributeDescriptor(Security.RELEASABILITY));

  public static final NitfHeaderAttribute ORIGINATORS_NAME_ATTRIBUTE =
      new NitfHeaderAttribute(
          Contact.CREATOR_NAME,
          "ONAME",
          NitfHeader::getOriginatorsName,
          new ContactAttributes().getAttributeDescriptor(Contact.CREATOR_NAME),
          ORIGINATORS_NAME);

  public static final NitfHeaderAttribute ORIGINATORS_PHONE_NUMBER_ATTRIBUTE =
      new NitfHeaderAttribute(
          Contact.CREATOR_PHONE,
          "OPHONE",
          NitfHeader::getOriginatorsPhoneNumber,
          new ContactAttributes().getAttributeDescriptor(Contact.CREATOR_PHONE),
          ORIGINATORS_PHONE_NUMBER);

  /*
   * Non-normalized attributes
   */

  public static final NitfHeaderAttribute EXT_FILE_CLASSIFICATION_SECURITY_SYSTEM_ATTRIBUTE =
      new NitfHeaderAttribute(
          FILE_CLASSIFICATON_SECURITY_SYSTEM,
          "FSCLSY",
          header -> header.getFileSecurityMetadata().getSecurityClassificationSystem(),
          BasicTypes.STRING_TYPE);

  public static final NitfHeaderAttribute EXT_FILE_RELEASING_INSTRUCTIONS_ATTRIBUTE =
      new NitfHeaderAttribute(
          FILE_RELEASING_INSTRUCTIONS,
          "FSREL",
          header -> header.getFileSecurityMetadata().getReleaseInstructions(),
          BasicTypes.STRING_TYPE);

  public static final NitfHeaderAttribute COMPLEXITY_LEVEL_ATTRIBUTE =
      new NitfHeaderAttribute(
          COMPLEXITY_LEVEL, "CLEVEL", NitfHeader::getComplexityLevel, BasicTypes.INTEGER_TYPE);

  public static final NitfHeaderAttribute STANDARD_TYPE_ATTRIBUTE =
      new NitfHeaderAttribute(
          STANDARD_TYPE, "STYPE", NitfHeader::getStandardType, BasicTypes.STRING_TYPE);

  public static final NitfHeaderAttribute FILE_DECLASSIFICATION_EXEMPTION_ATTRIBUTE =
      new NitfHeaderAttribute(
          FILE_DECLASSIFICATION_EXEMPTION,
          "FSDCXM",
          header -> header.getFileSecurityMetadata().getDeclassificationExemption(),
          BasicTypes.STRING_TYPE);

  public static final NitfHeaderAttribute FILE_DECLASSIFICATION_TYPE_ATTRIBUTE =
      new NitfHeaderAttribute(
          FILE_DECLASSIFICATION_TYPE,
          "FSDCTP",
          header -> header.getFileSecurityMetadata().getDeclassificationType(),
          BasicTypes.STRING_TYPE);

  public static final NitfHeaderAttribute FILE_DECLASSIFICATION_DATE_ATTRIBUTE =
      new NitfHeaderAttribute(
          FILE_DECLASSIFICATION_DATE,
          "FSDCDT",
          header -> header.getFileSecurityMetadata().getDeclassificationDate(),
          BasicTypes.STRING_TYPE);

  public static final NitfHeaderAttribute FILE_DOWNGRADE_ATTRIBUTE =
      new NitfHeaderAttribute(
          FILE_DOWNGRADE,
          "FSDG",
          header -> header.getFileSecurityMetadata().getDowngrade(),
          BasicTypes.STRING_TYPE);

  public static final NitfHeaderAttribute FILE_DOWNGRADE_DATE_ATTRIBUTE =
      new NitfHeaderAttribute(
          FILE_DOWNGRADE_DATE,
          "FSDGDT",
          header -> header.getFileSecurityMetadata().getDowngradeDate(),
          BasicTypes.STRING_TYPE);

  public static final NitfHeaderAttribute FILE_CLASSIFICATION_TEXT_ATTRIBUTE =
      new NitfHeaderAttribute(
          FILE_CLASSIFICATION_TEXT,
          "FSCLTX",
          header -> header.getFileSecurityMetadata().getClassificationText(),
          BasicTypes.STRING_TYPE);

  public static final NitfHeaderAttribute FILE_CLASSIFICATION_AUTHORITY_TYPE_ATTRIBUTE =
      new NitfHeaderAttribute(
          FILE_CLASSIFICATION_AUTHORITY_TYPE,
          "FSCATP",
          header -> header.getFileSecurityMetadata().getClassificationAuthorityType(),
          BasicTypes.STRING_TYPE);

  public static final NitfHeaderAttribute FILE_CLASSIFICATION_AUTHORITY_ATTRIBUTE =
      new NitfHeaderAttribute(
          FILE_CLASSIFICATION_AUTHORITY,
          "FSCAUT",
          header -> header.getFileSecurityMetadata().getClassificationAuthority(),
          BasicTypes.STRING_TYPE);

  public static final NitfHeaderAttribute FILE_CLASSIFICATION_REASON_ATTRIBUTE =
      new NitfHeaderAttribute(
          FILE_CLASSIFICATION_REASON,
          "FSCRSN",
          header -> header.getFileSecurityMetadata().getClassificationReason(),
          BasicTypes.STRING_TYPE);

  public static final NitfHeaderAttribute FILE_SECURITY_SOURCE_DATE_ATTRIBUTE =
      new NitfHeaderAttribute(
          FILE_SECURITY_SOURCE_DATE,
          "FSSRDT",
          header -> header.getFileSecurityMetadata().getSecuritySourceDate(),
          BasicTypes.STRING_TYPE);

  public static final NitfHeaderAttribute FILE_SECURITY_CONTROL_NUMBER_ATTRIBUTE =
      new NitfHeaderAttribute(
          FILE_SECURITY_CONTROL_NUMBER,
          "FSCTLN",
          header -> header.getFileSecurityMetadata().getSecurityControlNumber(),
          BasicTypes.STRING_TYPE);

  public static final NitfHeaderAttribute FILE_COPY_NUMBER_ATTRIBUTE =
      new NitfHeaderAttribute(
          FILE_COPY_NUMBER,
          "FSCOP",
          header -> header.getFileSecurityMetadata().getFileCopyNumber(),
          BasicTypes.STRING_TYPE);

  public static final NitfHeaderAttribute FILE_NUMBER_OF_COPIES_ATTRIBUTE =
      new NitfHeaderAttribute(
          FILE_NUMBER_OF_COPIES,
          "FSCPYS",
          header -> header.getFileSecurityMetadata().getFileNumberOfCopies(),
          BasicTypes.STRING_TYPE);

  public static final NitfHeaderAttribute FILE_BACKGROUND_COLOR_ATTRIBUTE =
      new NitfHeaderAttribute(
          FILE_BACKGROUND_COLOR,
          "FBKGC",
          header ->
              header.getFileBackgroundColour() != null
                  ? header.getFileBackgroundColour().toString()
                  : "",
          BasicTypes.STRING_TYPE);

  private NitfHeaderAttribute(
      String longName,
      String shortName,
      Function<NitfHeader, Serializable> accessorFunction,
      AttributeType attributeType) {
    super(longName, shortName, accessorFunction, attributeType);
    ATTRIBUTES.add(this);
  }

  private NitfHeaderAttribute(
      String longName,
      String shortName,
      Function<NitfHeader, Serializable> accessorFunction,
      AttributeDescriptor attributeDescriptor) {
    super(longName, shortName, accessorFunction, attributeDescriptor, "");
    ATTRIBUTES.add(this);
  }

  private NitfHeaderAttribute(
      String longName,
      String shortName,
      Function<NitfHeader, Serializable> accessorFunction,
      AttributeDescriptor attributeDescriptor,
      String extNitfName) {
    super(longName, shortName, accessorFunction, attributeDescriptor, extNitfName);
    ATTRIBUTES.add(this);
  }

  private static String convertFormat(final FileType format) {
    if (format == FileType.NITF_TWO_ONE || format == FileType.NITF_TWO_ZERO) {
      return NITF;
    } else if (format == FileType.NSIF_ONE_ZERO) {
      return NSIF;
    }

    return "";
  }

  private static String convertFormatVersion(final FileType format) {
    if (format == FileType.NITF_TWO_ONE) {
      return TWO_ONE;
    } else if (format == FileType.NITF_TWO_ZERO) {
      return TWO_ZERO;
    } else if (format == FileType.NSIF_ONE_ZERO) {
      return ONE_ZERO;
    }

    return "";
  }

  private static Serializable handleReleasability(final String nitfReleaseInstructions) {
    if (StringUtils.isEmpty(nitfReleaseInstructions)) {
      return "";
    }

    String[] fipsCountryCodes = nitfReleaseInstructions.split(" ");
    return Stream.of(fipsCountryCodes)
        .map(NitfAttributeConverters::fipsToStandardCountryCode)
        .filter(Objects::nonNull)
        .distinct()
        .collect(Collectors.joining(" "));
  }

  public static List<NitfAttribute<NitfHeader>> getAttributes() {
    return Collections.unmodifiableList(ATTRIBUTES);
  }
}
