/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.catalog.transformer.mgmp;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class MgmpConstants {

    public static final String LANGUAGE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/language/LanguageCode/@codeListValue";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_NUMBER_PATH =
            "/MD_Metadata/referenceSystemInfo/MD_ReferenceSystem/referenceSystemIdentifier/mgmp:MGMP_CRS_Identifier/codeSpace/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/MD_ReferenceSystem/referenceSystemIdentifier/mgmp:MGMP_CRS_Identifier/code/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_DATUM_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:datum/mgmp:MGMP_CRS_Object/mgmp:identifier/mgmp:MGMP_CRS_Identifier/code/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_DATUM_NUMBER_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:datum/mgmp:MGMP_CRS_Object/mgmp:identifier/mgmp:MGMP_CRS_Identifier/codeSpace/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_ELLIPSOID_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:ellipsoid/mgmp:MGMP_CRS_Object/mgmp:identifier/mgmp:MGMP_CRS_Identifier/code/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_ELLIPSOID_NUMBER_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:ellipsoid/mgmp:MGMP_CRS_Object/mgmp:identifier/mgmp:MGMP_CRS_Identifier/codeSpace/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_PROJECTION_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:projection/mgmp:MGMP_CRS_Object/mgmp:identifier/mgmp:MGMP_CRS_Identifier/code/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_PROJECTION_NUMBER_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:projection/mgmp:MGMP_CRS_Object/mgmp:identifier/mgmp:MGMP_CRS_Identifier/codeSpace/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_GRID_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:grid/mgmp:MGMP_CRS_Object/mgmp:identifier/mgmp:MGMP_CRS_Identifier/code/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_GRID_NUMBER_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:grid/mgmp:MGMP_CRS_Object/mgmp:identifier/mgmp:MGMP_CRS_Identifier/codeSpace/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_GRID_WKT_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:grid/mgmp:MGMP_CRS_Object/mgmp:specification/mgmp:MGMP_CRS_WKTSpecification/mgmp:wkt/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_PROJECTION_WKT_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:projection/mgmp:MGMP_CRS_Object/mgmp:specification/mgmp:MGMP_CRS_WKTSpecification/mgmp:wkt/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_ELLIPSOID_WKT_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:ellipsoid/mgmp:MGMP_CRS_Object/mgmp:specification/mgmp:MGMP_CRS_WKTSpecification/mgmp:wkt/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_DATUM_WKT_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:datum/mgmp:MGMP_CRS_Object/mgmp:specification/mgmp:MGMP_CRS_WKTSpecification/mgmp:wkt/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_CRS_WKT_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:crs/mgmp:MGMP_CRS_Object/mgmp:specification/mgmp:MGMP_CRS_WKTSpecification/mgmp:wkt/gco:CharacterString";

    /* MD_CoverageDescription */
    public static final String ISR_COVERAGE_COMMENT_PATH =
            "/MD_Metadata/contentInfo/MD_CoverageDescription/attributeDescription/gco:RecordType";

    public static final String ISR_COVERAGE_CATEGORY_PATH =
            "/MD_Metadata/contentInfo/MD_CoverageDescription/contentType/MD_CoverageContentTypeCode/@codeListValue";

    /* MGMP_VideoDescription */
    public static final String ISR_VIDEO_COMMENT_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_VideoDescription/attributeDescription/gco:RecordType";

    public static final String ISR_VIDEO_DESCRIPTION_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_VideoDescription/contentType/MD_CoverageContentTypeCode/@codeListValue";

    /* MGMP_ImageDescription */
    public static final String ISR_IMAGE_COMMENT_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/attributeDescription/gco:RecordType";

    public static final String ISR_IMAGE_DESCRIPTION_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/contentType/MD_CoverageContentTypeCode/@codeListValue";

    public static final String ISR_MD_IMAGE_COMMENT_PATH =
            "/MD_Metadata/contentInfo/MD_ImageDescription/attributeDescription/gco:RecordType";

    public static final String ISR_MD_IMAGE_DESCRIPTION_PATH =
            "/MD_Metadata/contentInfo/MD_ImageDescription/contentType/MD_CoverageContentTypeCode/@codeListValue";

    public static final String NIIRS_RATING_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/imageQualityCode/RS_Identifier/code/gco:CharacterString";

    public static final String CLOUD_COVERAGE_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/cloudCoverPercentage/gco:Real";

    public static final String NIIRS_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/imageQualityCode/RS_Identifier/codeSpace/gco:CharacterString";

    public static final String METADATA_SECURITY_PATH =
            "/MD_Metadata/metadataConstraints/mgmp:MGMP_SecurityConstraints/classification/MD_ClassificationCode/@codeListValue";

    public static final String METADATA_RELEASABILITY_PATH =
            "/MD_Metadata/metadataConstraints/mgmp:MGMP_SecurityConstraints/mgmp:caveat/gco:CharacterString";

    public static final String METADATA_ORIGINATOR_SECURITY_PATH =
            "/MD_Metadata/metadataConstraints/mgmp:MGMP_SecurityConstraints/mgmp:originatorClassification/MD_ClassificationCode/@codeListValue";

    public static final String RESOURCE_SECURITY_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/resourceConstraints/mgmp:MGMP_SecurityConstraints/classification/MD_ClassificationCode/@codeListValue";

    public static final String RESOURCE_ORIGINATOR_SECURITY_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/resourceConstraints/mgmp:MGMP_SecurityConstraints/mgmp:originatorClassification/MD_ClassificationCode/@codeListValue";

    public static final String RESOURCE_SECURITY_RELEASABILITY_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/resourceConstraints/mgmp:MGMP_SecurityConstraints/mgmp:caveat/gco:CharacterString";

    public static final List<String> DATA_QUALITY_LIST = Arrays.asList(
            "/MD_Metadata/dataQualityInfo/DQ_DataQuality/report/MGMP_UsabilityElement",
            "/MD_Metadata/dataQualityInfo/DQ_DataQuality/report/DQ_GriddedDataPositionalAccuracy",
            "/MD_Metadata/dataQualityInfo/DQ_DataQuality/report/DQ_RelativeInternalPositionalAccuracy",
            "/MD_Metadata/dataQualityInfo/DQ_DataQuality/report/DQ_AbsoluteExternalPositionalAccuracy");

    public static final String FORMAT_PATH =
            "/MD_Metadata/distributionInfo/MD_Distribution/distributionFormat/MD_Format/name/mgmp:MGMP_FormatCode/@codeListValue";

    public static final String DQ_QUANTITATIVE_RESULT = "DQ_QuantitativeResult";

    public static final String DESCRIPTIVE_RESULT = "mgmp:MGMP_DescriptiveResult";

    /* Defaults  */

    public static final List<String> DEFAULT_LANGUAGE = Arrays.asList(Locale.ENGLISH.getISO3Country().toLowerCase());

    /* Misc */

    public static final String NIIRS = "NIIRS";

    public static final String EYES_ONLY = "Eyes Only";

    public static final String EYES_DISCRETION = "Eyes Discretion";

    public static final String RELEASABLE_TO = "Releasable to";

    public static final String RESULT = "result";

    public static final String NAME_OF_MEASURE = "nameOfMeasure";

    private MgmpConstants() {

    }
}