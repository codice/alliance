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
package org.codice.alliance.catalog.mgmp;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.codice.ddf.spatial.ogc.csw.catalog.common.GmdConstants;

/**
 * These constants are only meant to be shared between {@link MgmpConverter} and {@link MgmpConverter}.
 */
final class MgmpConstants {

    static final String GML_NAMESPACE_PATH = "/MD_Metadata/@xmlns:gml";

    static final String XLINK_NAMESPACE_PATH = "/MD_Metadata/@xmlns:xlink";

    static final String GML_NAMESPACE = "http://www.opengis.net/gml/3.2";

    static final String XLINK_NAMESPACE = "http://www.w3.org/1999/xlink";

    static final String INDEX_TAG = "%index%";

    static final String MGMP_CHARACTER_SET_CODE =
            "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_CharacterSetCode";

    static final String ENCODING_TYPE = "utf8";

    static final String ENCODING_DESCRIPTION = "UTF-8";

    static final String MGMP_PROGRESS_CODE =
            "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_ProgressCode";

    static final String MGMP_DATE_TYPE_CODE =
            "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_DateTypeCode";

    static final String MGMP_ASSOCIATION_TYPE_CODE =
            "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_AssociationTypeCode";

    static final String MGMP_SCOPE_CODE = "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_ScopeCode";

    static final String MGMP_COVERAGE_CONTENT_TYPE_CODE =
            "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_CoverageContentTypeCode";

    static final String MD_METADATA_STANDARD_VERSION_PATH =
            "/MD_Metadata/metadataStandardVersion/gco:CharacterString";

    static final String MGMP_CLASSIFICATION_CODE =
            "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_ClassificationCode";

    static final String MD_METADATA_STANDARD_NAME_PATH =
            "/MD_Metadata/metadataStandardName/gco:CharacterString";

    static final String MGMP_ROLE_CODE = "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_RoleCode";

    static final String SOUTH_BOUND_LATITUDE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/geographicElement["
                    + INDEX_TAG + "]/EX_GeographicBoundingBox/southBoundLatitude/gco:Decimal";

    static final String EAST_BOUND_LONGITUDE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/geographicElement["
                    + INDEX_TAG + "]/EX_GeographicBoundingBox/eastBoundLongitude/gco:Decimal";

    static final String WEST_BOUND_LONGITUDE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/geographicElement["
                    + INDEX_TAG + "]/EX_GeographicBoundingBox/westBoundLongitude/gco:Decimal";

    static final String NORTH_BOUND_LATITUDE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/geographicElement["
                    + INDEX_TAG + "]/EX_GeographicBoundingBox/northBoundLatitude/gco:Decimal";

    static final String MD_IDENTIFICATION_LANGUAGE_CODE_LIST_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/language[" + INDEX_TAG
                    + "]/LanguageCode/@codeList";

    static final String MD_IDENTIFICATION_LANGUAGE_CODE_LIST_VALUE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/language[" + INDEX_TAG
                    + "]/LanguageCode/@codeListValue";

    static final String MD_IDENTIFICATION_LANGUAGE_TEXT_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/language[" + INDEX_TAG
                    + "]/LanguageCode";

    static final String GMD_TEMPORAL_START_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/temporalElement["
                    + INDEX_TAG + "]/EX_TemporalExtent/extent/gml:TimePeriod/gml:beginPosition";

    static final String GMD_TEMPORAL_END_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/temporalElement["
                    + INDEX_TAG + "]/EX_TemporalExtent/extent/gml:TimePeriod/gml:endPosition";

    static final String GMD_TEMPORAL_TIME_PERIOD_ID_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/temporalElement["
                    + INDEX_TAG + "]/EX_TemporalExtent/extent/gml:TimePeriod/@gml:id";

    static final String GMD_TEMPORAL_INSTANT_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/temporalElement["
                    + INDEX_TAG + "]/EX_TemporalExtent/extent/gml:TimeInstant/gml:timePosition";

    static final String GMD_TEMPORAL_TIME_INSTANT_ID_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/temporalElement["
                    + INDEX_TAG + "]/EX_TemporalExtent/extent/gml:TimeInstant/@gml:id";

    static final String BOUNDING_POLYGON_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/geographicElement["
                    + INDEX_TAG
                    + "]/EX_BoundingPolygon/polygon/gml:Polygon/gml:exterior/gml:LinearRing/gml:posList";

    static final String GMD_TOPIC_CATEGORY_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/topicCategory[" + INDEX_TAG
                    + "]/MD_TopicCategoryCode";

    static final String GMD_EMPTY_TOPIC_CATEGORY_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/topicCategory";

    static final String GMD_KEYWORD_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/descriptiveKeywords/MD_Keywords/keyword["
                    + INDEX_TAG + "]/gco:CharacterString";

    static final String GMD_EMPTY_KEYWORDS_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/descriptiveKeywords";

    static final String GMD_COUNTRY_CODE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/geographicElement["
                    + INDEX_TAG
                    + "]/EX_GeographicDescription/geographicIdentifier/RS_Identifier/code/gco:CharacterString";

    static final String GMD_COUNTRY_CODE_SPACE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/geographicElement["
                    + INDEX_TAG
                    + "]/EX_GeographicDescription/geographicIdentifier/RS_Identifier/codeSpace/gco:CharacterString";

    static final String GMD_POLYGON_GMLID_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/geographicElement["
                    + INDEX_TAG + "]/EX_BoundingPolygon/polygon/gml:Polygon/@gml:id";

    static final String GMD_POLYGON_SRSNAME_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/geographicElement["
                    + INDEX_TAG + "]/EX_BoundingPolygon/polygon/gml:Polygon/@srsName";

    static final String DATE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/citation/CI_Citation/date["
                    + INDEX_TAG + "]/CI_Date/date/gco:DateTime";

    static final String DATE_TYPE_CODE_VALUE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/citation/CI_Citation/date["
                    + INDEX_TAG + "]/CI_Date/dateType/CI_DateTypeCode/@codeListValue";

    static final String DATE_TYPE_CODE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/citation/CI_Citation/date["
                    + INDEX_TAG + "]/CI_Date/dateType/CI_DateTypeCode/@codeList";

    static final String DATE_TYPE_CODE_TEXT_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/citation/CI_Citation/date["
                    + INDEX_TAG + "]/CI_Date/dateType/CI_DateTypeCode";

    static final String DATA_IDENTIFICATION_RS_IDENTIFIER_CODE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/citation/CI_Citation/identifier/RS_Identifier/code/gco:CharacterString";

    static final String DATA_IDENTIFICATION_RS_IDENTIFIER_CODE_SPACE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/citation/CI_Citation/identifier/RS_Identifier/codeSpace/gco:CharacterString";

    static final String POINT_OF_CONTACT_NAME_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/pointOfContact/CI_ResponsibleParty/organisationName/gco:CharacterString";

    static final String POINT_OF_CONTACT_ADDRESS_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/pointOfContact/CI_ResponsibleParty/contactInfo/CI_Contact/address/CI_Address/deliveryPoint["
                    + INDEX_TAG + "]/gco:CharacterString";

    static final String POINT_OF_CONTACT_EMAIL_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/pointOfContact/CI_ResponsibleParty/contactInfo/CI_Contact/address/CI_Address/electronicMailAddress["
                    + INDEX_TAG + "]/gco:CharacterString";

    static final String POINT_OF_CONTACT_PHONE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/pointOfContact/CI_ResponsibleParty/contactInfo/CI_Contact/phone/CI_Telephone/voice["
                    + INDEX_TAG + "]/gco:CharacterString";

    static final String GMD_CONTACT_PHONE_PATH =
            "/MD_Metadata/contact/CI_ResponsibleParty/contactInfo/CI_Contact/phone/CI_Telephone/voice["
                    + INDEX_TAG + "]/gco:CharacterString";

    static final String GMD_CONTACT_EMAIL_PATH =
            "/MD_Metadata/contact/CI_ResponsibleParty/contactInfo/CI_Contact/address/CI_Address/electronicMailAddress["
                    + INDEX_TAG + "]/gco:CharacterString";

    static final String GMD_POINT_OF_CONTACT_ADDRESS_DELIVERY_POINT_PATH =
            "/MD_Metadata/contact/CI_ResponsibleParty/contactInfo/CI_Contact/address/CI_Address/deliveryPoint["
                    + INDEX_TAG + "]/gco:CharacterString";

    static final String GMD_CRS_AUTHORITY_PATH = "/MD_Metadata/referenceSystemInfo[" + INDEX_TAG
            + "]/MD_ReferenceSystem/referenceSystemIdentifier/RS_Identifier/codeSpace/gco:CharacterString";

    static final String GMD_CRS_CODE_PATH = "/MD_Metadata/referenceSystemInfo[" + INDEX_TAG
            + "]/MD_ReferenceSystem/referenceSystemIdentifier/RS_Identifier/code/gco:CharacterString";

    static final String CONTENT_INFO_MGMP_IMAGE_DESCRIPTION_CLOUD_COVERAGE_CONTENT_TYPE_TEXT_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/contentType/MD_CoverageContentTypeCode";

    static final String
            CONTENT_INFO_MGMP_IMAGE_DESCRIPTION_CLOUD_COVERAGE_CONTENT_TYPE_CODE_LIST_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/contentType/MD_CoverageContentTypeCode/@codeList";

    static final String CONTENT_INFO_MGMP_IMAGE_DESCRIPTION_CLOUD_COVERAGE_ATTRIBUTE_REASON_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/attributeDescription/@gco:nilReason";

    static final String CLASSIFICATION_CODE =
            "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_ClassificationUK_GovCode";

    static final String CHARACTER_SET_CODE_LIST_PATH =
            "/MD_Metadata/characterSet/MD_CharacterSetCode/@codeList";

    static final String CHARACTER_SET_CODE_LIST_VALUE_PATH =
            "/MD_Metadata/characterSet/MD_CharacterSetCode/@codeListValue";

    static final String CHARACTER_SET_TEXT_PATH = "/MD_Metadata/characterSet/MD_CharacterSetCode";

    static final String CONTACT_ROLE_CODE_LIST_PATH =
            "/MD_Metadata/contact/CI_ResponsibleParty/role/CI_RoleCode/@codeList";

    static final String CONTACT_ROLE_CODE_LIST_VALUE_PATH =
            "/MD_Metadata/contact/CI_ResponsibleParty/role/CI_RoleCode/@codeListValue";

    static final String CONTACT_ROLE_TEXT_PATH =
            "/MD_Metadata/contact/CI_ResponsibleParty/role/CI_RoleCode";

    static final String LANGUAGE_CODE_LIST =
            "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_LanguageCode";

    static final String GMD_METACARD_TYPE_NAME = "gmd.MD_Metadata";

    static final String GMD_NAMESPACE = "http://www.isotc211.org/2005/gmd";

    static final String GCO_NAMESPACE = "http://www.isotc211.org/2005/gco";

    static final String POINT_OF_CONTACT_ROLE_CODE_LIST_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/pointOfContact/CI_ResponsibleParty/role/CI_RoleCode/@codeList";

    static final String POINT_OF_CONTACT_ROLE_CODE_LIST_VALUE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/pointOfContact/CI_ResponsibleParty/role/CI_RoleCode/@codeListValue";

    static final String POINT_OF_CONTACT_ROLE_TEXT_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/pointOfContact/CI_ResponsibleParty/role/CI_RoleCode";

    static final String RESOURCE_SECURITY_CODE_LIST_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/resourceConstraints[" + INDEX_TAG
                    + "]/mgmp:MGMP_SecurityConstraints/classification/MD_ClassificationCode/@codeList";

    static final String RESOURCE_ORIGINATOR_SECURITY_CODE_LIST_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/resourceConstraints[" + INDEX_TAG
                    + "]/mgmp:MGMP_SecurityConstraints/mgmp:originatorClassification/MD_ClassificationCode/@codeList";

    static final String RESOURCE_STATUS_CODE_LIST_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/status/MD_ProgressCode/@codeList";

    static final String GMD_MIN_ALTITUDE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/verticalElement/EX_VerticalExtent/minimumValue/gco:Real";

    static final String GMD_VERTICAL_CRS_XLINK_HREF_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/verticalElement/EX_VerticalExtent/verticalCRS/@xlink:href";

    static final String GMD_VERTICAL_CRS_XLINK_TITLE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/verticalElement/EX_VerticalExtent/verticalCRS/@xlink:title";

    static final String ASSOCIATIONS_RELATED_CODE_SPACE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/aggregationInfo/MD_AggregateInformation/aggregateDataSetIdentifier/RS_Identifier/codeSpace/gco:CharacterString";

    static final String ASSOCIATIONS_RELATED_TYPE_CODE_LIST_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/aggregationInfo/MD_AggregateInformation/associationType/DS_AssociationTypeCode/@codeList";

    static final String ASSOCIATIONS_RELATED_TYPE_CODE_LIST_VALUE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/aggregationInfo/MD_AggregateInformation/associationType/DS_AssociationTypeCode/@codeListValue";

    static final String ASSOCIATIONS_RELATED_TYPE_TEXT_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/aggregationInfo/MD_AggregateInformation/associationType/DS_AssociationTypeCode";

    static final String METADATA_SECURITY_CODE_LIST_PATH =
            "/MD_Metadata/metadataConstraints/mgmp:MGMP_SecurityConstraints/classification/MD_ClassificationCode/@codeList";

    static final String METADATA_ORIGINATOR_CLASSIFICATION_CODE_LIST_PATH =
            "/MD_Metadata/metadataConstraints/mgmp:MGMP_SecurityConstraints/mgmp:originatorClassification/MD_ClassificationCode/@codeList";

    static final String GMD_POLYGON_SRSNAME = "http://www.opengis.net/def/crs/EPSG/0/4326";

    static final String GMD_NAMESPACE_PATH = "/MD_Metadata/@xmlns";

    static final String GCO_NAMESPACE_PATH = "/MD_Metadata/@xmlns:" + GmdConstants.GCO_PREFIX;

    static final String MGMP_NAMESPACE_PATH = "/MD_Metadata/@xmlns:mgmp";

    static final String MGMP_NAMESPACE = "http://mod.uk/spatial/ns/mgmp/2.0";

    static final String LANGUAGE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/language/LanguageCode/@codeListValue";

    static final String MGMP_SPATIAL_REFERENCE_SYSTEM_NUMBER_PATH =
            "/MD_Metadata/referenceSystemInfo/MD_ReferenceSystem/referenceSystemIdentifier/mgmp:MGMP_CRS_Identifier/codeSpace/gco:CharacterString";

    static final String MGMP_SPATIAL_REFERENCE_SYSTEM_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/MD_ReferenceSystem/referenceSystemIdentifier/mgmp:MGMP_CRS_Identifier/code/gco:CharacterString";

    static final String MGMP_SPATIAL_REFERENCE_SYSTEM_DATUM_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:datum/mgmp:MGMP_CRS_Object/mgmp:identifier/mgmp:MGMP_CRS_Identifier/code/gco:CharacterString";

    static final String MGMP_SPATIAL_REFERENCE_SYSTEM_DATUM_NUMBER_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:datum/mgmp:MGMP_CRS_Object/mgmp:identifier/mgmp:MGMP_CRS_Identifier/codeSpace/gco:CharacterString";

    static final String MGMP_SPATIAL_REFERENCE_SYSTEM_ELLIPSOID_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:ellipsoid/mgmp:MGMP_CRS_Object/mgmp:identifier/mgmp:MGMP_CRS_Identifier/code/gco:CharacterString";

    static final String MGMP_SPATIAL_REFERENCE_SYSTEM_ELLIPSOID_NUMBER_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:ellipsoid/mgmp:MGMP_CRS_Object/mgmp:identifier/mgmp:MGMP_CRS_Identifier/codeSpace/gco:CharacterString";

    static final String MGMP_SPATIAL_REFERENCE_SYSTEM_PROJECTION_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:projection/mgmp:MGMP_CRS_Object/mgmp:identifier/mgmp:MGMP_CRS_Identifier/code/gco:CharacterString";

    static final String MGMP_SPATIAL_REFERENCE_SYSTEM_PROJECTION_NUMBER_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:projection/mgmp:MGMP_CRS_Object/mgmp:identifier/mgmp:MGMP_CRS_Identifier/codeSpace/gco:CharacterString";

    static final String MGMP_SPATIAL_REFERENCE_SYSTEM_GRID_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:grid/mgmp:MGMP_CRS_Object/mgmp:identifier/mgmp:MGMP_CRS_Identifier/code/gco:CharacterString";

    static final String MGMP_SPATIAL_REFERENCE_SYSTEM_GRID_NUMBER_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:grid/mgmp:MGMP_CRS_Object/mgmp:identifier/mgmp:MGMP_CRS_Identifier/codeSpace/gco:CharacterString";

    static final String MGMP_SPATIAL_REFERENCE_SYSTEM_GRID_WKT_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:grid/mgmp:MGMP_CRS_Object/mgmp:specification/mgmp:MGMP_CRS_WKTSpecification/mgmp:wkt/gco:CharacterString";

    static final String MGMP_SPATIAL_REFERENCE_SYSTEM_PROJECTION_WKT_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:projection/mgmp:MGMP_CRS_Object/mgmp:specification/mgmp:MGMP_CRS_WKTSpecification/mgmp:wkt/gco:CharacterString";

    static final String MGMP_SPATIAL_REFERENCE_SYSTEM_ELLIPSOID_WKT_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:ellipsoid/mgmp:MGMP_CRS_Object/mgmp:specification/mgmp:MGMP_CRS_WKTSpecification/mgmp:wkt/gco:CharacterString";

    static final String MGMP_SPATIAL_REFERENCE_SYSTEM_DATUM_WKT_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:datum/mgmp:MGMP_CRS_Object/mgmp:specification/mgmp:MGMP_CRS_WKTSpecification/mgmp:wkt/gco:CharacterString";

    static final String MGMP_SPATIAL_REFERENCE_SYSTEM_CRS_WKT_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:crs/mgmp:MGMP_CRS_Object/mgmp:specification/mgmp:MGMP_CRS_WKTSpecification/mgmp:wkt/gco:CharacterString";

    /* MD_CoverageDescription */
    static final String ISR_COVERAGE_COMMENT_PATH =
            "/MD_Metadata/contentInfo/MD_CoverageDescription/attributeDescription/gco:RecordType";

    static final String ISR_COVERAGE_CATEGORY_PATH =
            "/MD_Metadata/contentInfo/MD_CoverageDescription/contentType/MD_CoverageContentTypeCode/@codeListValue";

    /* MGMP_VideoDescription */
    static final String ISR_VIDEO_COMMENT_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_VideoDescription/attributeDescription/gco:RecordType";

    static final String ISR_VIDEO_DESCRIPTION_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_VideoDescription/contentType/MD_CoverageContentTypeCode/@codeListValue";

    /* MGMP_ImageDescription */
    static final String ISR_IMAGE_COMMENT_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/attributeDescription/gco:RecordType";

    static final String ISR_IMAGE_DESCRIPTION_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/contentType/MD_CoverageContentTypeCode/@codeListValue";

    static final String ISR_MD_IMAGE_COMMENT_PATH =
            "/MD_Metadata/contentInfo/MD_ImageDescription/attributeDescription/gco:RecordType";

    static final String ISR_MD_IMAGE_DESCRIPTION_PATH =
            "/MD_Metadata/contentInfo/MD_ImageDescription/contentType/MD_CoverageContentTypeCode/@codeListValue";

    static final String NIIRS_RATING_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/imageQualityCode/RS_Identifier/code/gco:CharacterString";

    static final String CLOUD_COVERAGE_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/cloudCoverPercentage/gco:Real";

    static final String NIIRS_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/imageQualityCode/RS_Identifier/codeSpace/gco:CharacterString";

    static final String METADATA_SECURITY_PATH =
            "/MD_Metadata/metadataConstraints/mgmp:MGMP_SecurityConstraints/classification/MD_ClassificationCode/@codeListValue";

    static final String METADATA_RELEASABILITY_PATH =
            "/MD_Metadata/metadataConstraints/mgmp:MGMP_SecurityConstraints/mgmp:caveat/gco:CharacterString";

    static final String METADATA_ORIGINATOR_SECURITY_PATH =
            "/MD_Metadata/metadataConstraints/mgmp:MGMP_SecurityConstraints/mgmp:originatorClassification/MD_ClassificationCode/@codeListValue";

    static final String RESOURCE_SECURITY_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/resourceConstraints[" + INDEX_TAG
                    + "]/mgmp:MGMP_SecurityConstraints/classification/MD_ClassificationCode/@codeListValue";

    static final String RESOURCE_ORIGINATOR_SECURITY_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/resourceConstraints[" + INDEX_TAG
                    + "]/mgmp:MGMP_SecurityConstraints/mgmp:originatorClassification/MD_ClassificationCode/@codeListValue";

    static final String RESOURCE_SECURITY_RELEASABILITY_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/resourceConstraints[" + INDEX_TAG
                    + "]/mgmp:MGMP_SecurityConstraints/mgmp:caveat/gco:CharacterString";

    static final String USE_LIMITATIONS_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/resourceConstraints[" + INDEX_TAG
                    + "]/MD_Constraints/useLimitation/gco:CharacterString";

    static final String LEGAL_CONSTRAINTS_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/resourceConstraints[" + INDEX_TAG
                    + "]/MD_LegalConstraints/otherConstraints/gco:CharacterString";

    static final String LANGUAGE_CODE_LIST_PATH = "/MD_Metadata/language/LanguageCode/@codeList";

    static final String LANGUAGE_CODE_LIST_VALUE_PATH =
            "/MD_Metadata/language/LanguageCode/@codeListValue";

    static final String LANGUAGE_CODE_PATH = "/MD_Metadata/language/LanguageCode";

    static final List<String> DATA_QUALITY_LIST = Arrays.asList(
            "/MD_Metadata/dataQualityInfo/DQ_DataQuality/report/MGMP_UsabilityElement",
            "/MD_Metadata/dataQualityInfo/DQ_DataQuality/report/DQ_GriddedDataPositionalAccuracy",
            "/MD_Metadata/dataQualityInfo/DQ_DataQuality/report/DQ_RelativeInternalPositionalAccuracy",
            "/MD_Metadata/dataQualityInfo/DQ_DataQuality/report/DQ_AbsoluteExternalPositionalAccuracy");

    static final String FORMAT_PATH =
            "/MD_Metadata/distributionInfo/MD_Distribution/distributionFormat/MD_Format/name/mgmp:MGMP_FormatCode/@codeListValue";

    static final String DQ_QUANTITATIVE_RESULT = "DQ_QuantitativeResult";

    static final String DESCRIPTIVE_RESULT = "mgmp:MGMP_DescriptiveResult";

    /* Defaults  */

    static final List<String> DEFAULT_LANGUAGE =
            Collections.singletonList(Locale.ENGLISH.getISO3Country()
                    .toLowerCase());

    /* Misc */

    static final String NIIRS = "NIIRS";

    static final String EYES_ONLY = "Eyes Only";

    static final String EYES_DISCRETION = "Eyes Discretion";

    static final String RELEASABLE_TO = "Releasable to";

    static final String RESULT = "result";

    static final String NAME_OF_MEASURE = "nameOfMeasure";

    private MgmpConstants() {

    }
}