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

import ddf.catalog.filter.FilterDelegate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.lang.time.FastDateFormat;
import org.codice.alliance.nsili.common.GIAS.AttributeInformation;

public class NsiliFilterDelegate extends FilterDelegate<String> {

  private NsiliFilterFactory filterFactory;

  public static final String EMPTY_STRING = "";

  public static final String SQ = "'";

  public static final String UTC = "UTC";

  public static final String WILDCARD = "%";

  private static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

  private String view;

  private static final TimeZone TIME_ZONE = TimeZone.getTimeZone(UTC);

  private static final FastDateFormat FAST_DATE_FORMAT =
      FastDateFormat.getInstance(DATE_FORMAT, TIME_ZONE, null);

  private Map<String, List<AttributeInformation>> queryableAttributes;

  public NsiliFilterDelegate(
      Map<String, List<AttributeInformation>> queryableAttributes, String view) {
    this.view = view;
    this.queryableAttributes = queryableAttributes;
    filterFactory = new NsiliFilterFactory(queryableAttributes, view);
  }

  @Override
  public String and(List<String> filters) {
    return filterFactory.buildAndFilter(filters);
  }

  @Override
  public String or(List<String> filters) {
    return filterFactory.buildOrFilter(filters);
  }

  @Override
  public String not(String filter) {
    return filterFactory.buildNotFilter(filter);
  }

  @Override
  public String propertyIsEqualTo(String propertyName, String literal, boolean isCaseSensitive) {
    return filterFactory.buildPropertyIsEqualTo(propertyName, SQ + literal + SQ);
  }

  @Override
  public String propertyIsEqualTo(String propertyName, Date literal) {
    return filterFactory.buildPropertyIsEqualTo(propertyName, getStringFromDate(literal));
  }

  @Override
  public String propertyIsEqualTo(String propertyName, int literal) {
    return filterFactory.buildPropertyIsEqualTo(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsEqualTo(String propertyName, short literal) {
    return filterFactory.buildPropertyIsEqualTo(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsEqualTo(String propertyName, long literal) {
    return filterFactory.buildPropertyIsEqualTo(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsEqualTo(String propertyName, float literal) {
    return filterFactory.buildPropertyIsEqualTo(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsEqualTo(String propertyName, double literal) {
    return filterFactory.buildPropertyIsEqualTo(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsEqualTo(String propertyName, boolean literal) {
    return filterFactory.buildPropertyIsEqualTo(
        propertyName, SQ + Boolean.toString(literal).toUpperCase() + SQ);
  }

  @Override
  public String propertyIsNotEqualTo(String propertyName, String literal, boolean isCaseSensitive) {
    return filterFactory.buildPropertyIsNotEqualTo(propertyName, SQ + literal + SQ);
  }

  @Override
  public String propertyIsNotEqualTo(String propertyName, Date literal) {
    return filterFactory.buildPropertyIsNotEqualTo(propertyName, getStringFromDate(literal));
  }

  @Override
  public String propertyIsNotEqualTo(String propertyName, int literal) {
    return filterFactory.buildPropertyIsNotEqualTo(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsNotEqualTo(String propertyName, short literal) {
    return filterFactory.buildPropertyIsNotEqualTo(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsNotEqualTo(String propertyName, long literal) {
    return filterFactory.buildPropertyIsNotEqualTo(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsNotEqualTo(String propertyName, float literal) {
    return filterFactory.buildPropertyIsNotEqualTo(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsNotEqualTo(String propertyName, double literal) {
    return filterFactory.buildPropertyIsNotEqualTo(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsNotEqualTo(String propertyName, boolean literal) {
    return filterFactory.buildPropertyIsNotEqualTo(
        propertyName, SQ + Boolean.toString(literal).toUpperCase() + SQ);
  }

  @Override
  public String propertyIsGreaterThan(String propertyName, String literal) {
    return filterFactory.buildPropertyIsGreaterThan(propertyName, SQ + literal + SQ);
  }

  @Override
  public String propertyIsGreaterThan(String propertyName, Date literal) {
    return filterFactory.buildPropertyIsGreaterThan(propertyName, getStringFromDate(literal));
  }

  @Override
  public String propertyIsGreaterThan(String propertyName, int literal) {
    return filterFactory.buildPropertyIsGreaterThan(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsGreaterThan(String propertyName, short literal) {
    return filterFactory.buildPropertyIsGreaterThan(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsGreaterThan(String propertyName, long literal) {
    return filterFactory.buildPropertyIsGreaterThan(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsGreaterThan(String propertyName, float literal) {
    return filterFactory.buildPropertyIsGreaterThan(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsGreaterThan(String propertyName, double literal) {
    return filterFactory.buildPropertyIsGreaterThan(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsGreaterThanOrEqualTo(String propertyName, String literal) {
    return filterFactory.buildPropertyIsGreaterThanOrEqual(propertyName, SQ + literal + SQ);
  }

  @Override
  public String propertyIsGreaterThanOrEqualTo(String propertyName, Date literal) {
    return filterFactory.buildPropertyIsGreaterThanOrEqual(
        propertyName, getStringFromDate(literal));
  }

  @Override
  public String propertyIsGreaterThanOrEqualTo(String propertyName, int literal) {
    return filterFactory.buildPropertyIsGreaterThanOrEqual(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsGreaterThanOrEqualTo(String propertyName, short literal) {
    return filterFactory.buildPropertyIsGreaterThanOrEqual(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsGreaterThanOrEqualTo(String propertyName, long literal) {
    return filterFactory.buildPropertyIsGreaterThanOrEqual(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsGreaterThanOrEqualTo(String propertyName, float literal) {
    return filterFactory.buildPropertyIsGreaterThanOrEqual(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsGreaterThanOrEqualTo(String propertyName, double literal) {
    return filterFactory.buildPropertyIsGreaterThanOrEqual(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsLessThan(String propertyName, String literal) {
    return filterFactory.buildPropertyIsLessThan(propertyName, SQ + literal + SQ);
  }

  @Override
  public String propertyIsLessThan(String propertyName, Date literal) {
    return filterFactory.buildPropertyIsLessThan(propertyName, getStringFromDate(literal));
  }

  @Override
  public String propertyIsLessThan(String propertyName, int literal) {
    return filterFactory.buildPropertyIsLessThan(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsLessThan(String propertyName, short literal) {
    return filterFactory.buildPropertyIsLessThan(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsLessThan(String propertyName, long literal) {
    return filterFactory.buildPropertyIsLessThan(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsLessThan(String propertyName, float literal) {
    return filterFactory.buildPropertyIsLessThan(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsLessThan(String propertyName, double literal) {
    return filterFactory.buildPropertyIsLessThan(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsLessThanOrEqualTo(String propertyName, String literal) {
    return filterFactory.buildPropertyIsLessThanOrEqual(propertyName, SQ + literal + SQ);
  }

  @Override
  public String propertyIsLessThanOrEqualTo(String propertyName, Date literal) {
    return filterFactory.buildPropertyIsLessThanOrEqual(propertyName, getStringFromDate(literal));
  }

  @Override
  public String propertyIsLessThanOrEqualTo(String propertyName, int literal) {
    return filterFactory.buildPropertyIsLessThanOrEqual(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsLessThanOrEqualTo(String propertyName, short literal) {
    return filterFactory.buildPropertyIsLessThanOrEqual(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsLessThanOrEqualTo(String propertyName, long literal) {
    return filterFactory.buildPropertyIsLessThanOrEqual(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsLessThanOrEqualTo(String propertyName, float literal) {
    return filterFactory.buildPropertyIsLessThanOrEqual(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsLessThanOrEqualTo(String propertyName, double literal) {
    return filterFactory.buildPropertyIsLessThanOrEqual(propertyName, String.valueOf(literal));
  }

  @Override
  public String propertyIsBetween(String propertyName, String lowerBoundary, String upperBoundary) {
    return filterFactory.buildPropertyIsBetween(
        propertyName, SQ + lowerBoundary + SQ, SQ + upperBoundary + SQ);
  }

  @Override
  public String propertyIsBetween(String propertyName, Date lowerBoundary, Date upperBoundary) {
    return filterFactory.buildPropertyIsBetween(
        propertyName, getStringFromDate(lowerBoundary), getStringFromDate(upperBoundary));
  }

  @Override
  public String propertyIsBetween(String propertyName, int lowerBoundary, int upperBoundary) {
    return filterFactory.buildPropertyIsBetween(
        propertyName, String.valueOf(lowerBoundary), String.valueOf(upperBoundary));
  }

  @Override
  public String propertyIsBetween(String propertyName, short lowerBoundary, short upperBoundary) {
    return filterFactory.buildPropertyIsBetween(
        propertyName, String.valueOf(lowerBoundary), String.valueOf(upperBoundary));
  }

  @Override
  public String propertyIsBetween(String propertyName, long lowerBoundary, long upperBoundary) {
    return filterFactory.buildPropertyIsBetween(
        propertyName, String.valueOf(lowerBoundary), String.valueOf(upperBoundary));
  }

  @Override
  public String propertyIsBetween(String propertyName, float lowerBoundary, float upperBoundary) {
    return filterFactory.buildPropertyIsBetween(
        propertyName, String.valueOf(lowerBoundary), String.valueOf(upperBoundary));
  }

  @Override
  public String propertyIsBetween(String propertyName, double lowerBoundary, double upperBoundary) {
    return filterFactory.buildPropertyIsBetween(
        propertyName, String.valueOf(lowerBoundary), String.valueOf(upperBoundary));
  }

  @Override
  public String propertyIsNull(String propertyName) {
    return filterFactory.buildPropertyIsNull(propertyName);
  }

  @Override
  public String propertyIsLike(String propertyName, String pattern, boolean isCaseSensitive) {
    return filterFactory.buildPropertyIsLike(propertyName, pattern);
  }

  // Temporal
  @Override
  public String after(String propertyName, Date date) {
    String filter = EMPTY_STRING;
    if (isSupportedQueryableAttribute(propertyName)) {
      filter = propertyIsGreaterThanOrEqualTo(propertyName, date);
    }
    return filter;
  }

  @Override
  public String before(String propertyName, Date date) {
    String filter = EMPTY_STRING;
    if (isSupportedQueryableAttribute(propertyName)) {
      filter = propertyIsLessThanOrEqualTo(propertyName, date);
    }
    return filter;
  }

  @Override
  public String during(String propertyName, Date startDate, Date endDate) {
    String startDateFilter = after(propertyName, startDate);
    String endDateFilter = before(propertyName, endDate);
    return and(Arrays.asList(startDateFilter, endDateFilter));
  }

  //  Spatial
  @Override
  public String disjoint(String propertyName, String wkt) {
    String filter = EMPTY_STRING;
    if (isSupportedQueryableAttribute(propertyName)) {
      filter = filterFactory.buildDisjointFilter(propertyName, wkt);
    }
    return filter;
  }

  @Override
  public String within(String propertyName, String wkt) {
    String filter = EMPTY_STRING;
    if (isSupportedQueryableAttribute(propertyName)) {
      filter = filterFactory.buildWithinFilter(propertyName, wkt);
    }
    return filter;
  }

  @Override
  public String intersects(String propertyName, String wkt) {
    String filter = EMPTY_STRING;
    if (isSupportedQueryableAttribute(propertyName)) {
      filter = filterFactory.buildIntersectsFilter(propertyName, wkt);
    }
    return filter;
  }

  @Override
  public String dwithin(String propertyName, String wkt, double distance) {
    String filter = EMPTY_STRING;
    if (isSupportedQueryableAttribute(propertyName)) {
      filter = filterFactory.buildDWithinFilter(propertyName, wkt, distance);
    }
    return filter;
  }

  @Override
  public String beyond(String propertyName, String wkt, double distance) {
    String filter = EMPTY_STRING;
    if (isSupportedQueryableAttribute(propertyName)) {
      filter = filterFactory.buildBeyondFilter(propertyName, wkt, distance);
    }
    return filter;
  }

  private String getStringFromDate(Date date) {
    return SQ + FAST_DATE_FORMAT.format(date) + SQ;
  }

  private boolean isSupportedQueryableAttribute(String propertyName) {
    List<String> mappedProperties = NsiliFilterFactory.mapToNsilQuery(propertyName);

    boolean supportedQueryAttribute = false;
    for (AttributeInformation attributeInformation : queryableAttributes.get(view)) {
      for (String mappedProperty : mappedProperties) {
        if (attributeInformation.attribute_name.equals(mappedProperty)) {
          supportedQueryAttribute = true;
          break;
        }
      }
    }
    return supportedQueryAttribute;
  }
}
