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
package org.codice.alliance.nsili.common.datamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.codice.alliance.nsili.common.GIAS.AttributeInformation;
import org.codice.alliance.nsili.common.GIAS.AttributeType;
import org.codice.alliance.nsili.common.GIAS.DateRange;
import org.codice.alliance.nsili.common.GIAS.Domain;
import org.codice.alliance.nsili.common.GIAS.FloatingPointRange;
import org.codice.alliance.nsili.common.GIAS.IntegerRange;
import org.codice.alliance.nsili.common.GIAS.RequirementMode;
import org.codice.alliance.nsili.common.NsiliApprovalStatus;
import org.codice.alliance.nsili.common.NsiliCardStatus;
import org.codice.alliance.nsili.common.NsiliCbrnAlarmClassification;
import org.codice.alliance.nsili.common.NsiliCbrnEvent;
import org.codice.alliance.nsili.common.NsiliClassification;
import org.codice.alliance.nsili.common.NsiliConstants;
import org.codice.alliance.nsili.common.NsiliCxpStatusType;
import org.codice.alliance.nsili.common.NsiliEntityType;
import org.codice.alliance.nsili.common.NsiliExploitationSubQualCode;
import org.codice.alliance.nsili.common.NsiliImageryDecompressionTech;
import org.codice.alliance.nsili.common.NsiliImageryType;
import org.codice.alliance.nsili.common.NsiliMetadataEncodingScheme;
import org.codice.alliance.nsili.common.NsiliProductType;
import org.codice.alliance.nsili.common.NsiliRelationship;
import org.codice.alliance.nsili.common.NsiliReportPriority;
import org.codice.alliance.nsili.common.NsiliReportType;
import org.codice.alliance.nsili.common.NsiliRfiStatus;
import org.codice.alliance.nsili.common.NsiliRfiWorkflowStatus;
import org.codice.alliance.nsili.common.NsiliScanningMode;
import org.codice.alliance.nsili.common.NsiliSdsOpStatus;
import org.codice.alliance.nsili.common.NsiliSituationType;
import org.codice.alliance.nsili.common.NsiliStreamStandard;
import org.codice.alliance.nsili.common.NsiliTaskStatus;
import org.codice.alliance.nsili.common.NsiliVideoCategoryType;
import org.codice.alliance.nsili.common.NsiliVideoEncodingScheme;
import org.codice.alliance.nsili.common.UCO.AbsTime;
import org.codice.alliance.nsili.common.UCO.Coordinate2d;
import org.codice.alliance.nsili.common.UCO.Date;
import org.codice.alliance.nsili.common.UCO.Rectangle;
import org.codice.alliance.nsili.common.UCO.Time;

public class NsiliAttributesGenerator {
  private static final AbsTime EARLIEST =
      new AbsTime(
          new Date((short) 1970, (short) 1, (short) 1), new Time((short) 18, (short) 0, (short) 0));

  private static final AbsTime LATEST =
      new AbsTime(
          new Date((short) 2020, (short) 1, (short) 1), new Time((short) 18, (short) 0, (short) 0));

  private static final DateRange DATE_RANGE = new DateRange(EARLIEST, LATEST);

  private static final IntegerRange NUM_OF_PARTS_RANGE = new IntegerRange(0, 99999);

  private static final IntegerRange POSITIVE_INT_RANGE = new IntegerRange(0, Integer.MAX_VALUE);

  private static final IntegerRange PERCENT_INT_RANGE = new IntegerRange(0, 100);

  private static final FloatingPointRange MAX_STANAG_4559_RANGE =
      new FloatingPointRange(0, 3 * Math.pow(10, 38));

  private static final Rectangle RECTANGLE_DOMAIN =
      new Rectangle(new Coordinate2d(-180.0, 90.0), new Coordinate2d(180.0, -90.0));

  public static List<AttributeInformation> getNsilCardAttributes() {
    String prefix = NsiliConstants.NSIL_CARD + ".";
    Domain domain;

    List<AttributeInformation> attributes = new ArrayList<>();

    domain = new Domain();
    domain.t(50);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.IDENTIFIER,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Alphanumeric identification code associated with the product. The code must universally uniquely identify the product.",
            true,
            false));

    domain = new Domain();
    domain.d(DATE_RANGE);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.SOURCE_DATE_TIME_MODIFIED,
            AttributeType.UCOS_ABS_TIME,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "The date and time upon which information (cataloguing metadata) about the product was last stored or changed in the source IPL. For any STANAG 4559 Edition 3 server that is not the source of the product, but which have received this product through synchronization, the value of this attribute shall stay unchanged as it is in the source STANAG 4559 server. This then means that the date-time value represent the instant in time when the product was uploaded or changed at the product-source STANAG 4559 Server.",
            true,
            false));

    domain = new Domain();
    domain.d(DATE_RANGE);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.DATE_TIME_MODIFIED,
            AttributeType.UCOS_ABS_TIME,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "The date and time upon which information (cataloguing metadata) about the product was last stored or changed in the local IPL. This attribute is used when synchronizing metadata, to capture when the metadata was synchronized (and not when the metadata was created at the source STANAG 4559 Server). For local products, not received through synchronization, the value of this attribute shall be identical to the value of the 'sourceDateTimeModified' attribute.",
            true,
            false));

    domain = new Domain();
    domain.l(getNsilCardStatusOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.STATUS,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "The status of the product metadata indicating if the metadata entry is new, has been changed or is marked as obsolete. Note: 1. The Library should remove the URL (productURL) when it sets the status to 'OBSOLETE'. Also the ordering of an obsolete product via OrderMgr should not be allowed. 2. A metadata entry should never be removed. 3. The product can never be modified after it has been posted to a library. The 'CHANGED' status refers to the metadata only.",
            true,
            false));

    domain = new Domain();
    domain.ir(NUM_OF_PARTS_RANGE);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.NUM_OF_PARTS,
            AttributeType.INTEGER,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "A number specifying the number of parts belonging to the product",
            true,
            false));

    domain = new Domain();
    domain.t(30);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.PUBLISHER,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "The name of the organization responsible for making the resource (product) available in an IPL. By doing so the publisher enables the discovery of that resource by a requestor entity (client). Examples of organizations are 'CJTF', 'LCC', 'ACC' etc.",
            true,
            true));

    domain = new Domain();
    domain.t(30);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.SOURCE_LIBRARY,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Alphanumeric identification code of the library in which the product was initially ingested. This identifier is NOT identifying the library the client is currently connected to.",
            true,
            false));

    return attributes;
  }

  public static List<AttributeInformation> getNsilCommonAttributes() {
    String prefix = NsiliConstants.NSIL_COMMON + ".";
    Domain domain;

    List<AttributeInformation> attributes = new ArrayList<>();

    domain = new Domain();
    domain.t(800);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.DESCRIPTION_ABSTRACT,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "A summary of the content of the resource (product).",
            false,
            true));

    domain = new Domain();
    domain.t(40);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.IDENTIFIER_MISSION,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "An alphanumeric identifier that identifies the mission (e.g. a reconnaissance mission) under which the product was collected/generated. As an example, for products collected by sensors on an aircraft, the mission identifier should be the 'Mission Number' from the Air Tasking Order (ATO).",
            false,
            true));

    domain = new Domain();
    domain.t(36);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.IDENTIFIER_UUID,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "A Universally Unique Identifier (UUID) is an identifier (standardized by the Open Software Foundation (OSF)) that can be generated by systems to uniquely identify information without any central coordination. Thus, anyone can create a UUID and use it to identify something with reasonable confidence that the identifier will never be unintentionally used by anyone for anything else.",
            false,
            true));

    domain = new Domain();
    domain.ir(POSITIVE_INT_RANGE);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.IDENTIFIER_JC3IEDM,
            AttributeType.INTEGER,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "An additional identifier that can be used for cross-referencing into peripherally accessed JC3IEDM databases.",
            false,
            true));

    domain = new Domain();
    domain.t(12);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.LANGUAGE,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "A list of 3 character language codes according to ISO 639-2 for the intellectual content of the resource. Multiple languages are separated by BCS Comma (code 0x2C). Note: There are two codes for special situations: * mul (for multiple languages) should be applied when several languages are used and it is not practical to specify all the appropriate language codes. * und (for undetermined) is provided for those situations in which a language or languages must be indicated but the language cannot be identified.",
            false,
            true));

    domain = new Domain();
    domain.t(200);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.SOURCE,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "References to assets (e.g. platform IDs) from which the tagged data asset is derived. Sources may be derived partially or wholly, and it is recommended that an identifier (such as a string or number from a formal identification system) be used as a reference. In case of multiple sources these shall be separated by BCS Comma (could happen for container files like AAF, MXF and NSIF). 'Source' is different from 'creator' in that the 'source' entity is the provider of the data content, while the 'creator' is the entity responsible for assembling the data (provided by \"sources\") into a file.",
            false,
            true));

    domain = new Domain();
    domain.t(50);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.SUBJECT_CATEGORY_TARGET,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "A target category from STANAG 3596 that should reflect the main subject of the resource. In case of multiple categories these shall be separated by the BCS Comma (code 0x2C) character.",
            false,
            true));

    domain = new Domain();
    domain.t(159);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.TARGET_NUMBER,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "This field shall contain the identification of the primary target in the product. In case of multiple target identifiers these shall be separated by the BCS Comma (code 0x2C) character.",
            false,
            true));

    domain = new Domain();
    domain.l(getNsilTypeOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.TYPE,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "The nature or genre of the content of the resource.",
            false,
            true));

    domain = new Domain();
    domain.t(200);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.KEYWORDS,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "A comma delimited list of keywords providing additional information to enable an operator to query the STANAG 4559 library by keywords.",
            false,
            true));

    return attributes;
  }

  public static List<AttributeInformation> getNsilCoverageAttributes() {
    String prefix = NsiliConstants.NSIL_COVERAGE + ".";
    Domain domain;

    List<AttributeInformation> attributes = new ArrayList<>();

    domain = new Domain();
    domain.t(20);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.SPATIAL_COUNTRY_CODE,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "List of standards-based abbreviation (three character codes) of a country names describing the area over which the product was collected separated by BCS Comma (code 0x2C). The three character code shall be according to STANAG 1059.",
            false,
            true));

    domain = new Domain();
    domain.g(RECTANGLE_DOMAIN);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.SPATIAL_GEOGRAPHIC_REF_BOX,
            AttributeType.UCOS_RECTANGLE,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Geographic location of the dataset. Always in WGS-84 reference system, and using decimal degrees. The first coordinate represents the most North-Western corner, the second the most South-Eastern corner. The x-value in a UCOS: Coordinate2D struct represents the longitude, the y-value represents the latitude.",
            false,
            true));

    domain = new Domain();
    domain.t(2048);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.ADVANCED_GEOSPATIAL,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Allows storing advanced geometry for display purposes only. The geometry must be encoded using the OGC Well-Known Text (WKT) format [ISO/IEC 13429-3:2011] and validation may be performed by the STANAG 4559 library.",
            false,
            true));

    domain = new Domain();
    domain.d(DATE_RANGE);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.TEMPORAL_END,
            AttributeType.UCOS_ABS_TIME,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "End time of a period of time for the content of the dataset (end time of content acquisition). For products capturing a single instant in time, start time and end time will be equal (or the end time could be omitted)",
            false,
            true));

    domain = new Domain();
    domain.d(DATE_RANGE);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.TEMPORAL_START,
            AttributeType.UCOS_ABS_TIME,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Start time of a period of time for the content of the dataset (start time of content acquisition). For products capturing a single instant in time, start time and end time will be equal (or the end time could be omitted).",
            false,
            true));

    return attributes;
  }

  public static List<AttributeInformation> getNsilFileAttributes() {
    String prefix = NsiliConstants.NSIL_FILE + ".";
    Domain domain;

    List<AttributeInformation> attributes = new ArrayList<>();

    domain = new Domain();
    domain.bv(false);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.ARCHIVED,
            AttributeType.BOOLEAN_DATA,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Indicates whether the file has been archived. Set to \"YES\" if the file has been archived, otherwise set to \"NO\".",
            true,
            false));

    domain = new Domain();
    domain.t(200);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.ARCHIVE_INFORMATION,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Additional archiving information in free text form allowing the archived data (product files) to be found and possibly be restored to the server storage. This information could contain the date when the data was archived and point of contact for retrieving the archived data.",
            true,
            true));

    domain = new Domain();
    domain.t(200);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.CREATOR,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "An entity primarily responsible for making the content of the resource. Creator is responsible for the intellectual or creative content of the\n"
                + "ANNEX G to STANAG 4559 Edition 3\n"
                + " \n"
                + "resource. With raw sensor products a creator is the unit that runs the sensor. With exploited products it is the exploitation station, with an Information Request (IR) it is an IR Management system/service.",
            true,
            true));

    domain = new Domain();
    domain.d(DATE_RANGE);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.DATE_TIME_DECLARED,
            AttributeType.UCOS_ABS_TIME,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Date and time on which the resource was declared, filed or stored (the date the content of the product (dataset) was compiled).",
            true,
            true));

    domain = new Domain();
    domain.fr(MAX_STANAG_4559_RANGE);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.EXTENT,
            AttributeType.FLOATING_POINT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "(Estimated) size of product in the specified transfer format, expressed in\n"
                + "Table G- 26 NSIL_FILE.format\n"
                + "ANNEX G to STANAG 4559 Edition 3\n"
                + " \n"
                + "megabytes. The transfer size is > 0.0.",
            true,
            false));

    domain = new Domain();
    domain.t(50);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.FORMAT,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "The MIME type for the product object to which this metadata applies with the necessary extension to cater for ISR specific file types not yet MIME registered.",
            true,
            false));

    domain = new Domain();
    domain.t(10);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.FORMAT_VERSION,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Version of product format. E.g. \"1.0\". In case of an XML document this attribute refers to the version of the XML schema that the document refers to (and not to the version of the XML format).",
            true,
            false));

    domain = new Domain();
    domain.t(0);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.PRODUCT_URL,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "The address and access protocol in the form of an URL by which a user can directly access a product without the user placing an order for delivery. Note, This attribute is not queryable.",
            true,
            false));

    domain = new Domain();
    domain.t(100);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.TITLE,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Product (dataset) title.",
            true,
            true));

    domain = new Domain();
    domain.bv(true);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.IS_PRODUCT_LOCAL,
            AttributeType.BOOLEAN_DATA,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Indicates whether the product file is available locally in the library, or resides in a remote library.",
            true,
            false));

    domain = new Domain();
    domain.t(256);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.FILENAME,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "A string item providing the original file name of the product.",
            true,
            true));

    return attributes;
  }

  public static List<AttributeInformation> getNsilGmtiAttributes() {
    String prefix = NsiliConstants.NSIL_GMTI + ".";
    Domain domain;

    List<AttributeInformation> attributes = new ArrayList<>();

    domain = new Domain();
    domain.fr(new FloatingPointRange(0.0, 4294967295.0));
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.IDENTIFIER_JOB,
            AttributeType.FLOATING_POINT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "A platform-assigned number identifying the specific requestor task to which the packet pertains. The Job ID shall be unique within a mission.",
            false,
            true));

    domain = new Domain();
    domain.ir(POSITIVE_INT_RANGE);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.NUMBER_OF_TARGET_REPORTS,
            AttributeType.INTEGER,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "The total number of target reports within all the dwells in the file.",
            false,
            true));

    return attributes;
  }

  public static List<AttributeInformation> getNsilImageryAttributes() {
    String prefix = NsiliConstants.NSIL_IMAGERY + ".";
    Domain domain;

    List<AttributeInformation> attributes = new ArrayList<>();

    domain = new Domain();
    domain.l(getImageryCategoryOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.CATEGORY,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "A valid indicator of the specific category of image, raster, or grid data. The specific category of an image reveals its intended use or the nature of its collector.",
            false,
            true));

    domain = new Domain();
    domain.ir(PERCENT_INT_RANGE);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.CLOUD_COVER_PCT,
            AttributeType.INTEGER,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "A code that indicates the percentage of the image obscured by cloud cover.",
            false,
            true));

    domain = new Domain();
    domain.t(800);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.COMMENTS,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Comments related to the image",
            false,
            true));

    domain = new Domain();
    domain.l(getImageryDecompressionTechOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.DECOMPRESSION_TECHNIQUE,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "Specification of algorithm or process to apply to read or expand the image to which compression techniques have been applied.",
            false,
            true));

    domain = new Domain();
    domain.t(10);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.IDENTIFIER,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "This field shall contain a valid alphanumeric identification code associated with the image. The valid codes are determined by the application.",
            false,
            true));

    domain = new Domain();
    domain.ir(new IntegerRange(0, 9));
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.NIIRS,
            AttributeType.INTEGER,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "NIIRS - National Image Interpretability Rating Scales (NIIRS) -defines and measures the quality of images and the performance of imaging systems.",
            false,
            true));

    domain = new Domain();
    domain.ir(new IntegerRange(0, 99999));
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.NUMBER_OF_BANDS,
            AttributeType.INTEGER,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "The number of data bands within the specified image.",
            false,
            true));

    domain = new Domain();
    domain.ir(new IntegerRange(1, Integer.MAX_VALUE));
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.NUMBER_OF_ROWS,
            AttributeType.INTEGER,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Number of significant rows in Image. This field shall contain the total number of rows of significant pixels in the image. It could be that an image has been padded with fill data (to fill out the last part of the block in a blocked image). The meaning of 'significant' is then only the rows conveying image data excluding all the rows with padded fill data.",
            false,
            true));

    domain = new Domain();
    domain.ir(new IntegerRange(1, Integer.MAX_VALUE));
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.NUMBER_OF_COLS,
            AttributeType.INTEGER,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Number of significant columns in Image. This field shall contain the total number of columns of significant pixels in the image. It could be that an image has been padded with fill data (to fill out the last part of the block in a blocked image). The meaning of 'significant' is then only the columns conveying image data excluding all the columns with padded fill data.",
            false,
            true));

    domain = new Domain();
    domain.t(80);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.TITLE,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "This field shall contain the title of the image.",
            false,
            true));

    return attributes;
  }

  public static List<AttributeInformation> getNsilMessageAttributes() {
    String prefix = NsiliConstants.NSIL_MESSAGE + ".";
    Domain domain;

    List<AttributeInformation> attributes = new ArrayList<>();

    domain = new Domain();
    domain.t(200);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.RECIPIENT,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "Identify the receiving entity of the message. This will typically be an XMPP conference room identifier (including the fully qualified domain name extension), but could also be an individual/personal XMPP or e- mail account. In the case a message is sent to more than one recipient, this shall be supported by separating the recipients by BCS Comma. Note that the 'from' information is captured in NSIL_COMMON.source",
            false,
            true));

    domain = new Domain();
    domain.t(200);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.SUBJECT,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Data that specifies the topic of the message.",
            false,
            true));

    domain = new Domain();
    domain.t(2048);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.MESSAGE_BODY,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "The body of the message. In case the message body text exceeds the maximum length of this metadata attribute the message text will be truncated. . The complete message is available through the URL in NSIL_FILE entity, but only the characters stored in this attribute can be used for free text search.",
            false,
            true));

    domain = new Domain();
    domain.l(getMessageTypeOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.MESSAGE_TYPE,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "Identification of message type (protocol). Initially only XMPP is recognized.",
            false,
            true));

    return attributes;
  }

  public static List<AttributeInformation> getNsilMetadataSecurityAttributes() {
    String prefix = NsiliConstants.NSIL_METADATA_SECURITY + ".";
    Domain domain;

    List<AttributeInformation> attributes = new ArrayList<>();

    domain = new Domain();
    domain.l(getClassificationOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.CLASSIFICATION,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "NATO Security markings that determine the physical security given to the metadata.",
            true,
            true));

    domain = new Domain();
    domain.t(20);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.POLICY,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "This field shall contain valid values indicating the national or multinational security policies used to classify the metadata. To indicate a national security policy, Country Codes per STANAG 1059 shall be used. In cases where the country is not listed by a three character country code, but as a six character region code, the six character province code from STANAG 1059 shall be used (e.g. Kosovo is listed as a region in Yugoslavia with six character province code YUKM-). In all cases when STANAG 1059 is not sufficient to describe the security policy, additional enumeration labels can be used (e.g. 'NATO/PFP' and 'NATO/EU'). Note: Although STANAG 1059 includes the 'XXN' entry for NATO, the full 'NATO' name shall be used to indicate NATO security policy to avoid any confusion from established and common used terms.",
            true,
            true));

    domain = new Domain();
    domain.t(50);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.RELEASABILITY,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "An additional marking to limit the dissemination of the product metadata information. Typical values include one or more country codes as found in STANAG 1059 separated by the BCS Comma (code 0x2C) character. Default value should be 'NATO'. Note: Although STANAG 1059 includes the 'XXN' entry for NATO, the full 'NATO' name shall be used to indicate NATO releasability to avoid any confusion from established and common used terms.",
            true,
            true));

    return attributes;
  }

  public static List<AttributeInformation> getNsilPartAttributes() {
    String prefix = NsiliConstants.NSIL_PART + ".";
    Domain domain;

    List<AttributeInformation> attributes = new ArrayList<>();

    domain = new Domain();
    domain.t(0);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.PART_IDENTIFIER,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "String describing the part of the containing Product.",
            false,
            true));

    return attributes;
  }

  public static List<AttributeInformation> getNsilRelatedFileAttributes() {
    String prefix = NsiliConstants.NSIL_RELATED_FILE + ".";
    Domain domain;

    List<AttributeInformation> attributes = new ArrayList<>();

    domain = new Domain();
    domain.t(200);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.CREATOR,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "An entity primarily responsible for making the content of the related file. Creator is responsible for the intellectual or creative content of the resource.",
            true,
            true));

    domain = new Domain();
    domain.d(DATE_RANGE);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.DATE_TIME_DECLARED,
            AttributeType.UCOS_ABS_TIME,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "Date and time on which the related file was declared, filed or stored (the date the content of the related file was compiled).",
            true,
            true));

    domain = new Domain();
    domain.fr(MAX_STANAG_4559_RANGE);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.EXTENT,
            AttributeType.FLOATING_POINT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "(Estimated) size of the file, expressed in megabytes. The transfer size is > 0.0.",
            true,
            false));

    domain = new Domain();
    domain.t(0);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.FILE_TYPE,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "The related file type.",
            false,
            false));

    domain = new Domain();
    domain.t(0);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.URL,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "An unambiguous reference (in form of an URL) to the related file.",
            false,
            false));

    domain = new Domain();
    domain.bv(true);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.IS_FILE_LOCAL,
            AttributeType.BOOLEAN_DATA,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Indicates whether the related file is available locally in the library, or resides in a remote library.",
            true,
            false));

    return attributes;
  }

  public static List<AttributeInformation> getNsilRelationAttributes() {
    String prefix = NsiliConstants.NSIL_RELATION + ".";
    Domain domain;

    List<AttributeInformation> attributes = new ArrayList<>();

    domain = new Domain();
    domain.t(0);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.AMPLIFICATION,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Additional information on the association. This could be used to add additional information for very specific associations. This information is set through the 'assoc_info' parameter of 'create_association' method, where name=\"amplification\".",
            true,
            true));

    domain = new Domain();
    domain.t(30);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.CONTRIBUTOR,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "An entity responsible for contributing the reference. This will be removed in a later edition.",
            true,
            true));

    domain = new Domain();
    domain.d(DATE_RANGE);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.DATE_TIME_DECLARED,
            AttributeType.UCOS_ABS_TIME,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Date and time when the relationship was created. Note that this value must not change when synchronizing the metadata to another library.",
            true,
            false));

    domain = new Domain();
    domain.t(255);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.DESCRIPTION,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Further describes or explains the relationship between the products. This information is set through the 'assoc_info' parameter of 'create_association' method, where name=\"description\"",
            true,
            true));

    domain = new Domain();
    domain.l(getRelationshipOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.RELATIONSHIP,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Defines the kind of relationship between the described product and the associated product. This value is specified by the 'assoc_name' parameter of 'create_association' method.",
            true,
            true));

    return attributes;
  }

  public static List<AttributeInformation> getNsilSecurityAttributes() {
    String prefix = NsiliConstants.NSIL_SECURITY + ".";
    Domain domain;

    List<AttributeInformation> attributes = new ArrayList<>();

    domain = new Domain();
    domain.l(getClassificationOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.CLASSIFICATION,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "NATO Security markings that determine the physical security given to the information in storage and transmission, its circulation, destruction and the personnel security clearance required for access as required by [C- M(2002)49].",
            false,
            true));

    domain = new Domain();
    domain.t(20);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.POLICY,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "This field shall contain valid values indicating the national or multinational security policies used to classify the Product. To indicate a national security policy Country Codes per STANAG 1059 shall be used. In cases where the country is not listed by a three character country code, but as a six character region code, the six character province code from STANAG 1059 shall be used (e.g. Kosovo is listed as a region in Yugoslavia with six character province code YUKM-). In all other cases when STANAG 1059 is not sufficient to describe the security policy, additional enumeration labels could be used (e.g. 'NATO/PFP' and 'NATO/EU'). Note: Although STANAG 1059 includes the 'XXN' entry for NATO, the full 'NATO' name shall be used to indicate NATO security policy to avoid any confusion from established and common used terms.",
            false,
            true));

    domain = new Domain();
    domain.t(50);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.RELEASABILITY,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "An additional marking to further limit the dissemination of classified information in accordance with [C-M (2002)49]. Values include one or more three character country codes as found in STANAG 1059 separated by a single BCS Comma (code 0x2C). Default value should be NATO. Note: Although STANAG 1059 includes the 'XXN' entry for NATO, the full 'NATO' name shall be used to indicate NATO releasability to avoid any confusion from established and common used terms.",
            false,
            true));

    return attributes;
  }

  public static List<AttributeInformation> getNsilStreamAttributes() {
    String prefix = NsiliConstants.NSIL_STREAM + ".";
    Domain domain;

    List<AttributeInformation> attributes = new ArrayList<>();

    domain = new Domain();
    domain.bv(false);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.ARCHIVED,
            AttributeType.BOOLEAN_DATA,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Indicates whether the stream has been archived. Set to \"YES\" if the file has been archived, otherwise set to \"NO\".",
            true,
            true));

    domain = new Domain();
    domain.t(200);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.ARCHIVE_INFORMATION,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Additional archiving information in free text form allowing the archived data (stream data) to be found and possibly be restored to the server storage. This information could contain the date when the data was archived and point of contact for retrieving the archived data.",
            true,
            true));

    domain = new Domain();
    domain.t(200);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.CREATOR,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "An entity responsible for providing the stream. Creator is responsible for the intellectual or creative content of the stream.",
            true,
            true));

    domain = new Domain();
    domain.d(DATE_RANGE);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.DATE_TIME_DECLARED,
            AttributeType.UCOS_ABS_TIME,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "Date and time on which the stream was declared, filed or stored.",
            true,
            true));

    domain = new Domain();
    domain.l(getNsilStreamStandardOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.STANDARD,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "The standard that the streamed data complies with.",
            false,
            true));

    domain = new Domain();
    domain.t(10);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.STANDARD_VERSION,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Version of product standard E.g. \"1.0\".",
            true,
            true));

    domain = new Domain();
    domain.t(0);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.SOURCE_URL,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "The address and access protocol in the form of an URL by which a user can directly access the data stream. For RTSP, HTTP, HTTPS and JPIP a standard URL syntax will be used (could include port number). For UDP/RTP, a URL notation using a '@-character-notaton' is used to allow a URL to define a broadcasted stream or a multicasted stream. A UDP broadcast source on port 8105 would be defined as 'udp://@:8105', a RTP broadcast source would be defined as 'rtp://@:8105'; similarly multicasted UDP/RTP source would be defined as 'udp://@multicast_address:port' or 'udp://@multicast_address:port'. This UDP/RTP @-syntax is identical to what is being used by the VideoLAN VLC application. For the broadcast situation it is assumed that the network takes care about forwarding the broadcasts between the LANs (e.g. using NIRIS) so the stream consumer does not need of the LAN-location of the or origial stream source. Note 1: Contrary to the productURL this attribute is queryable. This would allow clients to only discover streams using protocols that the client can consume. Note 2: When a stream is archived and no longer online, the sourceURL attribute shall be removed.",
            false,
            true));

    domain = new Domain();
    domain.ir(new IntegerRange(3, 16383));
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.PROGRAM_ID,
            AttributeType.INTEGER,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "The program ID determines the stream ID within the Transport Stream that contains the PMT (Program Mapping Table) of the stream. In cases where the Transport Stream would contain more than one stream, this would allow clients to find quickly the program being advertised.",
            false,
            true));

    return attributes;
  }

  public static List<AttributeInformation> getNsilVideoAttributes() {
    String prefix = NsiliConstants.NSIL_VIDEO + ".";
    Domain domain;

    List<AttributeInformation> attributes = new ArrayList<>();

    domain = new Domain();
    domain.fr(MAX_STANAG_4559_RANGE);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.AVG_BIT_RATE,
            AttributeType.FLOATING_POINT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "The bit rate of the motion imagery product when being streamed.",
            false,
            true));

    domain = new Domain();
    domain.l(getVideoCategoryOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.CATEGORY,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "An indicator of the specific category of motion image (information about the bands of visible or infrared region of electromagnetic continuum being used). The specific category of an image reveals its intended use or the nature of its collector.",
            false,
            true));

    domain = new Domain();
    domain.l(getVideoEncodingSchemeOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.ENCODING_SCHEME,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "A code that indicates the manner in which the motion imagery was encoded.",
            false,
            true));

    domain = new Domain();
    domain.fr(MAX_STANAG_4559_RANGE);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.FRAME_RATE,
            AttributeType.FLOATING_POINT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "The standard rate (in frames per second (FPS)) at which the motion imagery was captured. Note: The Motion Imagery Standards Profile identifies several values, such as: 23.98 FPS, 24 FPS, 29.97 FPS, 30 FPS, 59.94 FPS.",
            false,
            true));

    domain = new Domain();
    domain.ir(new IntegerRange(1, Integer.MAX_VALUE));
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.NUMBER_OF_ROWS,
            AttributeType.INTEGER,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "The frame vertical resolution.",
            false,
            true));

    domain = new Domain();
    domain.ir(new IntegerRange(1, Integer.MAX_VALUE));
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.NUMBER_OF_COLS,
            AttributeType.INTEGER,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "The frame horizontal resolution.",
            false,
            true));

    domain = new Domain();
    domain.l(getMetadataEncodingSchemeOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.METADATA_ENC_SCHEME,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "A code that indicates the manner in which the metadata for the motion imagery was encoded.",
            false,
            true));

    domain = new Domain();
    domain.ir(new IntegerRange(0, 12));
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.MISM_LEVEL,
            AttributeType.INTEGER,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "From [AEDP-8]: The \"Motion Imagery Systems (Spatial and Temporal) Matrix\" (MISM) defines an ENGINEERING GUIDELINE for the simple identification of broad categories of Motion Imagery Systems. The intent of the MISM is to give user communities an easy to use, common shorthand reference language to describe the fundamental technical capabilities of NATO motion imagery systems.",
            false,
            true));

    domain = new Domain();
    domain.l(getVideoScanningModeOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.SCANNING_MODE,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Indicate if progressive or interlaced scans are being applied.",
            false,
            true));

    domain = new Domain();
    domain.bv(false);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.VMTI_PROCESSED,
            AttributeType.BOOLEAN_DATA,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "A flag indicating if the video stream or file has been processed by a VMTI processor.",
            false,
            true));

    domain = new Domain();
    domain.ir(new IntegerRange(0, Integer.MAX_VALUE));
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.NUM_VMTI_TGT_REPORTS,
            AttributeType.INTEGER,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "The maximum number of unique detections in any one (1) second interval. For a video stream the numberOfVMTITargetReports shall not be provided a value.",
            false,
            true));

    return attributes;
  }

  public static List<AttributeInformation> getNsilCbrnAttributes() {
    String prefix = NsiliConstants.NSIL_CBRN + ".";
    Domain domain;

    List<AttributeInformation> attributes = new ArrayList<>();

    domain = new Domain();
    domain.t(56);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.OPERATION_NAME,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Exercise Identification or Operation Code Word",
            true,
            true));

    domain = new Domain();
    domain.t(26);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.INCIDENT_NUM,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "Incident Number typically based on the concatenation of ALFA1, ALFA2, ALFA3, and ALFA4.",
            true,
            true));

    domain = new Domain();
    domain.l(getCbrnEventOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.EVENT_TYPE,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "Type of event enumeration description",
            true,
            true));

    domain = new Domain();
    domain.t(100);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.CBRN_CATEGORY,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "The CBRN report type or plot type",
            true,
            true));

    domain = new Domain();
    domain.t(7);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.SUBSTANCE,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Description of substance",
            true,
            true));

    domain = new Domain();
    domain.l(getCbrnAlarmClassificationOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.ALARM_CLASSIFICATION,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Classification of CBRN sensor alarm",
            true,
            true));

    return attributes;
  }

  public static List<AttributeInformation> getNsilApprovalAttributes() {
    String prefix = NsiliConstants.NSIL_APPROVAL + ".";
    Domain domain;

    List<AttributeInformation> attributes = new ArrayList<>();

    domain = new Domain();
    domain.t(30);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.APPROVED_BY,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "The name of the organization responsible for Quality Assurance of the resource (product) available in an IPL.",
            true,
            true));

    domain = new Domain();
    domain.d(DATE_RANGE);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.DATE_TIME_MODIFIED,
            AttributeType.UCOS_ABS_TIME,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "The date and time upon which the approval information on the product was last stored or changed in the IPL.",
            true,
            true));

    domain = new Domain();
    domain.l(getApprovalStatusOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.STATUS,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "A status field reporting on whether this product has been approved by the party responsible for Quality Control of the product.",
            true,
            true));

    return attributes;
  }

  public static List<AttributeInformation> getNsilExploitationInfoAttributes() {
    String prefix = NsiliConstants.NSIL_EXPLOITATION_INFO + ".";
    Domain domain;

    List<AttributeInformation> attributes = new ArrayList<>();

    domain = new Domain();
    domain.t(400);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.DESCRIPTION,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "A textual description of the performed exploitation.",
            false,
            true));

    domain = new Domain();
    domain.ir(new IntegerRange(0, 9));
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.LEVEL,
            AttributeType.INTEGER,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "The degree of exploitation performed on the original data. A value of '0' means that the product is not exploited. The interpretation of other attribute values is currently undefined.",
            false,
            true));

    domain = new Domain();
    domain.bv(false);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.AUTO_GENERATED,
            AttributeType.BOOLEAN_DATA,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "A flag indicating if the exploitation was automatically generated",
            false,
            true));

    domain = new Domain();
    domain.l(getSubjectiveQualityCodeOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.SUBJ_QUALITY_CODE,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "A code that indicates a subjective rating of the quality of the image or video.",
            false,
            true));

    return attributes;
  }

  public static List<AttributeInformation> getNsilSdsAttributes() {
    String prefix = NsiliConstants.NSIL_SDS + ".";
    Domain domain;

    List<AttributeInformation> attributes = new ArrayList<>();

    domain = new Domain();
    domain.l(getSdsOperationalStatusOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.OPERATIONAL_STATUS,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "SDS operational status",
            false,
            true));

    return attributes;
  }

  public static List<AttributeInformation> getNsilTdlAttributes() {
    String prefix = NsiliConstants.NSIL_TDL + ".";
    Domain domain;

    List<AttributeInformation> attributes = new ArrayList<>();

    domain = new Domain();
    domain.ir(new IntegerRange(0, 127));
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.ACTIVITY,
            AttributeType.INTEGER,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "A number that together with the 'platform' number defines the identity of a track",
            false,
            true));

    domain = new Domain();
    domain.l(getTdlMessageNumberOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.MESSAGE_NUM,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "The Link 16 J Series message number.",
            false,
            true));

    domain = new Domain();
    domain.ir(new IntegerRange(0, 63));
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.PLATFORM,
            AttributeType.INTEGER,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "A number that together with the 'activity' number defines the identity of a track",
            false,
            true));

    domain = new Domain();
    domain.t(10);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.TRACK_NUM,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Link 16 J Series track number for the track found in the product. The track number shall be in the decoded 5-character format (e.g. EK627).",
            false,
            true));

    return attributes;
  }

  public static List<AttributeInformation> getNsilRfiAttributes() {
    String prefix = NsiliConstants.NSIL_RFI + ".";
    Domain domain;

    List<AttributeInformation> attributes = new ArrayList<>();

    domain = new Domain();
    domain.t(50);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.FOR_ACTION,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "A nation, command, agency, organization or unit requested to provide a response. Ref. STANAG 2149 (edition 6). Note that the forAction value is singular and only identifies one entity.",
            false,
            true));

    domain = new Domain();
    domain.t(200);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.FOR_INFORMATION,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "A comma-separated list of nations, commands, agencies, organizations and units which may have an interest in the response. Ref. STANAG 2149 (edition 6)",
            false,
            true));

    domain = new Domain();
    domain.t(30);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.SERIAL_NUMBER,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "Unique human readable string identifying the RFI instance.",
            false,
            true));

    domain = new Domain();
    domain.l(getRfiStatusOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.STATUS,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "Indicates the status of the RFIs",
            false,
            true));

    domain = new Domain();
    domain.l(getRfiWorkflowStatusOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.WORKFLOW_STATUS,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "Indicates the workflow status of the RFI.",
            false,
            true));

    return attributes;
  }

  public static List<AttributeInformation> getNsilCxpAttributes() {
    String prefix = NsiliConstants.NSIL_CXP + ".";
    Domain domain;

    List<AttributeInformation> attributes = new ArrayList<>();

    domain = new Domain();
    domain.l(getCxpStatusOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.STATUS,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "A status field describing whether the Collection and Exploitation Plan is active, planned or expired.",
            false,
            true));

    return attributes;
  }

  public static List<AttributeInformation> getNsilReportAttributes() {
    String prefix = NsiliConstants.NSIL_REPORT + ".";
    Domain domain;

    List<AttributeInformation> attributes = new ArrayList<>();

    domain = new Domain();
    domain.t(200);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.INFORMATION_RATING,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Information rating of the data source and the information of the data source upon which the report is generated by assessing the \"Reliability of the Source and Credibility of the Information\" according to STANAG 2511, 2190 and 2191. The rating is specified by the combination of reliability and credibility values, e.g. B2. If the report is generated upon multiple source, multiple ratings are separated by commas (e.g. B2,C3,C6).",
            false,
            true));

    domain = new Domain();
    domain.t(10);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.ORIGINATORS_REQ_SERIAL_NUM,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Based on the originators request serial number STANAG 3277",
            false,
            true));

    domain = new Domain();
    domain.l(getReportPriorityOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.PRIORITY,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "A priority marking of the report",
            false,
            true));

    domain = new Domain();
    domain.l(getReportTypeOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.TYPE,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "The specific type of report.",
            false,
            true));

    return attributes;
  }

  public static List<AttributeInformation> getNsilEntityAttributes() {
    String prefix = NsiliConstants.NSIL_ENTITY + ".";
    Domain domain;

    List<AttributeInformation> attributes = new ArrayList<>();

    domain = new Domain();
    domain.l(getEntityTypeOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.TYPE,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "Information rating of the data source and the information of the data source upon which the report is generated by assessing the Reliability of the Source and Credibility of the Information according to STANAG 2511, 2190 and 2191. The rating is specified by the combination of reliability and credibility values, e.g. B2. If the report is generated upon multiple source, multiple ratings are separated by commas (e.g. B2,C3,C6).",
            false,
            true));

    domain = new Domain();
    domain.t(255);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.NAME,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "The name identifying the entity.",
            false,
            true));

    domain = new Domain();
    domain.t(255);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.ALIAS,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "The alias of the entity.",
            false,
            true));

    return attributes;
  }

  public static List<AttributeInformation> getNsilIntrepAttributes() {
    String prefix = NsiliConstants.NSIL_INTREP + ".";
    Domain domain;

    List<AttributeInformation> attributes = new ArrayList<>();

    domain = new Domain();
    domain.l(getSituationTypeOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.SITUATION_TYPE,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "Represents the type of situation.",
            false,
            true));

    return attributes;
  }

  public static List<AttributeInformation> getNsilIntsumAttributes() {
    String prefix = NsiliConstants.NSIL_INTSUM + ".";
    Domain domain;

    List<AttributeInformation> attributes = new ArrayList<>();

    domain = new Domain();
    domain.t(2048);
    attributes.add(
        new AttributeInformation(
            NsiliConstants.AREA_ASSESSMENT,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "Evaluation of the current situation and/or events within the defined area.",
            false,
            true));

    domain = new Domain();
    domain.t(2048);
    attributes.add(
        new AttributeInformation(
            NsiliConstants.GENERAL_ASSESSMENT,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "Assessment of the current situation or events. This will include short and long term forecasts of the intentions and capabilities of the parties involved.",
            false,
            true));

    domain = new Domain();
    domain.t(200);
    attributes.add(
        new AttributeInformation(
            NsiliConstants.SITUATION_TYPE,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Comma separated list of the situation type of the referred reports.",
            false,
            true));

    return attributes;
  }

  public static List<AttributeInformation> getNsilTaskAttributes() {
    String prefix = NsiliConstants.NSIL_TASK + ".";
    Domain domain;

    List<AttributeInformation> attributes = new ArrayList<>();

    domain = new Domain();
    domain.t(255);
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.COMMENTS,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "Status of the task",
            false,
            true));

    domain = new Domain();
    domain.l(getTaskStatusOptions());
    attributes.add(
        new AttributeInformation(
            prefix + NsiliConstants.STATUS,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "Status of the task",
            false,
            true));

    return attributes;
  }

  public static List<AttributeInformation> getNsilSourceAttributes() {
    // No direct source attributes exist
    return new ArrayList<>();
  }

  public static List<AttributeInformation> getNsilDestinationAttributes() {
    // No direct source attributes exist
    return new ArrayList<>();
  }

  public static List<AttributeInformation> getNsilAssocationAttributes() {
    // No direct source attributes exist
    return new ArrayList<>();
  }

  private static String[] getNsilCardStatusOptions() {
    return Arrays.stream(NsiliCardStatus.values())
        .map(Object::toString)
        .collect(Collectors.toList())
        .toArray(new String[0]);
  }

  private static String[] getNsilTypeOptions() {
    return Arrays.stream(NsiliProductType.values())
        .map(NsiliProductType::getSpecName)
        .collect(Collectors.toList())
        .toArray(new String[0]);
  }

  private static String[] getImageryCategoryOptions() {
    return Arrays.stream(NsiliImageryType.values())
        .map(Object::toString)
        .collect(Collectors.toList())
        .toArray(new String[0]);
  }

  private static String[] getImageryDecompressionTechOptions() {
    return Arrays.stream(NsiliImageryDecompressionTech.values())
        .map(Object::toString)
        .collect(Collectors.toList())
        .toArray(new String[0]);
  }

  private static String[] getMessageTypeOptions() {
    String[] options = new String[] {"XMPP"};
    return options;
  }

  private static String[] getClassificationOptions() {
    return Arrays.stream(NsiliClassification.values())
        .map(NsiliClassification::getSpecName)
        .collect(Collectors.toList())
        .toArray(new String[0]);
  }

  private static String[] getRelationshipOptions() {
    return Arrays.stream(NsiliRelationship.values())
        .map(NsiliRelationship::getSpecName)
        .collect(Collectors.toList())
        .toArray(new String[0]);
  }

  private static String[] getNsilStreamStandardOptions() {
    return Arrays.stream(NsiliStreamStandard.values())
        .map(Object::toString)
        .collect(Collectors.toList())
        .toArray(new String[0]);
  }

  private static String[] getVideoCategoryOptions() {
    return Arrays.stream(NsiliVideoCategoryType.values())
        .map(Object::toString)
        .collect(Collectors.toList())
        .toArray(new String[0]);
  }

  private static String[] getVideoEncodingSchemeOptions() {
    return Arrays.stream(NsiliVideoEncodingScheme.values())
        .map(NsiliVideoEncodingScheme::getSpecName)
        .collect(Collectors.toList())
        .toArray(new String[0]);
  }

  private static String[] getMetadataEncodingSchemeOptions() {
    return Arrays.stream(NsiliMetadataEncodingScheme.values())
        .map(Object::toString)
        .collect(Collectors.toList())
        .toArray(new String[0]);
  }

  private static String[] getVideoScanningModeOptions() {
    return Arrays.stream(NsiliScanningMode.values())
        .map(Object::toString)
        .collect(Collectors.toList())
        .toArray(new String[0]);
  }

  private static String[] getApprovalStatusOptions() {
    return Arrays.stream(NsiliApprovalStatus.values())
        .map(NsiliApprovalStatus::getSpecName)
        .collect(Collectors.toList())
        .toArray(new String[0]);
  }

  private static String[] getSubjectiveQualityCodeOptions() {
    return Arrays.stream(NsiliExploitationSubQualCode.values())
        .map(Object::toString)
        .collect(Collectors.toList())
        .toArray(new String[0]);
  }

  private static String[] getSdsOperationalStatusOptions() {
    return Arrays.stream(NsiliSdsOpStatus.values())
        .map(NsiliSdsOpStatus::getSpecName)
        .collect(Collectors.toList())
        .toArray(new String[0]);
  }

  private static String[] getCbrnEventOptions() {
    return Arrays.stream(NsiliCbrnEvent.values())
        .map(NsiliCbrnEvent::getSpecName)
        .collect(Collectors.toList())
        .toArray(new String[0]);
  }

  public static String[] getCbrnAlarmClassificationOptions() {
    return Arrays.stream(NsiliCbrnAlarmClassification.values())
        .map(NsiliCbrnAlarmClassification::getSpecName)
        .collect(Collectors.toList())
        .toArray(new String[0]);
  }

  public static String[] getSituationTypeOptions() {
    return Arrays.stream(NsiliSituationType.values())
        .map(NsiliSituationType::getSpecName)
        .collect(Collectors.toList())
        .toArray(new String[0]);
  }

  public static String[] getEntityTypeOptions() {
    return Arrays.stream(NsiliEntityType.values())
        .map(Object::toString)
        .collect(Collectors.toList())
        .toArray(new String[0]);
  }

  private static String[] getTdlMessageNumberOptions() {
    String[] validMsgNumbers =
        new String[] {
          "J2.2", "J2.5", "J3.0", "J3.2", "J3.3", "J3.5", "J3.7", "J7.0", "J7.1", "J7.2", "J14.0",
          "J14.2"
        };

    return validMsgNumbers;
  }

  private static String[] getRfiStatusOptions() {
    return Arrays.stream(NsiliRfiStatus.values())
        .map(Object::toString)
        .collect(Collectors.toList())
        .toArray(new String[0]);
  }

  private static String[] getRfiWorkflowStatusOptions() {
    return Arrays.stream(NsiliRfiWorkflowStatus.values())
        .map(Object::toString)
        .collect(Collectors.toList())
        .toArray(new String[0]);
  }

  private static String[] getCxpStatusOptions() {
    return Arrays.stream(NsiliCxpStatusType.values())
        .map(Object::toString)
        .collect(Collectors.toList())
        .toArray(new String[0]);
  }

  private static String[] getReportPriorityOptions() {
    return Arrays.stream(NsiliReportPriority.values())
        .map(Object::toString)
        .collect(Collectors.toList())
        .toArray(new String[0]);
  }

  private static String[] getReportTypeOptions() {
    return Arrays.stream(NsiliReportType.values())
        .map(Object::toString)
        .collect(Collectors.toList())
        .toArray(new String[0]);
  }

  private static String[] getTaskStatusOptions() {
    return Arrays.stream(NsiliTaskStatus.values())
        .map(Object::toString)
        .collect(Collectors.toList())
        .toArray(new String[0]);
  }
}
