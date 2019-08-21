/*
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
package org.codice.alliance.ddms.v2;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.types.Associations;
import ddf.catalog.data.types.Contact;
import ddf.catalog.data.types.Core;
import ddf.catalog.data.types.DateTime;
import ddf.catalog.data.types.Location;
import ddf.catalog.data.types.Media;
import ddf.catalog.data.types.Topic;
import ddf.catalog.data.types.Validation;
import ddf.catalog.transform.CatalogTransformerException;
import ddf.catalog.transform.InputTransformer;
import ddf.catalog.validation.ValidationException;
import java.io.InputStream;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.lang3.StringUtils;
import org.codice.alliance.catalog.core.api.types.Security;
import org.codice.alliance.ddms.types.Ddms20MetacardType;
import org.codice.alliance.ddms.types.RestrictionAttributes;
import org.codice.alliance.ddms.wkt.GeometryCollectionBuilder;
import org.codice.ddf.platform.util.XMLUtils;
import org.codice.ddf.transformer.xml.streaming.Gml3ToWkt;
import org.codice.ddms.DdmsDate;
import org.codice.ddms.DdmsResource;
import org.codice.ddms.gml.v3.Point;
import org.codice.ddms.gml.v3.Polygon;
import org.codice.ddms.v2.reader.Ddms20XmlReader;
import org.codice.ddms.v2.resource.Identifier;
import org.codice.ddms.v2.resource.Language;
import org.codice.ddms.v2.resource.Title;
import org.codice.ddms.v2.resource.Type;
import org.codice.ddms.v2.security.ism.SecurityAttributes;
import org.codice.ddms.v2.summary.Category;
import org.codice.ddms.v2.summary.GeospatialCoverage;
import org.codice.ddms.v2.summary.Link;
import org.codice.ddms.v2.summary.RelatedResources;
import org.codice.ddms.v2.summary.TemporalCoverage;
import org.codice.ddms.v2.summary.geospatial.BoundingBox;
import org.codice.ddms.v2.summary.geospatial.BoundingGeometry;
import org.codice.ddms.v2.summary.geospatial.CountryCode;
import org.codice.ddms.v2.summary.geospatial.FacilityIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ddms20InputTransformer implements InputTransformer {
  private static final Logger LOGGER = LoggerFactory.getLogger(Ddms20InputTransformer.class);

  private static final String DCMI_TYPE_NAMESPACE = "http://purl.org/dc/terms/DCMIType/";

  private static final String RELATED_ASSOCIATION = "urn:catalog:metacard:association:related";

  private static final String DERIVED_ASSOCIATION = "urn:catalog:metacard:association:derived";

  private static final MetacardType DDMS_20_METACARD_TYPE = new Ddms20MetacardType();

  private Gml3ToWkt gml3ToWkt;

  public Ddms20InputTransformer(Gml3ToWkt gml3ToWkt) {
    this.gml3ToWkt = gml3ToWkt;
  }

  @Override
  public Metacard transform(InputStream input) throws CatalogTransformerException {
    return transform(input, null);
  }

  @Override
  public Metacard transform(InputStream input, String id) throws CatalogTransformerException {
    MetacardImpl metacard = new MetacardImpl(DDMS_20_METACARD_TYPE);
    metacard.setId(id);

    DdmsResource ddms = parseDdms(input);

    Set<Serializable> tags = new HashSet<>();
    List<Serializable> validationErrors = new ArrayList<>();

    // Core attributes
    metacard.setAttribute(getTitle(ddms));
    metacard.setAttribute(getResourceUri(ddms));
    metacard.setAttribute(getResourceDownloadUrl(ddms));
    metacard.setAttribute(getResourceSize(ddms));
    metacard.setAttribute(getDescription(ddms));
    metacard.setAttribute(getCreated(ddms));
    metacard.setAttribute(getLanguage(ddms));
    metacard.setAttribute(getDatatype(ddms));
    try {
      metacard.setAttribute(getLocation(ddms));
    } catch (ValidationException e) {
      LOGGER.warn("Failed to extract location from DDMS", e);
      tags.add("INVALID");
      validationErrors.add(e.getMessage());
    }

    // Associated attributes
    metacard.setAttribute(getDerivedAssociations(ddms));
    metacard.setAttribute(getRelatedAssociations(ddms));
    metacard.setAttribute(getExternalAssocations(ddms));

    // Datetime attributes
    metacard.setAttribute(getDatetimeNames(ddms));
    metacard.setAttribute(getDatetimeStart(ddms));
    metacard.setAttribute(getDatetimeEnd(ddms));

    // Contact attributes
    metacard.setAttribute(getCreatorNames(ddms));
    metacard.setAttribute(getCreatorEmails(ddms));
    metacard.setAttribute(getCreatorPhones(ddms));
    metacard.setAttribute(getPublisherNames(ddms));
    metacard.setAttribute(getPublisherEmails(ddms));
    metacard.setAttribute(getPublisherPhones(ddms));
    metacard.setAttribute(getContributorNames(ddms));
    metacard.setAttribute(getContributorEmails(ddms));
    metacard.setAttribute(getContributorPhones(ddms));
    metacard.setAttribute(getPointOfContactNames(ddms));
    metacard.setAttribute(getPointOfContactEmails(ddms));
    metacard.setAttribute(getPointOfContactPhones(ddms));

    // Location attributes
    metacard.setAttribute(getCountryCodes(ddms));
    metacard.setAttribute(getCrsNames(ddms));
    // TODO (CAL-519): location.altitude-meters is a content extension

    // Media attributes
    metacard.setAttribute(getMediaFormat(ddms));
    metacard.setAttribute(getMediaType(ddms));

    // Topic attributes
    metacard.setAttribute(getTopicCategories(ddms));
    metacard.setAttribute(getTopicKeywords(ddms));
    metacard.setAttribute(getTopicVocabulary(ddms));

    // @TODO (CAL-519): isr.frequency-hertz is a content extension

    // Security attributes
    SecurityAttributes securityAttributes = ddms.getSecurity().getSecurityAttributes();
    metacard.setAttribute(getClassification(securityAttributes));
    metacard.setAttribute(getReleasability(securityAttributes));
    metacard.setAttribute(getOwnerProducers(securityAttributes));
    metacard.setAttribute(getDisseminationControls(securityAttributes));
    metacard.setAttribute(getSciControls(securityAttributes));
    metacard.setAttribute(getNonIcMarkings(securityAttributes));

    // Restriction attributes
    metacard.setAttribute(getCopyright(ddms));
    metacard.setAttribute(getIntellectualProperty(ddms));
    metacard.setAttribute(getPrivacyAct(ddms));

    // Ddms attributes
    metacard.setAttribute(getInfoCutOff(ddms));
    metacard.setAttribute(getPosted(ddms));
    metacard.setAttribute(getGeographicNames(ddms));
    metacard.setAttribute(getGeographicRegions(ddms));
    metacard.setAttribute(getBeNumbers(ddms));
    metacard.setAttribute(getSubtitles(ddms));
    // TODO (CAL-519): Add handlers for these attributes
    // TODO (CAL-519): ext.ddms.icid-payload
    // TODO (CAL-519): ext.ddms.security portion markings
    // TODO (CAL-519): Portion-Markings: Title, Subtitle, description, contacts, related resources,
    // security
    // TODO (CAL-519): ext.ddms.related-resource not used?
    // TODO (CAL-519): Not sure what ext.ddms.topic-type is for

    // Validation attributes
    metacard.setAttribute(Core.METACARD_TAGS, new ArrayList<>(tags));
    metacard.setAttribute(new AttributeImpl(Validation.VALIDATION_ERRORS, validationErrors));

    return metacard;
  }

  private DdmsResource parseDdms(InputStream input) throws CatalogTransformerException {
    try {
      XMLStreamReader xmlStreamReader =
          XMLUtils.getInstance().getSecureXmlInputFactory().createXMLStreamReader(input);
      return new Ddms20XmlReader(xmlStreamReader).read();
    } catch (XMLStreamException e) {
      throw new CatalogTransformerException("Failed to parse DDMS 2.0", e);
    }
  }

  private Attribute getTitle(DdmsResource ddms) {
    String title = ddms.getTitles().stream().findFirst().map(Title::getValue).orElse(null);
    return new AttributeImpl(Core.TITLE, title);
  }

  private Attribute getLocation(DdmsResource ddms) throws ValidationException {
    GeometryCollectionBuilder geometryCollectionBuilder = new GeometryCollectionBuilder();
    for (GeospatialCoverage geospatialCoverage : ddms.getGeospatialCoverages()) {
      for (BoundingBox boundingBox : geospatialCoverage.getBoundingBoxes()) {
        geometryCollectionBuilder.polygon(
            boundingBox.getWest(),
            boundingBox.getNorth(),
            boundingBox.getEast(),
            boundingBox.getNorth(),
            boundingBox.getEast(),
            boundingBox.getSouth(),
            boundingBox.getWest(),
            boundingBox.getSouth(),
            boundingBox.getWest(),
            boundingBox.getNorth());
      }

      for (BoundingGeometry geometry : geospatialCoverage.getBoundingGeometries()) {
        for (Polygon p : geometry.getPolygons()) {
          geometryCollectionBuilder.addWkt(gml3ToWkt.convert(p.toString()));
        }

        for (Point p : geometry.getPoints()) {
          geometryCollectionBuilder.addWkt(gml3ToWkt.convert(p.toString()));
        }
      }
    }

    // Can't return blank string or the Solr Catalog Provider will throw a NullPointerException
    String location = geometryCollectionBuilder.toString();
    return (StringUtils.isNotBlank(location)) ? new AttributeImpl(Core.LOCATION, location) : null;
  }

  private Attribute getResourceUri(DdmsResource ddms) {
    String resourceUri =
        ddms.getIdentifiers()
            .stream()
            .filter(identifier -> "urn:catalog:resource-uri".equals(identifier.getQualifier()))
            .map(Identifier::getValue)
            .findFirst()
            .orElse(null);
    return new AttributeImpl(Core.RESOURCE_URI, resourceUri);
  }

  private Attribute getResourceDownloadUrl(DdmsResource ddms) {
    String resourceDownloadUrl =
        ddms.getRelatedResources()
            .stream()
            .filter(relatedResources -> "resource".equals(relatedResources.getRelationship()))
            .flatMap(resources -> resources.getResources().stream())
            .flatMap(resource -> resource.getLinks().stream())
            .map(Link::getHref)
            .findFirst()
            .orElse(null);
    return new AttributeImpl(Core.RESOURCE_DOWNLOAD_URL, resourceDownloadUrl);
  }

  private Attribute getResourceSize(DdmsResource ddms) {
    String resourceSize = null;
    if (ddms.getFormat() != null) {
      resourceSize = ddms.getFormat().getExtent().getValue();
    }
    return new AttributeImpl(Core.RESOURCE_SIZE, resourceSize);
  }

  private Attribute getDescription(DdmsResource ddms) {
    String description = null;
    if (ddms.getDescription() != null) {
      description = ddms.getDescription().getValue();
    }
    return new AttributeImpl(Core.DESCRIPTION, description);
  }

  private Attribute getCreated(DdmsResource ddms) {
    Date created = new Date();
    if (ddms.getDates() != null) {
      created = convertDdmsDate(ddms.getDates().getCreated());
    }
    return new AttributeImpl(Core.CREATED, created);
  }

  private Attribute getLanguage(DdmsResource ddms) {
    String language =
        ddms.getLanguages()
            .stream()
            .map(Language::getValue)
            .filter(StringUtils::isNotBlank)
            .findFirst()
            .orElse(null);
    return new AttributeImpl(Core.LANGUAGE, language);
  }

  private Attribute getDatatype(DdmsResource ddms) {
    String datatype =
        ddms.getTypes()
            .stream()
            .filter(type -> DCMI_TYPE_NAMESPACE.equals(type.getQualifier()))
            .findFirst()
            .map(Type::getValue)
            .filter(StringUtils::isNotBlank)
            .orElse(null);
    return new AttributeImpl(Core.DATATYPE, datatype);
  }

  private Attribute getRelatedAssociations(DdmsResource ddms) {
    List<Serializable> related =
        findAssociations(
                ddms,
                relatedResources -> relatedResources.getRelationship().equals(RELATED_ASSOCIATION))
            .stream()
            .map(this::getMetacardIdFromUrl)
            .collect(Collectors.toList());
    return new AttributeImpl(Associations.RELATED, related);
  }

  private Attribute getDerivedAssociations(DdmsResource ddms) {
    List<Serializable> derived =
        findAssociations(
                ddms,
                relatedResources -> relatedResources.getRelationship().equals(DERIVED_ASSOCIATION))
            .stream()
            .map(this::getMetacardIdFromUrl)
            .collect(Collectors.toList());
    return new AttributeImpl(Associations.DERIVED, derived);
  }

  private Attribute getExternalAssocations(DdmsResource ddms) {
    List<Serializable> external =
        convertStringListToSerializable(
            findAssociations(
                ddms,
                relatedResources ->
                    !Arrays.asList(RELATED_ASSOCIATION, DERIVED_ASSOCIATION, "resource")
                        .contains(relatedResources.getRelationship())));
    return new AttributeImpl(Associations.EXTERNAL, external);
  }

  private List<String> findAssociations(
      DdmsResource ddms, Predicate<? super RelatedResources> filter) {
    return ddms.getRelatedResources()
        .stream()
        .filter(filter)
        .flatMap(relatedResources -> relatedResources.getResources().stream())
        .flatMap(resource -> resource.getLinks().stream())
        .map(Link::getHref)
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.toList());
  }

  private String getMetacardIdFromUrl(String url) {
    String id = url.substring(url.lastIndexOf('/') + 1, url.length());
    return id.split("[?]")[0];
  }

  private List<Serializable> convertStringListToSerializable(List<String> list) {
    return list.stream().map(s -> (Serializable) s).collect(Collectors.toList());
  }

  private Attribute getDatetimeNames(DdmsResource ddms) {
    List<Serializable> names =
        ddms.getTemporalCoverages()
            .stream()
            .map(TemporalCoverage::getName)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    return new AttributeImpl(DateTime.NAME, names);
  }

  private Attribute getDatetimeStart(DdmsResource ddms) {
    List<Serializable> starts =
        ddms.getTemporalCoverages()
            .stream()
            .map(TemporalCoverage::getStart)
            .filter(Objects::nonNull)
            .map(this::convertDdmsDate)
            .collect(Collectors.toList());
    return new AttributeImpl(DateTime.START, starts);
  }

  private Attribute getDatetimeEnd(DdmsResource ddms) {
    List<Serializable> ends =
        ddms.getTemporalCoverages()
            .stream()
            .map(TemporalCoverage::getEnd)
            .filter(Objects::nonNull)
            .map(this::convertDdmsDate)
            .collect(Collectors.toList());
    return new AttributeImpl(DateTime.END, ends);
  }

  private Attribute getCreatorNames(DdmsResource ddms) {
    List<Serializable> names =
        ddms.getCreators()
            .stream()
            .map(org.codice.ddms.v2.resource.Contact::getProducer)
            .flatMap(producer -> producer.getNames().stream())
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    return new AttributeImpl(Contact.CREATOR_NAME, names);
  }

  private Attribute getCreatorPhones(DdmsResource ddms) {
    List<Serializable> phones =
        ddms.getCreators()
            .stream()
            .map(org.codice.ddms.v2.resource.Contact::getProducer)
            .flatMap(producer -> producer.getPhones().stream())
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    return new AttributeImpl(Contact.CREATOR_PHONE, phones);
  }

  private Attribute getCreatorEmails(DdmsResource ddms) {
    List<Serializable> emails =
        ddms.getCreators()
            .stream()
            .map(org.codice.ddms.v2.resource.Contact::getProducer)
            .flatMap(producer -> producer.getEmails().stream())
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    return new AttributeImpl(Contact.CREATOR_EMAIL, emails);
  }

  private Attribute getPublisherNames(DdmsResource ddms) {
    List<Serializable> names =
        ddms.getPublishers()
            .stream()
            .map(org.codice.ddms.v2.resource.Contact::getProducer)
            .flatMap(producer -> producer.getNames().stream())
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    return new AttributeImpl(Contact.PUBLISHER_NAME, names);
  }

  private Attribute getPublisherPhones(DdmsResource ddms) {
    List<Serializable> phones =
        ddms.getPublishers()
            .stream()
            .map(org.codice.ddms.v2.resource.Contact::getProducer)
            .flatMap(producer -> producer.getPhones().stream())
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    return new AttributeImpl(Contact.PUBLISHER_PHONE, phones);
  }

  private Attribute getPublisherEmails(DdmsResource ddms) {
    List<Serializable> emails =
        ddms.getPublishers()
            .stream()
            .map(org.codice.ddms.v2.resource.Contact::getProducer)
            .flatMap(producer -> producer.getEmails().stream())
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    return new AttributeImpl(Contact.PUBLISHER_EMAIL, emails);
  }

  private Attribute getContributorNames(DdmsResource ddms) {
    List<Serializable> names =
        ddms.getContributors()
            .stream()
            .map(org.codice.ddms.v2.resource.Contact::getProducer)
            .flatMap(producer -> producer.getNames().stream())
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    return new AttributeImpl(Contact.CONTRIBUTOR_NAME, names);
  }

  private Attribute getContributorPhones(DdmsResource ddms) {
    List<Serializable> phones =
        ddms.getContributors()
            .stream()
            .map(org.codice.ddms.v2.resource.Contact::getProducer)
            .flatMap(producer -> producer.getPhones().stream())
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    return new AttributeImpl(Contact.CONTRIBUTOR_PHONE, phones);
  }

  private Attribute getContributorEmails(DdmsResource ddms) {
    List<Serializable> emails =
        ddms.getContributors()
            .stream()
            .map(org.codice.ddms.v2.resource.Contact::getProducer)
            .flatMap(producer -> producer.getEmails().stream())
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    return new AttributeImpl(Contact.CONTRIBUTOR_EMAIL, emails);
  }

  private Attribute getPointOfContactNames(DdmsResource ddms) {
    List<Serializable> names =
        ddms.getPointOfContacts()
            .stream()
            .map(org.codice.ddms.v2.resource.Contact::getProducer)
            .flatMap(producer -> producer.getNames().stream())
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    return new AttributeImpl(Contact.POINT_OF_CONTACT_NAME, names);
  }

  private Attribute getPointOfContactPhones(DdmsResource ddms) {
    List<Serializable> phones =
        ddms.getPointOfContacts()
            .stream()
            .map(org.codice.ddms.v2.resource.Contact::getProducer)
            .flatMap(producer -> producer.getPhones().stream())
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    return new AttributeImpl(Contact.POINT_OF_CONTACT_PHONE, phones);
  }

  private Attribute getPointOfContactEmails(DdmsResource ddms) {
    List<Serializable> emails =
        ddms.getPointOfContacts()
            .stream()
            .map(org.codice.ddms.v2.resource.Contact::getProducer)
            .flatMap(producer -> producer.getEmails().stream())
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    return new AttributeImpl(Contact.POINT_OF_CONTACT_EMAIL, emails);
  }

  private Attribute getCountryCodes(DdmsResource ddms) {
    List<Serializable> countryCodes =
        ddms.getGeospatialCoverages()
            .stream()
            .flatMap(geospatialCoverage -> geospatialCoverage.getGeographicIdentifiers().stream())
            .flatMap(geographicIdentifier -> geographicIdentifier.getCountryCodes().stream())
            .map(CountryCode::getValue)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    return new AttributeImpl(Location.COUNTRY_CODE, countryCodes);
  }

  private Attribute getCrsNames(DdmsResource ddms) {
    Set<Serializable> crsNameSet =
        ddms.getGeospatialCoverages()
            .stream()
            .flatMap(geospatialCoverage -> geospatialCoverage.getBoundingGeometries().stream())
            .flatMap(boundingGeometry -> boundingGeometry.getPoints().stream())
            .map(point -> point.getSrsAttributes().getSrsName())
            .collect(Collectors.toSet());
    List<Polygon> polygons =
        ddms.getGeospatialCoverages()
            .stream()
            .flatMap(geospatialCoverage -> geospatialCoverage.getBoundingGeometries().stream())
            .flatMap(boundingGeometry -> boundingGeometry.getPolygons().stream())
            .collect(Collectors.toList());
    crsNameSet.addAll(
        polygons
            .stream()
            .map(polygon -> polygon.getSrsAttributes().getSrsName())
            .collect(Collectors.toSet()));
    crsNameSet.addAll(
        polygons
            .stream()
            .flatMap(polygon -> polygon.getExterior().getPositions().stream())
            .map(position -> position.getSrsAttributes().getSrsName())
            .collect(Collectors.toSet()));
    List<Serializable> crsNames = new ArrayList<>(crsNameSet);
    return new AttributeImpl(Location.COORDINATE_REFERENCE_SYSTEM_NAME, crsNames);
  }

  private Attribute getMediaFormat(DdmsResource ddms) {
    String format = null;
    if (ddms.getFormat() != null) {
      format = ddms.getFormat().getMedium();
    }
    return new AttributeImpl(Media.FORMAT, format);
  }

  private Attribute getMediaType(DdmsResource ddms) {
    String type = null;
    if (ddms.getFormat() != null) {
      type = ddms.getFormat().getMimeType();
    }
    return new AttributeImpl(Media.TYPE, type);
  }

  private Attribute getTopicCategories(DdmsResource ddms) {
    List<Serializable> categories = new ArrayList<>();
    List<Serializable> subjectCoverageCategories =
        ddms.getSubjectCoverage()
            .getCategories()
            .stream()
            .map(Category::getLabel)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    List<Serializable> typeCategories =
        ddms.getTypes()
            .stream()
            .map(Type::getValue)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    categories.addAll(subjectCoverageCategories);
    categories.addAll(typeCategories);
    return new AttributeImpl(Topic.CATEGORY, categories);
  }

  private Attribute getTopicKeywords(DdmsResource ddms) {
    List<Serializable> keywords =
        ddms.getSubjectCoverage()
            .getKeywords()
            .stream()
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    return new AttributeImpl(Topic.KEYWORD, keywords);
  }

  private Attribute getTopicVocabulary(DdmsResource ddms) {
    List<Serializable> keywords = new ArrayList<>();
    List<Serializable> subjectKeywords =
        ddms.getSubjectCoverage()
            .getCategories()
            .stream()
            .map(Category::getQualifier)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    List<Serializable> typeKeywords =
        ddms.getTypes()
            .stream()
            .map(Type::getQualifier)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    keywords.addAll(subjectKeywords);
    keywords.addAll(typeKeywords);
    return new AttributeImpl(Topic.VOCABULARY, keywords);
  }

  private Attribute getClassification(SecurityAttributes securityAttributes) {
    return new AttributeImpl(
        Security.CLASSIFICATION, securityAttributes.getClassification().getValue());
  }

  private Attribute getReleasability(SecurityAttributes securityAttributes) {
    List<Serializable> releasability =
        convertStringListToSerializable(securityAttributes.getReleasableTo());
    return new AttributeImpl(Security.RELEASABILITY, releasability);
  }

  private Attribute getOwnerProducers(SecurityAttributes securityAttributes) {
    List<Serializable> ownerProducers =
        convertStringListToSerializable(securityAttributes.getOwnerProducer());
    return new AttributeImpl(Security.OWNER_PRODUCER, ownerProducers);
  }

  private Attribute getDisseminationControls(SecurityAttributes securityAttributes) {
    List<Serializable> disseminationControls =
        convertStringListToSerializable(securityAttributes.getDisseminationControls());
    return new AttributeImpl(Security.DISSEMINATION_CONTROLS, disseminationControls);
  }

  private Attribute getSciControls(SecurityAttributes securityAttributes) {
    List<Serializable> sciControls =
        convertStringListToSerializable(securityAttributes.getSciControls());
    return new AttributeImpl(Security.CODEWORDS, sciControls);
  }

  private Attribute getNonIcMarkings(SecurityAttributes securityAttributes) {
    List<Serializable> nonIcMarkings =
        convertStringListToSerializable(securityAttributes.getNonIcMarkings());
    return new AttributeImpl(Security.OTHER_DISSEMINATION_CONTROLS, nonIcMarkings);
  }

  private Attribute getCopyright(DdmsResource ddms) {
    boolean copyright = false;
    if (ddms.getRights() != null) {
      copyright = ddms.getRights().getCopyright();
    }
    return new AttributeImpl(RestrictionAttributes.COPYRIGHT_APPLIES, copyright);
  }

  private Attribute getIntellectualProperty(DdmsResource ddms) {
    boolean intellectualProperty = false;
    if (ddms.getRights() != null) {
      intellectualProperty = ddms.getRights().getIntellectualProperty();
    }
    return new AttributeImpl(RestrictionAttributes.INTELLECTUAL_PROPERTY, intellectualProperty);
  }

  private Attribute getPrivacyAct(DdmsResource ddms) {
    boolean privacyAct = false;
    if (ddms.getRights() != null) {
      privacyAct = ddms.getRights().getPrivacyAct();
    }
    return new AttributeImpl(RestrictionAttributes.PRIVACY_ACT, privacyAct);
  }

  private Attribute getInfoCutOff(DdmsResource ddms) {
    Date infoCutOff = null;
    if (ddms.getDates() != null) {
      infoCutOff = convertDdmsDate(ddms.getDates().getInfoCutOff());
    }
    return new AttributeImpl(Ddms20MetacardType.INFO_CUT_OFF, infoCutOff);
  }

  private Attribute getPosted(DdmsResource ddms) {
    Date posted = null;
    if (ddms.getDates() != null) {
      posted = convertDdmsDate(ddms.getDates().getPosted());
    }
    return new AttributeImpl(Ddms20MetacardType.POSTED, posted);
  }

  private Attribute getGeographicNames(DdmsResource ddms) {
    List<Serializable> geographicNames =
        ddms.getGeospatialCoverages()
            .stream()
            .flatMap(geospatialCoverage -> geospatialCoverage.getGeographicIdentifiers().stream())
            .flatMap(geographicIdentifier -> geographicIdentifier.getNames().stream())
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    return new AttributeImpl(Ddms20MetacardType.LOCATION_NAME, geographicNames);
  }

  private Attribute getGeographicRegions(DdmsResource ddms) {
    List<Serializable> geographicRegions =
        ddms.getGeospatialCoverages()
            .stream()
            .flatMap(geospatialCoverage -> geospatialCoverage.getGeographicIdentifiers().stream())
            .flatMap(geographicIdentifier -> geographicIdentifier.getRegions().stream())
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    return new AttributeImpl(Ddms20MetacardType.LOCATION_REGION, geographicRegions);
  }

  private Attribute getBeNumbers(DdmsResource ddms) {
    List<Serializable> beNumbers =
        ddms.getGeospatialCoverages()
            .stream()
            .flatMap(geospatialCoverage -> geospatialCoverage.getGeographicIdentifiers().stream())
            .flatMap(geographicIdentifier -> geographicIdentifier.getFacilityIdentifiers().stream())
            .map(FacilityIdentifier::getBeNumber)
            .collect(Collectors.toList());
    return new AttributeImpl(Ddms20MetacardType.BE_NUMBER, beNumbers);
  }

  private Attribute getSubtitles(DdmsResource ddms) {
    List<Serializable> subtitles =
        ddms.getSubtitles().stream().map(Title::getValue).collect(Collectors.toList());
    return new AttributeImpl(Ddms20MetacardType.SUBTITLE, subtitles);
  }

  private Date convertDdmsDate(DdmsDate ddmsDate) {
    return (ddmsDate != null && !ddmsDate.isUnknown() && !ddmsDate.isNotApplicable())
        ? Date.from(Instant.from(ddmsDate.toRawTemporalAccessor()))
        : null;
  }
}
