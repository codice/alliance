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
package org.codice.alliance.nsili.common;

import java.util.HashSet;
import java.util.Set;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;

public class NsiliMetacardType implements MetacardType {

    /**
     * Prefix used for the name of the NSILI Metacard Sub-Type
     */
    public static final String NSILI_METACARD_TYPE_PREFIX = "nsili";

    /**
     * Postfix used for the name of the NSILI Metacard Sub-Type
     */
    public static final String NSILI_METACARD_TYPE_POSTFIX = "metacard";

    /**
     * The Metacard Type name for NSILI base class
     */
    public static final String METACARD_TYPE_NAME =
            NSILI_METACARD_TYPE_PREFIX + "." + NSILI_METACARD_TYPE_POSTFIX;

    /**
     * The date and time upon which information (cataloguing metadata) about the product was last stored or changed in the source IPL.
     */
    public static final String SOURCE_DATETIME_MODIFIED = "sourceDateTimeModified";

    /**
     * The name of the organization responsible for making the resource (product) available in an IPL.
     * By doing so the publisher enables the discovery of that resource by a requestor entity (client).
     * Examples of organizations are 'CJTF', 'LCC', 'ACC' etc.
     */
    public static final String PUBLISHER = "publisher";

    /**
     * Alphanumeric identification code of the library in which the product was initially ingested.
     */
    public static final String SOURCE_LIBRARY = "sourceLibrary";

    /**
     * An alphanumeric identifier that identifies the mission (e.g. a reconnaissance mission) under
     * which the product was collected/generated.
     * As an example, for products collected by sensors on an aircraft, the mission identifier should
     * be the 'Mission Number' from the Air Tasking Order (ATO).
     */
    public static final String IDENTIFIER_MISSION = "identifierMission";

    /**
     * An additional identifier that can be used for cross-referencing into peripherally accessed JC3IEDM databases.
     */
    public static final String ID_JC3IEDM = "identiferJC3Idm";

    /**
     * A list of 3 character language codes according to ISO 639-2 for the intellectual content of the resource.
     * Multiple languages are separated by BCS Comma (code 0x2C).
     * Note: There are two codes for special situations: * mul (for multiple languages) should be
     * applied when several languages are used and it is not practical to specify all the appropriate
     * language codes. * und (for undetermined) is provided for those situations in which a language
     * or languages must be indicated but the language cannot be identified.
     */
    public static final String LANGUAGE = "language";

    /**
     * References to assets (e.g. platform IDs) from which the tagged data asset is derived.
     * Sources may be derived partially or wholly, and it is recommended that an identifier
     * (such as a string or number from a formal identification system) be used as a reference.
     * In case of multiple sources these shall be separated by BCS Comma
     * (could happen for container files like AAF, MXF and NSIF).
     * 'Source' is different from 'creator' in that the 'source' entity is the provider of the data
     * content, while the 'creator' is the entity responsible for assembling the data
     * (provided by "sources") into a file.
     */
    public static final String SOURCE = "NSILISource";

    /**
     * A target category from STANAG 3596 that should reflect the main subject of the resource.
     * In case of multiple categories these shall be separated by the BCS Comma
     */
    public static final String SUBJECT_CATEGORY_TARGET = "subjectCategoryTarget";

    /**
     * This field shall contain the identification of the primary target in the product.
     * In case of multiple target identifiers these shall be separated by the BCS Comma
     */
    public static final String TARGET_NUMBER = "targetNumber";

    /**
     * The nature or genre of the content of the resource.
     *
     * @see NsiliProductType
     */
    public static final String PRODUCT_TYPE = "productType";

    /**
     * List of standards-based abbreviation (three character codes) of a country names describing
     * the area over which the product was collected separated by BCS Comma. The three character
     * code shall be according to STANAG 1059.
     */
    public static final String COUNTRY_CODE = "countryCode";

    /**
     * Start time of a period of time for the content of the dataset (start time of content acquisition).
     * For products capturing a single instant in time, start time and end time will be equal
     * (or the end time could be omitted).
     */
    public static final String START_DATETIME = "startDateTime";

    /**
     * End time of a period of time for the content of the dataset (end time of content acquisition).
     * For products capturing a single instant in time, start time and end time will be equal
     * (or the end time could be omitted).
     */
    public static final String END_DATETIME = "endDateTime";

    /**
     * Indicates whether the file has been archived.
     */
    public static final String FILE_ARCHIVED = "fileArchived";

    /**
     * Additional archiving information in free text form allowing the archived data (product files)
     * to be found and possibly be restored to the server storage.
     * This information could contain the date when the data was archived and point of contact for
     * retrieving the archived data.
     */
    public static final String FILE_ARCHIVED_INFO = "fileArchivedInfo";

    /**
     * Date and time on which the resource was declared, filed or stored.
     */
    public static final String PRODUCT_CREATE_TIME = "productCreateTime";

    /**
     * textual description of the performed exploitation.
     */
    public static final String EXPLOITATION_DESCRIPTION = "exploitationDescription";

    /**
     * The degree of exploitation performed on the original data. A value of '0' means that the
     * product is not exploited.
     */
    public static final String EXPLOITATION_LEVEL = "exploitationLevel";

    /**
     * A flag indicating if the exploitation was automatically generated.
     */
    public static final String EXPLOITATION_AUTO_GEN = "exploitationAutoGenerated";

    /**
     * A code that indicates a subjective rating of the quality of the image or video.
     */
    public static final String EXPLOITATION_SUBJ_QUAL_CODE = "exploitationSubjectiveQualityCode";

    /**
     * SDS operational status.
     */
    public static final String SDS_OPERATIONAL_STATUS = "sdsOperationalStatus";

    /**
     * The name of the organization responsible for Quality Assurance of the resource (product)
     * available in an IPL.
     */
    public static final String APPROVAL_BY = "approvedBy";

    /**
     * The date and time upon which the approval information on the product was last stored or
     * changed in the IPL.
     */
    public static final String APPROVAL_DATETIME_MODIFIED = "approvedDateTimeModified";

    /**
     * A status field reporting on whether this product has been approved by the party responsible
     * for Quality Control of the product.
     */
    public static final String APPROVAL_STATUS = "approvalStatus";

    /**
     * NATO Security markings that determine the physical security given to the information in
     * storage and transmission, its circulation, destruction and the personnel security clearance
     * required for access as required by [C- M(2002)49].
     * <p>
     * NOTE: This is a merged classification of the METADATA_SECURITY and SECURITY fields
     */
    public static final String SECURITY_CLASSIFICATION = "securityClassification";

    /**
     * This field shall contain valid values indicating the national or multinational security
     * policies used to classify the Product. To indicate a national security policy Country Codes
     * per STANAG 1059 shall be used. In cases where the country is not listed by a three character
     * country code, but as a six character region code, the six character province code from
     * STANAG 1059 shall be used (e.g. Kosovo is listed as a region in Yugoslavia with six character
     * province code YUKM-). In all other cases when STANAG 1059 is not sufficient to describe the
     * security policy, additional enumeration labels could be used (e.g. 'NATO/PFP' and 'NATO/EU').
     * Note: Although STANAG 1059 includes the 'XXN' entry for NATO, the full 'NATO' name shall be
     * used to indicate NATO security policy to avoid any confusion from established and common
     * used terms.
     * <p>
     * NOTE: This is a merged classification of the METADATA_SECURITY and SECURITY fields
     */
    public static final String SECURITY_POLICY = "securityPolicy";

    /**
     * An additional marking to further limit the dissemination of classified information in
     * accordance with [C-M (2002)49]. Values include one or more three character country codes as
     * found in STANAG 1059 separated by a single BCS Comma (code 0x2C). Default value should be
     * NATO. Note: Although STANAG 1059 includes the 'XXN' entry for NATO, the full 'NATO' name
     * shall be used to indicate NATO releasability to avoid any confusion from established and
     * common used terms.
     * <p>
     * NOTE: This is a merged classification of the METADATA_SECURITY and SECURITY fields
     */
    public static final String SECURITY_RELEASABILITY = "securityReleasability";

    /**
     * Associated metadata records
     */
    public static final String ASSOCIATIONS = "associations";

    /**
     * Associated files
     */
    public static final String RELATED_FILES = "relatedFiles";

    /**
     * Indicates whether the stream has been archived.
     */
    public static final String STREAM_ARCHIVED = "archived";

    /**
     * Additional archiving information in free text form allowing the archived data (stream data)
     * to be found and possibly be restored to the server storage. This information could contain
     * the date when the data was archived and point of contact for retrieving the archived data.
     */
    public static final String STREAM_ARCHIVAL_INFO = "archivalInfo";

    /**
     * An entity responsible for providing the stream. Creator is responsible for the intellectual
     * or creative content of the stream.
     */
    public static final String STREAM_CREATOR = "creator";

    /**
     * Date and time on which the stream was declared, filed or stored.
     */
    public static final String STREAM_DATETIME_DECLARED = "dateTimeDeclared";

    /**
     * The program ID determines the stream ID within the Transport Stream that contains the
     * PMT (Program Mapping Table) of the stream.
     */
    public static final String STREAM_PROGRAM_ID = "programId";

    /**
     * The standard that the streamed data complies with.
     */
    public static final String STREAM_STANDARD = "streamFormat";

    /**
     * Version of product standard E.g. "1.0".
     */
    public static final String STREAM_STANDARD_VER = "streamFormatVer";

    /**
     * The address and access protocol in the form of an URL by which a user can directly access the
     * data stream. For RTSP, HTTP, HTTPS and JPIP a standard URL syntax will be used (could include
     * port number). For UDP/RTP, a URL notation using a '@-character-notaton' is used to allow a URL
     * to define a broadcasted stream or a multicasted stream. A UDP broadcast source on port 8105
     * would be defined as 'udp://@:8105', a RTP broadcast source would be defined as
     * 'rtp://@:8105'; similarly multicasted UDP/RTP source would be defined as
     * 'udp://@multicast_address:port' or 'udp://@multicast_address:port'. This UDP/RTP @-syntax is
     * identical to what is being used by the VideoLAN VLC application. For the broadcast situation
     * it is assumed that the network takes care about forwarding the broadcasts between the LANs
     * (e.g. using NIRIS) so the stream consumer does not need of the LAN-location of the or
     * origial stream source. Note 1: Contrary to the productURL this attribute is queryable.
     * This would allow clients to only discover streams using protocols that the client can consume.
     * Note 2: When a stream is archived and no longer online, the sourceURL attribute shall be removed.
     */
    public static final String STREAM_SOURCE_URL = "streamSourceURL";

    /**
     * The MIME type for the product object to which this metadata applies with the necessary
     * extension to cater for ISR specific file types not yet MIME registered.
     */
    public static final String FILE_FORMAT = "fileFormat";

    /**
     * Version of product format. E.g. "1.0". In case of an XML document this attribute refers to
     * the version of the XML schema that the document refers to (and not to the version of the
     * XML format).
     */
    public static final String FILE_FORMAT_VER = "fileFormatVer";

    /**
     * The address and access protocol in the form of an URL by which a user can directly access a
     * product without the user placing an order for delivery. Note, This attribute is not queryable.
     */
    public static final String FILE_URL = "fileURL";

    /**
     * AttributeType used to wrap a NsiliProductType enumeration.
     */
    public static final AttributeType<NsiliProductType> NSILI_PRODUCT_TYPE;

    /**
     * AttributeType used to wrap a NsiliProductType enumeration.
     */
    public static final AttributeType<NsiliExploitationSubQualCode>
            NSILI_EXPLOITATION_SUBJ_QUAL_CD_TYPE;

    /**
     * AttributeType used to wrap a NsiliSdsOpStatus enumeration.
     */
    public static final AttributeType<NsiliSdsOpStatus> NSILI_SDS_OP_STATUS_TYPE;

    /**
     * AttributeType used to wrap a NsiliApprovalStatus enumeration.
     */
    public static final AttributeType<NsiliApprovalStatus> NSILI_APPROVAL_STATUS_TYPE;

    /**
     * AttributeType used to wrap a NsiliStreamStandard enumeration.
     */
    public static final AttributeType<NsiliStreamStandard> NSILI_STREAM_STANDARD_TYPE;

    protected Set<AttributeDescriptor> attributeDescriptors = new HashSet<>();

    private static final Set<AttributeDescriptor> NSILI_DESCRIPTORS = new HashSet<>();

    static {
        NSILI_PRODUCT_TYPE = new AttributeType<NsiliProductType>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AttributeFormat getAttributeFormat() {
                return AttributeFormat.STRING;
            }

            @Override
            public Class<NsiliProductType> getBinding() {
                return NsiliProductType.class;
            }
        };

        NSILI_EXPLOITATION_SUBJ_QUAL_CD_TYPE = new AttributeType<NsiliExploitationSubQualCode>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AttributeFormat getAttributeFormat() {
                return AttributeFormat.STRING;
            }

            @Override
            public Class<NsiliExploitationSubQualCode> getBinding() {
                return NsiliExploitationSubQualCode.class;
            }
        };

        NSILI_SDS_OP_STATUS_TYPE = new AttributeType<NsiliSdsOpStatus>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AttributeFormat getAttributeFormat() {
                return AttributeFormat.STRING;
            }

            @Override
            public Class<NsiliSdsOpStatus> getBinding() {
                return NsiliSdsOpStatus.class;
            }
        };

        NSILI_APPROVAL_STATUS_TYPE = new AttributeType<NsiliApprovalStatus>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AttributeFormat getAttributeFormat() {
                return AttributeFormat.STRING;
            }

            @Override
            public Class<NsiliApprovalStatus> getBinding() {
                return NsiliApprovalStatus.class;
            }
        };

        NSILI_STREAM_STANDARD_TYPE = new AttributeType<NsiliStreamStandard>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AttributeFormat getAttributeFormat() {
                return AttributeFormat.STRING;
            }

            @Override
            public Class<NsiliStreamStandard> getBinding() {
                return NsiliStreamStandard.class;
            }
        };

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(SOURCE_DATETIME_MODIFIED,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.DATE_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(PUBLISHER,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(SOURCE_LIBRARY,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(IDENTIFIER_MISSION,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(ID_JC3IEDM,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.LONG_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(LANGUAGE,
                true /* indexed */,
                true /* stored */,
                true /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(SOURCE,
                true /* indexed */,
                true /* stored */,
                true /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(SUBJECT_CATEGORY_TARGET,
                true /* indexed */,
                true /* stored */,
                true /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(TARGET_NUMBER,
                true /* indexed */,
                true /* stored */,
                true /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(PRODUCT_TYPE,
                true /* indexed */,
                true /* stored */,
                true /* tokenized */,
                false /* multivalued */,
                NSILI_PRODUCT_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(COUNTRY_CODE,
                true /* indexed */,
                true /* stored */,
                true /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(START_DATETIME,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.DATE_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(END_DATETIME,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.DATE_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(FILE_ARCHIVED,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.BOOLEAN_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(FILE_ARCHIVED_INFO,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(PRODUCT_CREATE_TIME,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.DATE_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(EXPLOITATION_DESCRIPTION,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(EXPLOITATION_LEVEL,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.INTEGER_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(EXPLOITATION_AUTO_GEN,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.BOOLEAN_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(EXPLOITATION_SUBJ_QUAL_CODE,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                NSILI_EXPLOITATION_SUBJ_QUAL_CD_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(SDS_OPERATIONAL_STATUS,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                NSILI_SDS_OP_STATUS_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(APPROVAL_BY,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(APPROVAL_DATETIME_MODIFIED,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.DATE_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(APPROVAL_STATUS,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                NSILI_APPROVAL_STATUS_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(APPROVAL_STATUS,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                NSILI_APPROVAL_STATUS_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(SECURITY_CLASSIFICATION,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(SECURITY_POLICY,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(SECURITY_RELEASABILITY,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(ASSOCIATIONS,
                false /* indexed */,
                true /* stored */,
                false /* tokenized */,
                true /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(STREAM_ARCHIVED,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.BOOLEAN_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(STREAM_ARCHIVAL_INFO,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(STREAM_CREATOR,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(STREAM_DATETIME_DECLARED,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.DATE_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(STREAM_PROGRAM_ID,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.INTEGER_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(STREAM_STANDARD,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                NSILI_STREAM_STANDARD_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(STREAM_STANDARD_VER,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(STREAM_SOURCE_URL,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(FILE_FORMAT,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(FILE_FORMAT_VER,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(FILE_URL,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));
    }

    /**
     * Basic attributes
     *
     * Metacard.MODIFIED,
     * Metacard.EXPIRATION
     * Metacard.EFFECTIVE
     * Metacard.CREATED
     * Metacard.ID
     * Metacard.TITLE
     * Metacard.POINT_OF_CONTACT
     * Metacard.CONTENT_TYPE
     * Metacard.CONTENT_TYPE_VERSION
     * Metacard.TARGET_NAMESPACE
     * Metacard.METADATA
     * Metacard.RESOURCE_URI
     * Metacard.RESOURCE_DOWNLOAD_URL
     * Metacard.RESOURCE_SIZE
     * Metacard.THUMBNAIL
     * Metacard.GEOGRAPHY
     * Metacard.DESCRIPTION
     * VALIDATION_WARNINGS
     * VALIDATION_ERRORS
     *
     */

    /**
     * NSIL_COMMON Attributes
     * descriptionAbstract -> Metacard.Description
     */

    public NsiliMetacardType() {
        attributeDescriptors.addAll(BasicTypes.BASIC_METACARD.getAttributeDescriptors());
        attributeDescriptors.addAll(NsiliMetacardType.NSILI_DESCRIPTORS);
    }

    @Override
    public String getName() {
        return METACARD_TYPE_NAME;
    }

    @Override
    public Set<AttributeDescriptor> getAttributeDescriptors() {
        return attributeDescriptors;
    }

    @Override
    public AttributeDescriptor getAttributeDescriptor(String attributeName) {
        AttributeDescriptor attributeDescriptor = null;
        for (AttributeDescriptor descriptor : attributeDescriptors) {
            if (descriptor.getName()
                    .equals(attributeName)) {
                attributeDescriptor = descriptor;
                break;
            }
        }
        return attributeDescriptor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NsiliMetacardType that = (NsiliMetacardType) o;

        if (attributeDescriptors != null ?
                !attributeDescriptors.equals(that.attributeDescriptors) :
                that.attributeDescriptors != null) {
            return false;
        }
        return getName() != null ? getName().equals(that.getName()) : that.getName() == null;

    }

    @Override
    public int hashCode() {
        int result = attributeDescriptors != null ? attributeDescriptors.hashCode() : 0;
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        return result;
    }
}
