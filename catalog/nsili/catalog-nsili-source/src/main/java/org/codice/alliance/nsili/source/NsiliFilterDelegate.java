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

  private static String view;

  private static final TimeZone TIME_ZONE = TimeZone.getTimeZone(UTC);

  private static final FastDateFormat FAST_DATE_FORMAT =
      FastDateFormat.getInstance(DATE_FORMAT, TIME_ZONE, null);

  private static Map<String, List<AttributeInformation>> queryableAttributes;

  public NsiliFilterDelegate(
      Map<String, List<AttributeInformation>> queryableAttributes, String view) {
    this.view = view;
    this.queryableAttributes = queryableAttributes;
    filterFactory = new NsiliFilterFactory(queryableAttributes, view);
  }

  @Override
  public String and(List<String> filters) {
    String filter = filterFactory.buildAndFilter(filters);
    return filter;
  }

  @Override
  public String or(List<String> filters) {
    String filter = filterFactory.buildOrFilter(filters);
    return filter;
  }

  @Override
  public String not(String filter) {
    String newFilter = filterFactory.buildNotFilter(filter);
    return newFilter;
  }

  @Override
  public String propertyIsEqualTo(String propertyName, String literal, boolean isCaseSensitive) {
    String filter = filterFactory.buildPropertyIsEqualTo(propertyName, SQ + literal + SQ);
    return filter;
  }

  @Override
  public String propertyIsEqualTo(String propertyName, Date literal) {
    String filter = filterFactory.buildPropertyIsEqualTo(propertyName, getStringFromDate(literal));

    return filter;
  }

  @Override
  public String propertyIsEqualTo(String propertyName, int literal) {
    String filter = filterFactory.buildPropertyIsEqualTo(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsEqualTo(String propertyName, short literal) {
    String filter = filterFactory.buildPropertyIsEqualTo(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsEqualTo(String propertyName, long literal) {
    String filter = filterFactory.buildPropertyIsEqualTo(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsEqualTo(String propertyName, float literal) {
    String filter = filterFactory.buildPropertyIsEqualTo(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsEqualTo(String propertyName, double literal) {
    String filter = filterFactory.buildPropertyIsEqualTo(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsEqualTo(String propertyName, boolean literal) {
    String filter =
        filterFactory.buildPropertyIsEqualTo(
            propertyName, SQ + Boolean.valueOf(literal).toString().toUpperCase() + SQ);
    return filter;
  }

  @Override
  public String propertyIsNotEqualTo(String propertyName, String literal, boolean isCaseSensitive) {
    String filter = filterFactory.buildPropertyIsNotEqualTo(propertyName, SQ + literal + SQ);
    return filter;
  }

  @Override
  public String propertyIsNotEqualTo(String propertyName, Date literal) {
    String filter =
        filterFactory.buildPropertyIsNotEqualTo(propertyName, getStringFromDate(literal));
    return filter;
  }

  @Override
  public String propertyIsNotEqualTo(String propertyName, int literal) {
    String filter = filterFactory.buildPropertyIsNotEqualTo(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsNotEqualTo(String propertyName, short literal) {
    String filter = filterFactory.buildPropertyIsNotEqualTo(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsNotEqualTo(String propertyName, long literal) {
    String filter = filterFactory.buildPropertyIsNotEqualTo(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsNotEqualTo(String propertyName, float literal) {
    String filter = filterFactory.buildPropertyIsNotEqualTo(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsNotEqualTo(String propertyName, double literal) {
    String filter = filterFactory.buildPropertyIsNotEqualTo(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsNotEqualTo(String propertyName, boolean literal) {
    String filter =
        filterFactory.buildPropertyIsNotEqualTo(
            propertyName, SQ + Boolean.valueOf(literal).toString().toUpperCase() + SQ);
    return filter;
  }

  @Override
  public String propertyIsGreaterThan(String propertyName, String literal) {
    String filter = filterFactory.buildPropertyIsGreaterThan(propertyName, SQ + literal + SQ);
    return filter;
  }

  @Override
  public String propertyIsGreaterThan(String propertyName, Date literal) {
    String filter =
        filterFactory.buildPropertyIsGreaterThan(propertyName, getStringFromDate(literal));
    return filter;
  }

  @Override
  public String propertyIsGreaterThan(String propertyName, int literal) {
    String filter = filterFactory.buildPropertyIsGreaterThan(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsGreaterThan(String propertyName, short literal) {
    String filter = filterFactory.buildPropertyIsGreaterThan(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsGreaterThan(String propertyName, long literal) {
    String filter = filterFactory.buildPropertyIsGreaterThan(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsGreaterThan(String propertyName, float literal) {
    String filter = filterFactory.buildPropertyIsGreaterThan(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsGreaterThan(String propertyName, double literal) {
    String filter = filterFactory.buildPropertyIsGreaterThan(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsGreaterThanOrEqualTo(String propertyName, String literal) {
    String filter =
        filterFactory.buildPropertyIsGreaterThanOrEqual(propertyName, SQ + literal + SQ);
    return filter;
  }

  @Override
  public String propertyIsGreaterThanOrEqualTo(String propertyName, Date literal) {
    String filter =
        filterFactory.buildPropertyIsGreaterThanOrEqual(propertyName, getStringFromDate(literal));
    return filter;
  }

  @Override
  public String propertyIsGreaterThanOrEqualTo(String propertyName, int literal) {
    String filter =
        filterFactory.buildPropertyIsGreaterThanOrEqual(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsGreaterThanOrEqualTo(String propertyName, short literal) {
    String filter =
        filterFactory.buildPropertyIsGreaterThanOrEqual(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsGreaterThanOrEqualTo(String propertyName, long literal) {
    String filter =
        filterFactory.buildPropertyIsGreaterThanOrEqual(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsGreaterThanOrEqualTo(String propertyName, float literal) {
    String filter =
        filterFactory.buildPropertyIsGreaterThanOrEqual(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsGreaterThanOrEqualTo(String propertyName, double literal) {
    String filter =
        filterFactory.buildPropertyIsGreaterThanOrEqual(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsLessThan(String propertyName, String literal) {
    String filter = filterFactory.buildPropertyIsLessThan(propertyName, SQ + literal + SQ);
    return filter;
  }

  @Override
  public String propertyIsLessThan(String propertyName, Date literal) {
    String filter = filterFactory.buildPropertyIsLessThan(propertyName, getStringFromDate(literal));
    return filter;
  }

  @Override
  public String propertyIsLessThan(String propertyName, int literal) {
    String filter = filterFactory.buildPropertyIsLessThan(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsLessThan(String propertyName, short literal) {
    String filter = filterFactory.buildPropertyIsLessThan(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsLessThan(String propertyName, long literal) {
    String filter = filterFactory.buildPropertyIsLessThan(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsLessThan(String propertyName, float literal) {
    String filter = filterFactory.buildPropertyIsLessThan(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsLessThan(String propertyName, double literal) {
    String filter = filterFactory.buildPropertyIsLessThan(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsLessThanOrEqualTo(String propertyName, String literal) {
    String filter = filterFactory.buildPropertyIsLessThanOrEqual(propertyName, SQ + literal + SQ);
    return filter;
  }

  @Override
  public String propertyIsLessThanOrEqualTo(String propertyName, Date literal) {
    String filter =
        filterFactory.buildPropertyIsLessThanOrEqual(propertyName, getStringFromDate(literal));
    return filter;
  }

  @Override
  public String propertyIsLessThanOrEqualTo(String propertyName, int literal) {
    String filter =
        filterFactory.buildPropertyIsLessThanOrEqual(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsLessThanOrEqualTo(String propertyName, short literal) {
    String filter =
        filterFactory.buildPropertyIsLessThanOrEqual(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsLessThanOrEqualTo(String propertyName, long literal) {
    String filter =
        filterFactory.buildPropertyIsLessThanOrEqual(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsLessThanOrEqualTo(String propertyName, float literal) {
    String filter =
        filterFactory.buildPropertyIsLessThanOrEqual(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsLessThanOrEqualTo(String propertyName, double literal) {
    String filter =
        filterFactory.buildPropertyIsLessThanOrEqual(propertyName, String.valueOf(literal));
    return filter;
  }

  @Override
  public String propertyIsBetween(String propertyName, String lowerBoundary, String upperBoundary) {
    String filter =
        filterFactory.buildPropertyIsBetween(
            propertyName, SQ + lowerBoundary + SQ, SQ + upperBoundary + SQ);
    return filter;
  }

  @Override
  public String propertyIsBetween(String propertyName, Date lowerBoundary, Date upperBoundary) {
    String filter =
        filterFactory.buildPropertyIsBetween(
            propertyName, getStringFromDate(lowerBoundary), getStringFromDate(upperBoundary));
    return filter;
  }

  @Override
  public String propertyIsBetween(String propertyName, int lowerBoundary, int upperBoundary) {
    String filter =
        filterFactory.buildPropertyIsBetween(
            propertyName, String.valueOf(lowerBoundary), String.valueOf(upperBoundary));
    return filter;
  }

  @Override
  public String propertyIsBetween(String propertyName, short lowerBoundary, short upperBoundary) {
    String filter =
        filterFactory.buildPropertyIsBetween(
            propertyName, String.valueOf(lowerBoundary), String.valueOf(upperBoundary));
    return filter;
  }

  @Override
  public String propertyIsBetween(String propertyName, long lowerBoundary, long upperBoundary) {
    String filter =
        filterFactory.buildPropertyIsBetween(
            propertyName, String.valueOf(lowerBoundary), String.valueOf(upperBoundary));
    return filter;
  }

  @Override
  public String propertyIsBetween(String propertyName, float lowerBoundary, float upperBoundary) {
    String filter =
        filterFactory.buildPropertyIsBetween(
            propertyName, String.valueOf(lowerBoundary), String.valueOf(upperBoundary));
    return filter;
  }

  @Override
  public String propertyIsBetween(String propertyName, double lowerBoundary, double upperBoundary) {
    String filter =
        filterFactory.buildPropertyIsBetween(
            propertyName, String.valueOf(lowerBoundary), String.valueOf(upperBoundary));
    return filter;
  }

  @Override
  public String propertyIsNull(String propertyName) {
    String filter = filterFactory.buildPropertyIsNull(propertyName);
    return filter;
  }

  @Override
  public String propertyIsLike(String propertyName, String pattern, boolean isCaseSensitive) {
    String filter = filterFactory.buildPropertyIsLike(propertyName, pattern);
    return filter;
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
