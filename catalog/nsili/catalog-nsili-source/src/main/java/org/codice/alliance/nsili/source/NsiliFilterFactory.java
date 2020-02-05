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
package org.codice.alliance.nsili.source;

import ddf.catalog.data.Metacard;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.codice.alliance.nsili.common.GIAS.AttributeInformation;
import org.codice.alliance.nsili.common.GIAS.AttributeType;
import org.codice.alliance.nsili.common.GIAS.DomainType;
import org.codice.alliance.nsili.common.NsiliAttributeMap;
import org.codice.alliance.nsili.common.NsiliConstants;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NsiliFilterFactory {

  public static final String TYPE = "type";

  public static final String LP = "(";

  public static final String RP = ")";

  public static final String LIKE = " like ";

  public static final String OR = " or ";

  public static final String AND = " and ";

  public static final String EXISTS = " exists";

  public static final String NOT = "not ";

  public static final String EQ = " = ";

  public static final String LT = " < ";

  public static final String GT = " > ";

  public static final String LTE = " <= ";

  public static final String GTE = " >= ";

  public static final String BTW = " <> ";

  public static final String COMMA = ",";

  public static final String INTERSECT = " intersect ";

  public static final String OUTSIDE = " outside ";

  public static final String INSIDE = " inside ";

  public static final String WITHIN = " within ";

  public static final String BEYOND = " beyond ";

  public static final String METERS_OF = " meters of ";

  private static final Logger LOGGER = LoggerFactory.getLogger(NsiliFilterFactory.class);

  private Map<String, List<AttributeInformation>> queryableAttributes;

  private String view;

  private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

  public NsiliFilterFactory(
      Map<String, List<AttributeInformation>> queryableAttributes, String view) {
    this.queryableAttributes = queryableAttributes;
    this.view = view;
  }

  public String buildPropertyIsLike(String propertyName, String value) {
    if (queryableAttributes == null) {
      return NsiliFilterDelegate.EMPTY_STRING;
    }

    // Replace * with %, since * is not a valid wildcard in BQS
    value = value.replaceAll("\\*", NsiliFilterDelegate.WILDCARD);
    value = value.replaceAll("\\?", NsiliFilterDelegate.WILDCARD);
    boolean addPrefixWildcard = false;
    boolean addPostfixWildcard = false;

    if (!value.startsWith(NsiliFilterDelegate.WILDCARD)) {
      addPrefixWildcard = true;
    }

    if (!value.endsWith(NsiliFilterDelegate.WILDCARD)) {
      addPostfixWildcard = true;
    }

    List<AttributeInformation> attributeInformationList = queryableAttributes.get(view);

    if (attributeInformationList == null) {
      return NsiliFilterDelegate.EMPTY_STRING;
    }

    Map<String, AttributeInformation> attrInfoMap = createAttrInfoMap(attributeInformationList);
    List<String> filters = new ArrayList<>();

    // anyText searches we will grab each attribute in NSILI, else we add only the attribute
    if (propertyName.equals(Metacard.ANY_TEXT)) {
      for (AttributeInformation attributeInformation : attributeInformationList) {
        if (isTextAttributeType(attributeInformation)) {
          setTextFilter(
              value, addPrefixWildcard, addPostfixWildcard, filters, attributeInformation);
        }
      }
    } else {
      List<String> propertyList = mapToNsilQuery(propertyName);
      String operator = EQ;
      if (addPrefixWildcard || addPostfixWildcard) {
        operator = LIKE;
      }
      for (String property : propertyList) {
        AttributeInformation attrInfo = attrInfoMap.get(property);
        setNonTextFilter(
            value, addPrefixWildcard, addPostfixWildcard, filters, operator, property, attrInfo);
      }
    }

    return buildOrFilter(filters);
  }

  private void setNonTextFilter(
      String value,
      boolean addPrefixWildcard,
      boolean addPostfixWildcard,
      List<String> filters,
      String operator,
      String property,
      AttributeInformation attrInfo) {
    String filterString = LP + property + LIKE + NsiliFilterDelegate.SQ;
    if (attrInfo != null) {
      if (isAttributeListDomain(attrInfo)) {
        filterString = LP + attrInfo.attribute_name + operator + NsiliFilterDelegate.SQ;
      }

      if (addPrefixWildcard) {
        filterString = filterString + NsiliFilterDelegate.WILDCARD;
      }

      filterString = filterString + value;

      if (addPostfixWildcard) {
        filterString = filterString + NsiliFilterDelegate.WILDCARD;
      }

      filterString = filterString + NsiliFilterDelegate.SQ + RP;
      filters.add(filterString);
    }
  }

  private void setTextFilter(
      String value,
      boolean addPrefixWildcard,
      boolean addPostfixWildcard,
      List<String> filters,
      AttributeInformation attributeInformation) {
    String filterString = LP + attributeInformation.attribute_name + LIKE + NsiliFilterDelegate.SQ;

    if (isAttributeListDomain(attributeInformation)) {
      filterString = LP + attributeInformation.attribute_name + EQ + NsiliFilterDelegate.SQ;
    }

    if (addPrefixWildcard) {
      filterString = filterString + NsiliFilterDelegate.WILDCARD;
    }

    filterString = filterString + value;

    if (addPostfixWildcard) {
      filterString = filterString + NsiliFilterDelegate.WILDCARD;
    }

    filterString = filterString + NsiliFilterDelegate.SQ + RP;
    filters.add(filterString);
  }

  public boolean isTextAttributeType(AttributeInformation attributeInformation) {
    return !attributeInformation.attribute_name.contains(NsiliConstants.ADVANCED_GEOSPATIAL)
        && attributeInformation.attribute_domain.discriminator() == DomainType.TEXT_VALUE
        && attributeInformation.attribute_type.equals(AttributeType.TEXT)
        && !attributeInformation.attribute_name.equals(TYPE);
  }

  public boolean isAttributeListDomain(AttributeInformation attributeInformation) {
    return attributeInformation.attribute_domain.discriminator() == DomainType.LIST
        || attributeInformation.attribute_domain.discriminator() == DomainType.ORDERED_LIST;
  }

  public String buildPropertyIsEqualTo(String property, String value) {
    List<String> propertyList = mapToNsilQuery(property);
    List<String> filters = new ArrayList<>();
    for (String ddfProperty : propertyList) {
      String filter = LP + ddfProperty + EQ + value + RP;
      filters.add(filter);
    }
    return buildOrFilter(filters);
  }

  public String buildPropertyIsNotEqualTo(String property, String value) {
    return buildNotFilter(buildPropertyIsEqualTo(property, value));
  }

  public String buildPropertyIsGreaterThan(String property, String value) {
    List<String> propertyList = mapToNsilQuery(property);
    List<String> filters = new ArrayList<>();
    for (String ddfProperty : propertyList) {
      String filter = LP + ddfProperty + GT + value + RP;
      filters.add(filter);
    }
    return buildOrFilter(filters);
  }

  public String buildPropertyIsGreaterThanOrEqual(String property, String value) {
    List<String> propertyList = mapToNsilQuery(property);
    List<String> filters = new ArrayList<>();
    for (String ddfProperty : propertyList) {
      String filter = LP + ddfProperty + GTE + value + RP;
      filters.add(filter);
    }
    return buildOrFilter(filters);
  }

  public String buildPropertyIsLessThan(String property, String value) {
    List<String> propertyList = mapToNsilQuery(property);
    List<String> filters = new ArrayList<>();
    for (String ddfProperty : propertyList) {
      String filter = LP + ddfProperty + LT + value + RP;
      filters.add(filter);
    }
    return buildOrFilter(filters);
  }

  public String buildPropertyIsLessThanOrEqual(String property, String value) {
    List<String> propertyList = mapToNsilQuery(property);
    List<String> filters = new ArrayList<>();
    for (String ddfProperty : propertyList) {
      String filter = LP + ddfProperty + LTE + value + RP;
      filters.add(filter);
    }
    return buildOrFilter(filters);
  }

  public String buildPropertyIsBetween(String property, String lowerBound, String upperBound) {
    List<String> propertyList = mapToNsilQuery(property);
    List<String> filters = new ArrayList<>();
    for (String ddfProperty : propertyList) {
      String filter =
          LP + ddfProperty + GTE + lowerBound + AND + ddfProperty + LTE + upperBound + RP;
      filters.add(filter);
    }
    return buildOrFilter(filters);
  }

  public String buildOrFilter(List<String> filters) {
    if (filters.size() == 1) {
      return filters.get(0);
    }

    StringBuilder stringBuilder = new StringBuilder(LP);

    for (String filter : filters) {
      if (StringUtils.isNotBlank(filter)) {
        stringBuilder.append(filter);
        stringBuilder.append(OR);
      }
    }

    String result = stringBuilder.toString();
    if (result.length() > 1) {
      return result.substring(0, result.length() - OR.length()) + RP;
    }
    return NsiliFilterDelegate.EMPTY_STRING;
  }

  public String buildAndFilter(List<String> filters) {
    if (filters.size() == 1) {
      return filters.get(0);
    }

    StringBuilder stringBuilder = new StringBuilder();

    for (String filter : filters) {
      if (StringUtils.isNotBlank(filter)) {
        stringBuilder.append(filter);
        stringBuilder.append(AND);
      }
    }
    String result = stringBuilder.toString();
    if (result.length() > 1) {
      return result.substring(0, result.length() - AND.length());
    }
    return NsiliFilterDelegate.EMPTY_STRING;
  }

  public String buildPropertyIsNull(String property) {
    List<String> propertyList = mapToNsilQuery(property);
    List<String> filters = new ArrayList<>();
    for (String ddfProperty : propertyList) {
      String filter = buildNotFilter(LP + ddfProperty + EXISTS + RP);
      filters.add(filter);
    }
    return buildOrFilter(filters);
  }

  public String buildNotFilter(String filter) {
    if (StringUtils.isNotBlank(filter)) {
      return NOT + filter;
    } else {
      return filter;
    }
  }

  public String buildIntersectsFilter(String propertyName, String wkt) {
    List<String> filters = new ArrayList<>();
    String bqsGeo = convertWktToBqs(wkt);
    String filter;
    if (StringUtils.isNotBlank(bqsGeo)) {
      List<String> propertyList = mapToNsilQuery(propertyName);
      for (String ddfProperty : propertyList) {
        filter = LP + ddfProperty + INTERSECT + convertWktToBqs(wkt) + RP;
        filters.add(filter);
      }
    }
    return buildOrFilter(filters);
  }

  public String buildDisjointFilter(String propertyName, String wkt) {
    List<String> filters = new ArrayList<>();
    String bqsGeo = convertWktToBqs(wkt);
    String filter;
    if (StringUtils.isNotBlank(bqsGeo)) {
      List<String> propertyList = mapToNsilQuery(propertyName);
      for (String ddfProperty : propertyList) {
        filter = LP + ddfProperty + OUTSIDE + convertWktToBqs(wkt) + RP;
        filters.add(filter);
      }
    }
    return buildOrFilter(filters);
  }

  public String buildWithinFilter(String propertyName, String wkt) {
    List<String> filters = new ArrayList<>();
    String bqsGeo = convertWktToBqs(wkt);
    String filter;
    if (StringUtils.isNotBlank(bqsGeo)) {
      List<String> propertyList = mapToNsilQuery(propertyName);
      for (String ddfProperty : propertyList) {
        filter = LP + ddfProperty + INSIDE + convertWktToBqs(wkt) + RP;
        filters.add(filter);
      }
    }
    return buildOrFilter(filters);
  }

  public String buildDWithinFilter(String propertyName, String wkt, double distance) {
    List<String> filters = new ArrayList<>();
    String bqsGeo = convertWktToBqs(wkt);
    String filter;
    if (StringUtils.isNotBlank(bqsGeo)) {
      List<String> propertyList = mapToNsilQuery(propertyName);
      for (String ddfProperty : propertyList) {
        filter = LP + ddfProperty + WITHIN + distance + METERS_OF + convertWktToBqs(wkt) + RP;
        filters.add(filter);
      }
    }
    return buildOrFilter(filters);
  }

  public String buildBeyondFilter(String propertyName, String wkt, double distance) {
    List<String> filters = new ArrayList<>();
    String bqsGeo = convertWktToBqs(wkt);
    String filter;
    if (StringUtils.isNotBlank(bqsGeo)) {
      List<String> propertyList = mapToNsilQuery(propertyName);
      for (String ddfProperty : propertyList) {
        filter = LP + ddfProperty + BEYOND + distance + METERS_OF + convertWktToBqs(wkt) + RP;
        filters.add(filter);
      }
    }
    return buildOrFilter(filters);
  }

  public static List<String> mapToNsil(String attribute) {
    List<String> nsiliProperties = NsiliAttributeMap.getNsiliAttributeForDdf(attribute);
    if (nsiliProperties == null) {
      nsiliProperties = new ArrayList<>();
    }

    if (nsiliProperties.isEmpty()) {
      LOGGER.debug("Couldn't map query attribute ({}) to NSIL", attribute);
    }

    return nsiliProperties;
  }

  public static List<String> mapToNsilQuery(String attribute) {
    if (attribute.equals(Metacard.ANY_GEO)) {
      return Collections.singletonList(
          NsiliConstants.NSIL_COVERAGE + "." + NsiliConstants.SPATIAL_GEOGRAPHIC_REF_BOX);
    } else {
      return mapToNsil(attribute);
    }
  }

  public String convertWktToBqs(String wkt) {
    WKTReader wktReader = new WKTReader(GEOMETRY_FACTORY);
    Geometry geometry;
    try {
      geometry = wktReader.read(wkt);
    } catch (ParseException e) {
      LOGGER.debug("Unable to parse WKT String {}", wkt, e);
      return NsiliFilterDelegate.EMPTY_STRING;
    }

    if (geometry.getCoordinates() == null || StringUtils.isBlank(geometry.getGeometryType())) {
      return NsiliFilterDelegate.EMPTY_STRING;
    }

    StringBuilder result = new StringBuilder(geometry.getGeometryType().toUpperCase() + LP);
    Coordinate[] coordinates = geometry.getCoordinates();
    for (Coordinate coordinate : coordinates) {
      result.append(coordinate.y + COMMA + coordinate.x + COMMA);
    }
    return result.toString().substring(0, result.toString().length() - 1) + RP;
  }

  private Map<String, AttributeInformation> createAttrInfoMap(
      List<AttributeInformation> attributeInformationList) {
    Map<String, AttributeInformation> attrInfoMap = new HashMap<>();
    for (AttributeInformation attrInfo : attributeInformationList) {
      attrInfoMap.put(attrInfo.attribute_name, attrInfo);
    }
    return attrInfoMap;
  }
}
