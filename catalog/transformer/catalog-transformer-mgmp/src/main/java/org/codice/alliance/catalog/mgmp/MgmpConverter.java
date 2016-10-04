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
 **/
package org.codice.alliance.catalog.mgmp;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.ws.Holder;

import org.apache.commons.lang.StringUtils;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.catalog.core.api.types.Security;
import org.codice.ddf.spatial.ogc.csw.catalog.common.GmdConstants;
import org.codice.ddf.spatial.ogc.csw.catalog.converter.AbstractGmdConverter;
import org.codice.ddf.spatial.ogc.csw.catalog.converter.XstreamPathValueTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.io.path.Path;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.types.Associations;
import ddf.catalog.data.types.Contact;
import ddf.catalog.data.types.Core;
import ddf.catalog.data.types.DateTime;
import ddf.catalog.data.types.Location;
import ddf.catalog.data.types.Media;
import ddf.catalog.data.types.Topic;

/**
 * Convert a metacard to MGMPv2 compliant XML.
 * <p>
 * Not thread safe.
 */
public class MgmpConverter extends AbstractGmdConverter {

    private static final String BOUNDING_BOX_FORMAT = "%.15f";

    private static final Logger LOGGER = LoggerFactory.getLogger(MgmpConverter.class);

    private static final int MIN_CLOUD_COVERAGE = 0;

    private static final int MAX_CLOUD_COVERAGE = 100;

    private static final String ID_REGEX = "^.{32}$";

    private static final int ID_SEGMENT_1_START = 0;

    private static final int ID_SEGMENT_1_END = 8;

    private static final int ID_SEGMENT_2_START = 8;

    private static final int ID_SEGMENT_2_END = 12;

    private static final int ID_SEGMENT_3_START = 12;

    private static final int ID_SEGMENT_3_END = 16;

    private static final int ID_SEGMENT_4_START = 16;

    private static final int ID_SEGMENT_4_END = 20;

    private static final int ID_SEGMENT_5_START = 20;

    private static final int ID_SEGMENT_5_END = 32;

    private static final String ID_SEPARATOR = "-";

    private static final Pattern CRS_FILTER_PATTERN = Pattern.compile("^[^:]+:[^:]+$");

    private Predicate<String> crsFilter = s -> CRS_FILTER_PATTERN.matcher(s)
            .matches();

    private int geographicElementIndex = 1;

    private int dateElementIndex = 1;

    private Supplier<String> gmlIdSupplier = this::generateGmlGuid;

    private XstreamPathValueTracker pathValueTracker;

    private MetacardImpl metacard;

    private int resourceContraintsIndex = 1;

    @Override
    protected List<String> getXstreamAliases() {
        return Arrays.asList(GmdConstants.GMD_LOCAL_NAME, MgmpConstants.GMD_METACARD_TYPE_NAME);
    }

    /**
     * Builds up the xml paths and values to write.
     * Order matters!  Paths should be added in the order they must be written.
     *
     * @param sourceMetacard the source
     * @return XstreamPathValueTracker containing XML paths and values to write
     */
    @Override
    protected XstreamPathValueTracker buildPaths(MetacardImpl sourceMetacard) {

        this.metacard = sourceMetacard;
        pathValueTracker = new XstreamPathValueTracker();

        addNamespaces();

        addFileIdentifier();
        addLanguage();
        addCharacterSet();
        addHierarchyLevel();
        addContact();
        addDateStamp();
        addMetadataStandardName();
        addMetadataStandardVersion();
        addCrs();
        addMdIdentification();
        addContentInfo();
        addDistributionInfo();
        addMetadataConstraints();

        return pathValueTracker;
    }

    private void addMetadataConstraints() {
        addMetadataSecurityClassification();
        addOriginatorSecurity();
        addMetadataReleasability();
    }

    private void addDistributionInfo() {
        addGmdMetacardFormat();
        addGmdResourceUri();
    }

    private void addContentInfo() {
        addImageDescription();
    }

    private void addMdIdentification() {
        addMdIdentificationCitation();
        addMdIdentificationAbstract();
        addMdIdentificationStatus();
        addMdIdentificationPointOfContact();
        addMdIdentificationDescriptiveKeywords();
        addMdIdentificationResourceConstraints();
        addMdIdentificationAggregationInfo();
        addMdIdentificationLanguage();
        addMdIdentificationTopicCategories();
        addMdIdentificationExtent();
    }

    private void addMdIdentificationExtent() {
        addGeographicIdentifier();
        addBoundingPolygon();
        addTemporalElements();
        addVerticalElement();
    }

    private void addMdIdentificationResourceConstraints() {
        addResourceSecurityClassification();
        addMdIdentificationResourceConstraintsOriginatorClassification();
        addMdIdentificationResourceConstraintsCaveats();
        resourceContraintsIndex++;
        addDefaultUseLimitations();
        resourceContraintsIndex++;
        addDefaultOtherConstraints();
        resourceContraintsIndex++;
    }

    private void addMdIdentificationResourceConstraintsOriginatorClassification() {
        addFieldIfString(Security.RESOURCE_ORIGINATOR_CLASSIFICATION,
                replaceIndex(MgmpConstants.RESOURCE_ORIGINATOR_SECURITY_PATH,
                        resourceContraintsIndex),
                () -> pathValueTracker.add(new Path(replaceIndex(MgmpConstants.RESOURCE_ORIGINATOR_SECURITY_CODE_LIST_PATH,
                        resourceContraintsIndex)), MgmpConstants.MGMP_CLASSIFICATION_CODE));
    }

    private void addMdIdentificationCitation() {
        addGmdTitle();
        addModifiedDate();
        addCreatedDate();
        addExpirationDate();
        addMdIdentificationUUID();
    }

    private void addMdIdentificationUUID() {
        pathValueTracker.add(new Path(MgmpConstants.DATA_IDENTIFICATION_RS_IDENTIFIER_CODE_PATH),
                formatId(metacard.getId()));
        pathValueTracker.add(new Path(MgmpConstants.DATA_IDENTIFICATION_RS_IDENTIFIER_CODE_SPACE_PATH),
                "UUID");
    }

    private void addMdIdentificationLanguage() {

        List<String> languageCodes = getValues(Core.LANGUAGE);

        if (languageCodes.isEmpty()) {
            languageCodes = Collections.singletonList(Locale.ENGLISH.getISO3Language());
        }

        List<String> languageTexts = languageCodes.stream()
                .map(Locale::forLanguageTag)
                .map(Locale::getDisplayLanguage)
                .collect(Collectors.toList());

        List<String> languageCodeList = createListOfStrings(languageCodes,
                MgmpConstants.LANGUAGE_CODE_LIST);

        addMultiValues(languageCodeList, MgmpConstants.MD_IDENTIFICATION_LANGUAGE_CODE_LIST_PATH);
        addMultiValues(languageCodes,
                MgmpConstants.MD_IDENTIFICATION_LANGUAGE_CODE_LIST_VALUE_PATH);
        addMultiValues(languageTexts, MgmpConstants.MD_IDENTIFICATION_LANGUAGE_TEXT_PATH);

    }

    private void addMdIdentificationAggregationInfo() {
        addMdIdentificationAggregationInfoAggregateDataSetIdentifier();
    }

    private void addMetadataStandardVersion() {
        pathValueTracker.add(new Path(MgmpConstants.MD_METADATA_STANDARD_VERSION_PATH), "2.0");
    }

    private void addMetadataStandardName() {
        pathValueTracker.add(new Path(MgmpConstants.MD_METADATA_STANDARD_NAME_PATH),
                "MOD Geospatial Metadata Profile");
    }

    private void addNamespaces() {
        pathValueTracker.add(new Path(MgmpConstants.GMD_NAMESPACE_PATH),
                MgmpConstants.GMD_NAMESPACE);
        pathValueTracker.add(new Path(MgmpConstants.GCO_NAMESPACE_PATH),
                MgmpConstants.GCO_NAMESPACE);
        pathValueTracker.add(new Path(MgmpConstants.MGMP_NAMESPACE_PATH),
                MgmpConstants.MGMP_NAMESPACE);
        pathValueTracker.add(new Path(MgmpConstants.GML_NAMESPACE_PATH),
                MgmpConstants.GML_NAMESPACE);
        pathValueTracker.add(new Path(MgmpConstants.XLINK_NAMESPACE_PATH),
                MgmpConstants.XLINK_NAMESPACE);
    }

    private void addCharacterSet() {
        pathValueTracker.add(new Path(MgmpConstants.CHARACTER_SET_CODE_LIST_PATH),
                MgmpConstants.MGMP_CHARACTER_SET_CODE);
        pathValueTracker.add(new Path(MgmpConstants.CHARACTER_SET_CODE_LIST_VALUE_PATH),
                MgmpConstants.ENCODING_TYPE);
        pathValueTracker.add(new Path(MgmpConstants.CHARACTER_SET_TEXT_PATH),
                MgmpConstants.ENCODING_DESCRIPTION);
    }

    private void addOriginatorSecurity() {

        addFieldIfString(Security.METADATA_ORIGINATOR_CLASSIFICATION,
                MgmpConstants.METADATA_ORIGINATOR_SECURITY_PATH,
                () -> pathValueTracker.add(new Path(MgmpConstants.METADATA_ORIGINATOR_CLASSIFICATION_CODE_LIST_PATH),
                        MgmpConstants.CLASSIFICATION_CODE));

    }

    private void addCrs() {

        List<String> validCrsCodes = getValues(Location.COORDINATE_REFERENCE_SYSTEM_CODE).stream()
                .filter(crsFilter)
                .collect(Collectors.toList());

        addMultiValues(validCrsCodes.stream()
                .map(value -> value.split(":")[1])
                .collect(Collectors.toList()), MgmpConstants.GMD_CRS_CODE_PATH);

        addMultiValues(validCrsCodes.stream()
                .map(value -> value.split(":")[0])
                .collect(Collectors.toList()), MgmpConstants.GMD_CRS_AUTHORITY_PATH);

    }

    private void addBoundingPolygonFromLocation(String location) {
        try {
            Geometry geometry = new WKTReader().read(location);

            Optional<String> str = Optional.of(Stream.of(geometry.getCoordinates())
                    .flatMap(coordinate -> Stream.of(coordinate.x, coordinate.y))
                    .map(d -> Double.toString(d))
                    .collect(Collectors.joining(" ")));

            pathValueTracker.add(new Path(replaceIndex(MgmpConstants.GMD_POLYGON_GMLID_PATH,
                    geographicElementIndex)), gmlIdSupplier.get());
            pathValueTracker.add(new Path(replaceIndex(MgmpConstants.GMD_POLYGON_SRSNAME_PATH,
                    geographicElementIndex)), MgmpConstants.GMD_POLYGON_SRSNAME);

            addFieldIfString(() -> str,
                    replaceIndex(MgmpConstants.BOUNDING_POLYGON_PATH, geographicElementIndex));

            geographicElementIndex++;

            Coordinate[] coords = geometry.getCoordinates();

            OptionalDouble westBoundLongitude = Stream.of(coords)
                    .mapToDouble(coord -> coord.x)
                    .min();

            OptionalDouble eastBoundLongitude = Stream.of(coords)
                    .mapToDouble(coord -> coord.x)
                    .max();

            OptionalDouble northBoundLatitude = Stream.of(coords)
                    .mapToDouble(coord -> coord.y)
                    .max();

            OptionalDouble southBoundLatitude = Stream.of(coords)
                    .mapToDouble(coord -> coord.y)
                    .min();

            boolean allSet = Stream.of(westBoundLongitude,
                    eastBoundLongitude,
                    northBoundLatitude,
                    southBoundLatitude)
                    .allMatch(OptionalDouble::isPresent);

            if (allSet) {
                addBoundingBoxElement(MgmpConstants.WEST_BOUND_LONGITUDE_PATH,
                        westBoundLongitude.getAsDouble());
                addBoundingBoxElement(MgmpConstants.EAST_BOUND_LONGITUDE_PATH,
                        eastBoundLongitude.getAsDouble());
                addBoundingBoxElement(MgmpConstants.SOUTH_BOUND_LATITUDE_PATH,
                        southBoundLatitude.getAsDouble());
                addBoundingBoxElement(MgmpConstants.NORTH_BOUND_LATITUDE_PATH,
                        northBoundLatitude.getAsDouble());

                geographicElementIndex++;
            }

        } catch (ParseException e) {
            LOGGER.debug("unable to set location: wkt={}", location, e);
        }
    }

    private void addBoundingPolygon() {
        getValues(Core.LOCATION).stream()
                .findFirst()
                .ifPresent(this::addBoundingPolygonFromLocation);
    }

    private void addBoundingBoxElement(String path, Double value) {
        pathValueTracker.add(new Path(replaceIndex(path, geographicElementIndex)),
                String.format(BOUNDING_BOX_FORMAT, value));
    }

    private void addGeographicIdentifier() {
        addMultiValues(getValues(Location.COUNTRY_CODE),
                MgmpConstants.GMD_COUNTRY_CODE_PATH,
                geographicElementIndex);
        addMultiValues(createListOfStrings(getValues(Location.COUNTRY_CODE), "ISO3166-1-a3"),
                MgmpConstants.GMD_COUNTRY_CODE_SPACE_PATH,
                geographicElementIndex);
        geographicElementIndex += getValues(Location.COUNTRY_CODE).size();
    }

    private void addMdIdentificationStatus() {
        addFieldIfString(GmdConstants.RESOURCE_STATUS,
                GmdConstants.RESOURCE_STATUS_PATH,
                () -> pathValueTracker.add(new Path(MgmpConstants.RESOURCE_STATUS_CODE_LIST_PATH),
                        MgmpConstants.MGMP_PROGRESS_CODE));
    }

    private String generateGmlGuid() {
        return "GMLID_" + UUID.randomUUID()
                .toString();
    }

    private void addTemporalElements() {

        List<String> starts = getDateStrings(DateTime.START);
        List<String> ends = getDateStrings(DateTime.END);

        if (starts.size() == ends.size()) {
            for (int i = 0; i < starts.size(); i++) {

                String start = starts.get(i);
                String end = ends.get(i);

                int elementIndex = i + 1;

                if (start.equals(end)) {
                    pathValueTracker.add(new Path(replaceIndex(MgmpConstants.GMD_TEMPORAL_TIME_INSTANT_ID_PATH,
                            elementIndex)), gmlIdSupplier.get());
                    pathValueTracker.add(new Path(replaceIndex(MgmpConstants.GMD_TEMPORAL_INSTANT_PATH,
                            elementIndex)), start);
                } else {
                    pathValueTracker.add(new Path(replaceIndex(MgmpConstants.GMD_TEMPORAL_TIME_PERIOD_ID_PATH,
                            elementIndex)), gmlIdSupplier.get());
                    pathValueTracker.add(new Path(replaceIndex(MgmpConstants.GMD_TEMPORAL_START_PATH,
                            elementIndex)), start);
                    pathValueTracker.add(new Path(replaceIndex(MgmpConstants.GMD_TEMPORAL_END_PATH,
                            elementIndex)), end);
                }
            }
        }
    }

    private void addVerticalElement() {

        final Holder<Boolean> addVerticalCRS = new Holder<>(false);

        addFieldIfString(() -> {
            OptionalDouble optionalDouble = getSerializables(Location.ALTITUDE).stream()
                    .filter(Double.class::isInstance)
                    .mapToDouble(Double.class::cast)
                    .min();
            if (optionalDouble.isPresent()) {
                addVerticalCRS.value = true;
                return Optional.of(Double.toString(optionalDouble.getAsDouble()));
            }
            return Optional.empty();
        }, MgmpConstants.GMD_MIN_ALTITUDE_PATH);

        addFieldIfString(() -> {
            OptionalDouble optionalDouble = getSerializables(Location.ALTITUDE).stream()
                    .filter(Double.class::isInstance)
                    .mapToDouble(Double.class::cast)
                    .max();
            if (optionalDouble.isPresent()) {
                addVerticalCRS.value = true;
                return Optional.of(Double.toString(optionalDouble.getAsDouble()));
            }
            return Optional.empty();
        }, GmdConstants.ALTITUDE_PATH);

        if (addVerticalCRS.value) {
            pathValueTracker.add(new Path(MgmpConstants.GMD_VERTICAL_CRS_XLINK_HREF_PATH),
                    "http://www.opengis.net/def/crs/EPSG/0/5701");
            pathValueTracker.add(new Path(MgmpConstants.GMD_VERTICAL_CRS_XLINK_TITLE_PATH),
                    "Newlyn Height");
        }
    }

    private void addCreatedDate() {
        addFieldIfString(() -> Optional.ofNullable(metacard.getCreatedDate())
                        .map(this::dateToIso8601),
                replaceIndex(MgmpConstants.DATE_PATH, dateElementIndex),
                this::addCreatedDateExtra);
    }

    private void addCreatedDateExtra() {
        addDateTypeCode(GmdConstants.CREATION, "Creation");
        dateElementIndex++;
    }

    private void addExpirationDate() {
        addFieldIfString(() -> Optional.ofNullable(metacard.getExpirationDate())
                        .map(this::dateToIso8601),
                replaceIndex(MgmpConstants.DATE_PATH, dateElementIndex),
                this::addExpirationDateExtra);
    }

    private void addExpirationDateExtra() {
        addDateTypeCode(GmdConstants.EXPIRY, "Expiry");
        dateElementIndex++;
    }

    private void addDateTypeCode(String dateTypeCodeValue, String dateTypeText) {
        pathValueTracker.add(new Path(replaceIndex(MgmpConstants.DATE_TYPE_CODE_VALUE_PATH,
                dateElementIndex)), dateTypeCodeValue);
        pathValueTracker.add(new Path(replaceIndex(MgmpConstants.DATE_TYPE_CODE_PATH,
                dateElementIndex)), MgmpConstants.MGMP_DATE_TYPE_CODE);
        pathValueTracker.add(new Path(replaceIndex(MgmpConstants.DATE_TYPE_CODE_TEXT_PATH,
                dateElementIndex)), dateTypeText);
    }

    private void addModifiedDate() {
        addFieldIfString(() -> Optional.ofNullable(metacard.getModifiedDate())
                        .map(this::dateToIso8601),
                replaceIndex(MgmpConstants.DATE_PATH, dateElementIndex),
                this::addModifiedDateExtra);
    }

    private void addModifiedDateExtra() {
        addDateTypeCode(GmdConstants.LAST_UPDATE, "LastUpdate");
        dateElementIndex++;
    }

    private void addDateStamp() {

        Optional<Date> modified = getOptionalDate(Core.METACARD_MODIFIED);
        Optional<Date> created = getOptionalDate(Core.METACARD_CREATED);

        Optional<Date> date = modified.isPresent() ? modified : created;

        date = date.isPresent() ? date : Optional.of(new Date());

        date.ifPresent(date1 -> pathValueTracker.add(new Path(GmdConstants.DATE_TIME_STAMP_PATH),
                dateToIso8601(date1)));
    }

    private void addMdIdentificationPointOfContact() {

        addFieldIfString(Contact.POINT_OF_CONTACT_NAME, MgmpConstants.POINT_OF_CONTACT_NAME_PATH);

        addMultiValues(getValues(Contact.POINT_OF_CONTACT_PHONE),
                MgmpConstants.POINT_OF_CONTACT_PHONE_PATH);

        addMultiValues(getValues(Contact.POINT_OF_CONTACT_ADDRESS),
                MgmpConstants.POINT_OF_CONTACT_ADDRESS_PATH);

        addMultiValues(getValues(Contact.POINT_OF_CONTACT_EMAIL),
                MgmpConstants.POINT_OF_CONTACT_EMAIL_PATH);

        pathValueTracker.add(new Path(MgmpConstants.POINT_OF_CONTACT_ROLE_CODE_LIST_PATH),
                MgmpConstants.MGMP_ROLE_CODE);
        pathValueTracker.add(new Path(MgmpConstants.POINT_OF_CONTACT_ROLE_CODE_LIST_VALUE_PATH),
                "originator");
        pathValueTracker.add(new Path(MgmpConstants.POINT_OF_CONTACT_ROLE_TEXT_PATH), "Originator");

    }

    private void addContact() {

        addFieldIfString(Contact.PUBLISHER_NAME, GmdConstants.CONTACT_ORGANISATION_PATH);

        addMultiValues(getValues(Contact.PUBLISHER_PHONE), MgmpConstants.GMD_CONTACT_PHONE_PATH);

        addMultiValues(getValues(Contact.PUBLISHER_ADDRESS),
                MgmpConstants.GMD_POINT_OF_CONTACT_ADDRESS_DELIVERY_POINT_PATH);

        addMultiValues(getValues(Contact.PUBLISHER_EMAIL), MgmpConstants.GMD_CONTACT_EMAIL_PATH);

        pathValueTracker.add(new Path(MgmpConstants.CONTACT_ROLE_CODE_LIST_PATH),
                MgmpConstants.MGMP_ROLE_CODE);
        pathValueTracker.add(new Path(MgmpConstants.CONTACT_ROLE_CODE_LIST_VALUE_PATH),
                "pointOfContact");
        pathValueTracker.add(new Path(MgmpConstants.CONTACT_ROLE_TEXT_PATH), "Point of Contact");

    }

    private void addGmdResourceUri() {
        addFieldIfString(Core.RESOURCE_DOWNLOAD_URL, GmdConstants.LINKAGE_URI_PATH);
    }

    private String formatId(String id) {
        if (id.matches(ID_REGEX)) {
            return id.substring(ID_SEGMENT_1_START, ID_SEGMENT_1_END) + ID_SEPARATOR + id.substring(
                    ID_SEGMENT_2_START,
                    ID_SEGMENT_2_END) + ID_SEPARATOR + id.substring(ID_SEGMENT_3_START,
                    ID_SEGMENT_3_END) + ID_SEPARATOR + id.substring(ID_SEGMENT_4_START,
                    ID_SEGMENT_4_END) + ID_SEPARATOR + id.substring(ID_SEGMENT_5_START,
                    ID_SEGMENT_5_END);
        }
        return id;
    }

    private void addMdIdentificationAggregationInfoAggregateDataSetIdentifier() {

        addFieldIfString(() -> Optional.ofNullable(metacard.getAttribute(Associations.RELATED))
                        .map(Attribute::getValue)
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .map(this::formatId),
                GmdConstants.ASSOCIATION_PATH,
                this::addMdIdentificationAggregationInfoAggregateDataSetIdentifierExtra);
    }

    private void addMdIdentificationAggregationInfoAggregateDataSetIdentifierExtra() {
        pathValueTracker.add(new Path(MgmpConstants.ASSOCIATIONS_RELATED_CODE_SPACE_PATH),
                "UUIDCollectiveProduct");
        pathValueTracker.add(new Path(MgmpConstants.ASSOCIATIONS_RELATED_TYPE_CODE_LIST_PATH),
                MgmpConstants.MGMP_ASSOCIATION_TYPE_CODE);
        pathValueTracker.add(new Path(MgmpConstants.ASSOCIATIONS_RELATED_TYPE_CODE_LIST_VALUE_PATH),
                "mediaAssociation");
        pathValueTracker.add(new Path(MgmpConstants.ASSOCIATIONS_RELATED_TYPE_TEXT_PATH),
                "Media Association");
    }

    private void addMdIdentificationTopicCategories() {

        List<String> topics = getValues(Topic.CATEGORY);

        if (!topics.isEmpty()) {
            addMultiValues(getValues(Topic.CATEGORY), MgmpConstants.GMD_TOPIC_CATEGORY_PATH);
        } else {
            pathValueTracker.add(new Path(MgmpConstants.GMD_EMPTY_TOPIC_CATEGORY_PATH), "");
        }
    }

    private void addMdIdentificationDescriptiveKeywords() {

        List<String> keywords = getValues(Topic.KEYWORD);

        if (!keywords.isEmpty()) {
            addMultiValues(keywords, MgmpConstants.GMD_KEYWORD_PATH);
        } else {
            pathValueTracker.add(new Path(MgmpConstants.GMD_EMPTY_KEYWORDS_PATH), "");
        }
    }

    private void addGmdMetacardFormat() {
        addFieldIfString(Media.FORMAT, GmdConstants.FORMAT_PATH);
        addFieldIfString(Media.FORMAT_VERSION, GmdConstants.FORMAT_VERSION_PATH);
    }

    private void addMdIdentificationAbstract() {
        addFieldIfString(() -> {
            String description = metacard.getDescription();
            return Optional.of(description != null ? description : "");
        }, GmdConstants.ABSTRACT_PATH);
    }

    private void addGmdTitle() {
        addFieldIfString(() -> Optional.of(StringUtils.defaultString(metacard.getTitle())),
                GmdConstants.TITLE_PATH);
    }

    private void addFileIdentifier() {
        addFieldIfString(() -> Optional.of(StringUtils.defaultString(metacard.getId()))
                .map(this::formatId), GmdConstants.FILE_IDENTIFIER_PATH);
    }

    private void addLanguage() {

        Optional<String> metacardLanguage =
                Optional.ofNullable(metacard.getAttribute(Core.LANGUAGE))
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .map(String::toLowerCase);

        String lang = metacardLanguage.orElse(Locale.getDefault()
                .getISO3Language()
                .toLowerCase());

        String displayLanguage = metacardLanguage.map(s -> Locale.forLanguageTag(s)
                .getDisplayLanguage())
                .orElse(Locale.getDefault()
                        .getDisplayLanguage());

        pathValueTracker.add(new Path(MgmpConstants.LANGUAGE_CODE_LIST_PATH),
                MgmpConstants.LANGUAGE_CODE_LIST);
        pathValueTracker.add(new Path(MgmpConstants.LANGUAGE_CODE_LIST_VALUE_PATH), lang);
        pathValueTracker.add(new Path(MgmpConstants.LANGUAGE_CODE_PATH), displayLanguage);
    }

    private void addHierarchyLevel() {
        pathValueTracker.add(new Path(GmdConstants.CODE_LIST_VALUE_PATH), "dataset");
        pathValueTracker.add(new Path(GmdConstants.CODE_LIST_PATH), MgmpConstants.MGMP_SCOPE_CODE);
    }

    @Override
    protected String getRootNodeName() {
        return GmdConstants.GMD_LOCAL_NAME;
    }

    private void addMetadataReleasability() {
        Attribute releasibilityAttribute = metacard.getAttribute(Security.METADATA_RELEASABILITY);
        Attribute disseminationAttribute = metacard.getAttribute(Security.METADATA_DISSEMINATION);

        if (isReleasabilityAndDisseminationSet(releasibilityAttribute, disseminationAttribute)) {

            String value = disseminationAttribute.getValue()
                    .toString() + " " + String.join("/",
                    releasibilityAttribute.getValues()
                            .stream()
                            .map(Objects::toString)
                            .collect(Collectors.toList()));

            pathValueTracker.add(new Path(MgmpConstants.METADATA_RELEASABILITY_PATH), value);

        }

    }

    private boolean isReleasabilityAndDisseminationSet(Attribute releasibilityAttribute,
            Attribute disseminationAttribute) {
        return releasibilityAttribute != null && disseminationAttribute != null
                && releasibilityAttribute.getValues() != null && !releasibilityAttribute.getValues()
                .isEmpty() && StringUtils.isNotBlank((String) disseminationAttribute.getValue());
    }

    private void addMdIdentificationResourceConstraintsCaveats() {

        Attribute releasibilityAttribute = metacard.getAttribute(Security.RESOURCE_RELEASABILITY);
        Attribute disseminationAttribute = metacard.getAttribute(Security.RESOURCE_DISSEMINATION);

        if (isReleasabilityAndDisseminationSet(releasibilityAttribute, disseminationAttribute)) {

            String value = disseminationAttribute.getValue()
                    .toString() + " " + String.join("/",
                    releasibilityAttribute.getValues()
                            .stream()
                            .map(Objects::toString)
                            .collect(Collectors.toList()));

            pathValueTracker.add(new Path(replaceIndex(MgmpConstants.RESOURCE_SECURITY_RELEASABILITY_PATH,
                    resourceContraintsIndex)), value);

        }
    }

    private void addDefaultUseLimitations() {
        pathValueTracker.add(new Path(replaceIndex(MgmpConstants.USE_LIMITATIONS_PATH,
                resourceContraintsIndex)),
                "No known limitations; see Legal and Security Constraints.");
    }

    private void addDefaultOtherConstraints() {
        pathValueTracker.add(new Path(replaceIndex(MgmpConstants.LEGAL_CONSTRAINTS_PATH,
                resourceContraintsIndex)),
                "No known Legal Constraints, please refer to Security Constraints.");
    }

    private void addResourceSecurityClassification() {
        addFieldIfString(Security.RESOURCE_CLASSIFICATION,
                replaceIndex(MgmpConstants.RESOURCE_SECURITY_PATH, resourceContraintsIndex),
                () -> pathValueTracker.add(new Path(replaceIndex(MgmpConstants.RESOURCE_SECURITY_CODE_LIST_PATH,
                        resourceContraintsIndex)), MgmpConstants.CLASSIFICATION_CODE));

    }

    private void addMetadataSecurityClassification() {
        addFieldIfString(Security.METADATA_CLASSIFICATION,
                MgmpConstants.METADATA_SECURITY_PATH,
                () -> pathValueTracker.add(new Path(MgmpConstants.METADATA_SECURITY_CODE_LIST_PATH),
                        MgmpConstants.CLASSIFICATION_CODE));
    }

    private void addImageDescription() {

        Optional<String> cloudCoverage = Optional.ofNullable(metacard.getAttribute(Isr.CLOUD_COVER))
                .map(Attribute::getValue)
                .filter(Integer.class::isInstance)
                .map(Integer.class::cast)
                .filter(integer -> integer >= MIN_CLOUD_COVERAGE && integer <= MAX_CLOUD_COVERAGE)
                .map(integer -> Integer.toString(integer));

        boolean isCloudCoverageAvailable = cloudCoverage.isPresent();

        List<Serializable> ratingScaleValues =
                getSerializables(Isr.NATIONAL_IMAGERY_INTERPRETABILITY_RATING_SCALE);

        boolean isRatingScaleValuesAvailable = ratingScaleValues.size() == 1;

        if (isCloudCoverageAvailable || isRatingScaleValuesAvailable) {

            List<String> attributeDescription = Stream.of(metacard.getAttribute(Isr.COMMENTS))
                    .flatMap(attribute -> attribute.getValues()
                            .stream())
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .collect(Collectors.toList());

            if (attributeDescription.size() != 1) {
                pathValueTracker.add(new Path(MgmpConstants.CONTENT_INFO_MGMP_IMAGE_DESCRIPTION_CLOUD_COVERAGE_ATTRIBUTE_REASON_PATH),
                        "unknown");
            } else {
                pathValueTracker.add(new Path(MgmpConstants.ISR_IMAGE_COMMENT_PATH),
                        attributeDescription.get(0));
            }

            pathValueTracker.add(new Path(MgmpConstants.CONTENT_INFO_MGMP_IMAGE_DESCRIPTION_CLOUD_COVERAGE_CONTENT_TYPE_TEXT_PATH),
                    "Image");
            pathValueTracker.add(new Path(MgmpConstants.CONTENT_INFO_MGMP_IMAGE_DESCRIPTION_CLOUD_COVERAGE_CONTENT_TYPE_CODE_LIST_PATH),
                    MgmpConstants.MGMP_COVERAGE_CONTENT_TYPE_CODE);
            pathValueTracker.add(new Path(MgmpConstants.ISR_IMAGE_DESCRIPTION_PATH), "image");
        }

        if (!ratingScaleValues.isEmpty()) {
            ratingScaleValues.stream()
                    .map(Object::toString)
                    .findFirst()
                    .ifPresent(value -> {
                        pathValueTracker.add(new Path(MgmpConstants.NIIRS_RATING_PATH),
                                MgmpConstants.NIIRS);
                        pathValueTracker.add(new Path(MgmpConstants.NIIRS_PATH), value);
                    });

        }

        addFieldIfString(() -> cloudCoverage, MgmpConstants.CLOUD_COVERAGE_PATH);
    }

    void setGmlIdSupplier(Supplier<String> gmlIdSupplier) {
        this.gmlIdSupplier = gmlIdSupplier;
    }

    /**
     * Return a list of strings where the list contains {@code list.size()} elements
     * with the string {@code value}.
     */
    private List<String> createListOfStrings(List<String> list, String value) {
        return Collections.nCopies(list.size(), value);
    }

    private Optional<Date> getOptionalDate(String dateField) {
        return Optional.ofNullable(metacard.getAttribute(dateField))
                .map(Attribute::getValue)
                .filter(Date.class::isInstance)
                .map(Date.class::cast);
    }

    private String replaceIndex(String template, int index) {
        return template.replace(MgmpConstants.INDEX_TAG, Integer.toString(index));
    }

    private List<Serializable> getSerializables(String attributeName) {
        return Optional.of(attributeName)
                .map(metacard::getAttribute)
                .map(Attribute::getValues)
                .map(serializables -> serializables.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    private List<String> getValues(String attributeName) {
        return getSerializables(attributeName).stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .collect(Collectors.toList());
    }

    private void addMultiValues(List<String> values, String xpathTemplate) {
        addMultiValues(values, xpathTemplate, 1);
    }

    private void addMultiValues(List<String> values, String xpathTemplate, int startIndex) {
        int index = startIndex;
        for (String value : values) {
            pathValueTracker.add(new Path(replaceIndex(xpathTemplate, index)), value);
            index++;
        }
    }

    private List<String> getDateStrings(String attributeName) {
        return getSerializables(attributeName).stream()
                .filter(Date.class::isInstance)
                .map(Date.class::cast)
                .map(this::dateToIso8601)
                .collect(Collectors.toList());
    }

    /**
     * @param attributeName the attribute source
     * @param xpath         the xpath destination
     * @param extraAdd      execute this BiConsumer if the value string is being applied to the pathValueTracker
     * @return true if the value was added, false otherwise
     */
    private boolean addFieldIfString(String attributeName, String xpath, Runnable extraAdd) {
        return addFieldIfString(() -> Optional.ofNullable(metacard.getAttribute(attributeName))
                .map(Attribute::getValue)
                .filter(String.class::isInstance)
                .map(String.class::cast), xpath, extraAdd);
    }

    private boolean addFieldIfString(String attributeName, String xpath) {
        return addFieldIfString(attributeName, xpath, () -> {
        });
    }

    private boolean addFieldIfString(Supplier<Optional<String>> metacardFunction, String xpath,
            Runnable extraAdd) {
        return metacardFunction.get()
                .map(value -> {
                    pathValueTracker.add(new Path(xpath), value);
                    extraAdd.run();
                    return true;
                })
                .orElse(false);
    }

    private boolean addFieldIfString(Supplier<Optional<String>> metacardFunction, String xpath) {
        return addFieldIfString(metacardFunction, xpath, () -> {
        });
    }

    private String dateToIso8601(Date date) {
        GregorianCalendar modifiedCal = new GregorianCalendar();
        if (date != null) {
            modifiedCal.setTime(date);
        }
        modifiedCal.setTimeZone(UTC_TIME_ZONE);

        return XSD_FACTORY.newXMLGregorianCalendar(modifiedCal)
                .toXMLFormat();
    }

}