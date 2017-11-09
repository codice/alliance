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
package org.codice.alliance.nsili.common;

import ddf.catalog.data.ContentType;
import ddf.catalog.data.impl.ContentTypeImpl;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class NsiliConstants {

  public static final String STANAG_VERSION = "STANAG 4559";

  private static final Set<String> CONTENT_STRINGS =
      Collections.unmodifiableSet(
          new HashSet<>(
              CollectionUtils.collect(
                  Arrays.asList(NsiliProductType.values()),
                  (Object object) -> {
                    NsiliProductType type = (NsiliProductType) object;
                    return type.getSpecName();
                  })));

  private static final Set<ContentType> CONTENT_TYPES =
      Collections.unmodifiableSet(
          new HashSet<>(
              CollectionUtils.collect(
                  Arrays.asList(NsiliProductType.values()),
                  (Object object) -> {
                    NsiliProductType type = (NsiliProductType) object;
                    return new ContentTypeImpl(type.getSpecName(), STANAG_VERSION);
                  })));

  // Categories
  public static final String NSIL_CORE = "NSIL_CORE";

  public static final String STANAG_4607 = "STANAG4607";

  public static final String STANAG_4609 = "STANAG4609";

  public static final String STANAG_4545 = "STANAG4545";

  public static final String STANAG_5516 = "STANAG5516";

  public static final String NACT_L16 = "NACT_L16";

  // Entity Fields
  public static final String NSIL_PRODUCT = "NSIL_PRODUCT";

  public static final String NSIL_ALL_VIEW = "NSIL_ALL_VIEW";

  public static final String NSIL_IMAGERY_VIEW = "NSIL_IMAGERY_VIEW";

  public static final String NSIL_GMTI_VIEW = "NSIL_GMTI_VIEW";

  public static final String NSIL_MESSAGE_VIEW = "NSIL_MESSAGE_VIEW";

  public static final String NSIL_VIDEO_VIEW = "NSIL_VIDEO_VIEW";

  public static final String NSIL_ASSOCIATION_VIEW = "NSIL_ASSOCIATION_VIEW";

  public static final String NSIL_REPORT_VIEW = "NSIL_REPORT_VIEW";

  public static final String NSIL_CCIRM_VIEW = "NSIL_CCIRM_VIEW";

  public static final String NSIL_TDL_VIEW = "NSIL_TDL_VIEW";

  public static final String NSIL_CBRN_VIEW = "NSIL_CBRN_VIEW";

  public static final String NSIL_DESTINATION = "NSIL_DESTINATION";

  public static final String NSIL_ASSOCIATION = "NSIL_ASSOCIATION";

  public static final String NSIL_RELATION = "NSIL_RELATION";

  public static final String NSIL_SOURCE = "NSIL_SOURCE";

  public static final String NSIL_VIDEO = "NSIL_VIDEO";

  public static final String NSIL_MESSAGE = "NSIL_MESSAGE";

  public static final String NSIL_IMAGERY = "NSIL_IMAGERY";

  public static final String NSIL_GMTI = "NSIL_GMTI";

  public static final String NSIL_COMMON = "NSIL_COMMON";

  public static final String NSIL_PART = "NSIL_PART";

  public static final String NSIL_RELATED_FILE = "NSIL_RELATED_FILE";

  public static final String NSIL_STREAM = "NSIL_STREAM";

  public static final String NSIL_SECURITY = "NSIL_SECURITY";

  public static final String NSIL_METADATA_SECURITY = "NSIL_METADATASECURITY";

  public static final String NSIL_FILE = "NSIL_FILE";

  public static final String NSIL_CARD = "NSIL_CARD";

  public static final String NSIL_CBRN = "NSIL_CBRN";

  public static final String NSIL_APPROVAL = "NSIL_APPROVAL";

  public static final String NSIL_COVERAGE = "NSIL_COVERAGE";

  public static final String NSIL_CXP = "NSIL_CXP";

  public static final String NSIL_EXPLOITATION_INFO = "EXPLOITATION_INFO";

  public static final String NSIL_REPORT = "NSIL_REPORT";

  public static final String NSIL_IR = "NSIL_IR";

  public static final String NSIL_RFI = "NSIL_RFI";

  public static final String NSIL_SDS = "NSIL_SDS";

  public static final String NSIL_ENTITY = "NSIL_ENTITY";

  public static final String NSIL_INTREP = "NSIL_INTREP";

  public static final String NSIL_INTSUM = "NSIL_INTSUM";

  public static final String NSIL_TASK = "NSIL_TASK";

  public static final String NSIL_TDL = "NSIL_TDL";

  // Attribute Fields
  public static final String IDENTIFIER_UUID = "identifierUUID";

  public static final String TYPE = "type";

  public static final String KEYWORDS = "keywords";

  public static final String IDENTIFIER_JOB = "identifierJob";

  public static final String NUMBER_OF_TARGET_REPORTS = "numberOfTargetReports";

  public static final String CATEGORY = "category";

  public static final String RECIPIENT = "recipient";

  public static final String SUBJECT = "subject";

  public static final String DECOMPRESSION_TECHNIQUE = "decompressionTechnique";

  public static final String NUMBER_OF_BANDS = "numberOfBands";

  public static final String IDENTIFIER = "identifier";

  public static final String MESSAGE_BODY = "messageBody";

  public static final String MESSAGE_TYPE = "messageType";

  public static final String ENCODING_SCHEME = "encodingScheme";

  public static final String PART_IDENTIFIER = "partIdentifier";

  public static final String DATE_TIME_DECLARED = "dateTimeDeclared";

  public static final String PRODUCT_URL = "productURL";

  public static final String CLASSIFICATION = "classification";

  public static final String POLICY = "policy";

  public static final String RELEASABILITY = "releasability";

  public static final String CREATOR = "creator";

  public static final String DESCRIPTION_ABSTRACT = "descriptionAbstract";

  public static final String IDENTIFIER_MISSION = "identifierMission";

  public static final String IDENTIFIER_JC3IEDM = "identifierJC3IEDM";

  public static final String LANGUAGE = "language";

  public static final String SOURCE = "source";

  public static final String SUBJECT_CATEGORY_TARGET = "subjectCategoryTarget";

  public static final String TARGET_NUMBER = "targetNumber";

  public static final String SOURCE_DATE_TIME_MODIFIED = "sourceDateTimeModified";

  public static final String DATE_TIME_MODIFIED = "dateTimeModified";

  public static final String PUBLISHER = "publisher";

  public static final String SOURCE_LIBRARY = "sourceLibrary";

  public static final String SPATIAL_COUNTRY_CODE = "spatialCountryCode";

  public static final String SPATIAL_GEOGRAPHIC_REF_BOX = "spatialGeographicReferenceBox";

  public static final String ADVANCED_GEOSPATIAL = "advancedGeoSpatial";

  public static final String TEMPORAL_START = "temporalStart";

  public static final String TEMPORAL_END = "temporalEnd";

  public static final String ARCHIVED = "archived";

  public static final String ARCHIVE_INFORMATION = "archiveInformation";

  public static final String ARCHIVAL_INFORMATION = "archivalInformation";

  public static final String EXTENT = "extent";

  public static final String FORMAT = "format";

  public static final String FORMAT_VERSION = "formatVersion";

  public static final String TITLE = "title";

  public static final String IS_PRODUCT_LOCAL = "isProductLocal";

  public static final String FILENAME = "filename";

  public static final String CLOUD_COVER_PCT = "cloudCoverPercentage";

  public static final String COMMENTS = "comments";

  public static final String NIIRS = "NIIRS";

  public static final String NUMBER_OF_ROWS = "numberOfRows";

  public static final String NUMBER_OF_COLS = "numberOfColumns";

  public static final String STANDARD = "standard";

  public static final String STANDARD_VERSION = "standardVersion";

  public static final String SOURCE_URL = "sourceURL";

  public static final String PROGRAM_ID = "programID";

  public static final String AVG_BIT_RATE = "averageBitRate";

  public static final String FRAME_RATE = "frameRate";

  public static final String METADATA_ENC_SCHEME = "metadataEncodingScheme";

  public static final String MISM_LEVEL = "MISMLevel";

  public static final String SCANNING_MODE = "scanningMode";

  public static final String VMTI_PROCESSED = "vmtiProcessed";

  public static final String NUM_VMTI_TGT_REPORTS = "numberOfVMTITargetReports";

  public static final String OPERATION_NAME = "operationName";

  public static final String INCIDENT_NUM = "incidentNumber";

  public static final String EVENT_TYPE = "eventType";

  public static final String CBRN_CATEGORY = "cbrnCategory";

  public static final String SUBSTANCE = "substance";

  public static final String ALARM_CLASSIFICATION = "alarmClassification";

  public static final String STATUS = "status";

  public static final String NUM_OF_PARTS = "numberOfParts";

  public static final String FOR_ACTION = "forAction";

  public static final String FOR_INFORMATION = "forInformation";

  public static final String SERIAL_NUMBER = "serialNumber";

  public static final String WORKFLOW_STATUS = "workflowStatus";

  public static final String DESCRIPTION = "description";

  public static final String LEVEL = "level";

  public static final String AUTO_GENERATED = "autoGenerated";

  public static final String SUBJ_QUALITY_CODE = "subjectiveQualityCode";

  public static final String OPERATIONAL_STATUS = "operationalStatus";

  public static final String ACTIVITY = "activity";

  public static final String MESSAGE_NUM = "messageNumber";

  public static final String PLATFORM = "platform";

  public static final String TRACK_NUM = "trackNumber";

  public static final String APPROVED_BY = "approvedBy";

  public static final String INFORMATION_RATING = "informationRating";

  public static final String ORIGINATORS_REQ_SERIAL_NUM = "originatorsRequestSerialNumber";

  public static final String PRIORITY = "priority";

  public static final String IS_FILE_LOCAL = "isFileLocal";

  public static final String AMPLIFICATION = "amplification";

  public static final String FILE_TYPE = "fileType";

  public static final String URL = "URL";

  public static final String CONTRIBUTOR = "contributor";

  public static final String RELATIONSHIP = "relationship";

  public static final String NAME = "name";

  public static final String ALIAS = "alias";

  public static final String SITUATION_TYPE = "situationType";

  public static final String AREA_ASSESSMENT = "areaAssessment";

  public static final String GENERAL_ASSESSMENT = "generalAssessment";

  public static final String NATO = "NATO";

  // Association Constants
  public static final String HAS_PART = "HAS PART";

  public static final String IS_VERSION_OF = "IS VERSION OF";

  public static final String REPLACES = "REPLACES";

  public static final String IS_SUPPORT_DATA_TO = "IS SUPPORT DATA TO";

  public static final String ORIGINATING_FROM = "ORIGINATING FROM";

  public static final String FOLLOWS = "FOLLOWS";

  // Related File
  public static final String THUMBNAIL_TYPE = "THUMBNAIL";

  // Request Properties
  public static final String PROP_PROTOCOL = "PROTOCOL";

  public static final String PROP_PORT = "PORT";

  public static final String UNKNOWN = "Unknown";

  private NsiliConstants() {}

  public static Set<String> getContentStrings() {
    return CONTENT_STRINGS;
  }

  public static Set<ContentType> getContentTypes() {
    return CONTENT_TYPES;
  }
}
