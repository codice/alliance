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

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.types.Core;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;
import org.codice.alliance.nsili.common.GIAS.AttributeInformation;
import org.codice.alliance.nsili.common.GIAS.AttributeType;
import org.codice.alliance.nsili.common.GIAS.DateRange;
import org.codice.alliance.nsili.common.GIAS.Domain;
import org.codice.alliance.nsili.common.GIAS.DomainType;
import org.codice.alliance.nsili.common.GIAS.IntegerRange;
import org.codice.alliance.nsili.common.GIAS.RequirementMode;
import org.codice.alliance.nsili.common.NsiliConstants;
import org.codice.alliance.nsili.common.UCO.Coordinate2d;
import org.codice.alliance.nsili.common.UCO.Rectangle;
import org.junit.Before;
import org.junit.Test;

public class NsiliFilterDelegateTest {

  private static final String ANY_TEXT = Metacard.ANY_TEXT;

  private static NsiliFilterDelegate filterDelegate;

  private static final String PROPERTY = "publisher";

  private static final String DATE_PROPERTY = Core.METACARD_MODIFIED;

  private static final String ATTRIBUTE = "attribute";

  private static final String FALSE = "FALSE";

  private static final int INT = 1;

  private static final float FLOAT = 1.0f;

  private static final Rectangle RECTANGLE_DOMAIN =
      new Rectangle(new Coordinate2d(0.0, 0.0), new Coordinate2d(0.0, 0.0));

  private static final Date DATE = getDate();

  private static final int YEAR = 1969;

  private static final int MONTH = 12;

  private static final int DAY = 31;

  // BQS Date Definition = 'year/month/day hour:minute:second'
  private static final String DATE_STRING = "1970/01/31 00:00:00";

  private static final String WKT =
      "POLYGON ((-96.3082 35.246, -96.3082 51.5455, -84.9437 51.5455, -84.9437 35.246, -96.3082 35.246))";

  private static final String POLYGON = "POLYGON";

  private static final String OUTSIDE = "outside";

  private static final String INSIDE = "inside";

  private static final String INTERSECT = "intersect";

  private static final String WITHIN = "within";

  private static final String BEYOND = "beyond";

  private static final String WKT_DISTANCE = "1.0 meters";

  @Before
  public void setUp() {
    filterDelegate =
        new NsiliFilterDelegate(generateAttributeInformation(), NsiliConstants.NSIL_ALL_VIEW);
  }

  @Test
  public void testPropertyIsEqualToStringLiteral() {
    String filter = filterDelegate.propertyIsEqualTo(PROPERTY, ATTRIBUTE, false);
    assertThat(
        filter,
        is(
            getPrimary(
                NsiliFilterFactory.EQ,
                NsiliFilterDelegate.SQ + ATTRIBUTE + NsiliFilterDelegate.SQ)));
  }

  @Test
  public void testPropertyIsEqualToDateLiteral() {
    String filter = filterDelegate.propertyIsEqualTo(PROPERTY, DATE);
    assertThat(
        filter,
        is(
            getPrimary(
                NsiliFilterFactory.EQ,
                NsiliFilterDelegate.SQ + DATE_STRING + NsiliFilterDelegate.SQ)));
  }

  @Test
  public void testPropertyIsEqualToIntLiteral() {
    String filter = filterDelegate.propertyIsEqualTo(PROPERTY, INT);
    assertThat(filter, is(getPrimary(NsiliFilterFactory.EQ, INT)));
  }

  @Test
  public void testPropertyIsEqualToShortLiteral() {
    String filter = filterDelegate.propertyIsEqualTo(PROPERTY, (short) INT);
    assertThat(filter, is(getPrimary(NsiliFilterFactory.EQ, INT)));
  }

  @Test
  public void testPropertyIsEqualToLongLiteral() {
    String filter = filterDelegate.propertyIsEqualTo(PROPERTY, (long) INT);
    assertThat(filter, is(getPrimary(NsiliFilterFactory.EQ, INT)));
  }

  @Test
  public void testPropertyIsEqualToFloatLiteral() {
    String filter = filterDelegate.propertyIsEqualTo(PROPERTY, FLOAT);
    assertThat(filter, is(getPrimary(NsiliFilterFactory.EQ, FLOAT)));
  }

  @Test
  public void testPropertyIsEqualToDoubleLiteral() {
    String filter = filterDelegate.propertyIsEqualTo(PROPERTY, (double) FLOAT);
    assertThat(filter, is(getPrimary(NsiliFilterFactory.EQ, FLOAT)));
  }

  @Test
  public void testPropertyIsEqualToBooleanLiteral() {
    String filter = filterDelegate.propertyIsEqualTo(PROPERTY, false);
    assertThat(
        filter,
        is(
            getPrimary(
                NsiliFilterFactory.EQ, NsiliFilterDelegate.SQ + FALSE + NsiliFilterDelegate.SQ)));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testPropertyIsEqualToByteArray() {
    filterDelegate.propertyIsEqualTo(PROPERTY, PROPERTY.getBytes());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testPropertyIsEqualToObjectLiteral() {
    filterDelegate.propertyIsEqualTo(PROPERTY, (Object) PROPERTY);
  }

  @Test
  public void testPropertyIsNotEqualToStringLiteral() {
    String filter = filterDelegate.propertyIsNotEqualTo(PROPERTY, ATTRIBUTE, false);
    assertThat(
        filter,
        is(
            NsiliFilterFactory.NOT
                + getPrimary(
                    NsiliFilterFactory.EQ,
                    NsiliFilterDelegate.SQ + ATTRIBUTE + NsiliFilterDelegate.SQ)));
  }

  @Test
  public void testPropertyIsNotEqualToDateLiteral() {
    String filter = filterDelegate.propertyIsNotEqualTo(PROPERTY, DATE);
    assertThat(
        filter,
        is(
            NsiliFilterFactory.NOT
                + getPrimary(
                    NsiliFilterFactory.EQ,
                    NsiliFilterDelegate.SQ + DATE_STRING + NsiliFilterDelegate.SQ)));
  }

  @Test
  public void testPropertyIsNotEqualToIntLiteral() {
    String filter = filterDelegate.propertyIsNotEqualTo(PROPERTY, INT);
    assertThat(filter, is(NsiliFilterFactory.NOT + getPrimary(NsiliFilterFactory.EQ, INT)));
  }

  @Test
  public void testPropertyIsNotEqualToShortLiteral() {
    String filter = filterDelegate.propertyIsNotEqualTo(PROPERTY, (short) INT);
    assertThat(filter, is(NsiliFilterFactory.NOT + getPrimary(NsiliFilterFactory.EQ, INT)));
  }

  @Test
  public void testPropertyIsNotEqualToLongLiteral() {
    String filter = filterDelegate.propertyIsNotEqualTo(PROPERTY, (long) INT);
    assertThat(filter, is(NsiliFilterFactory.NOT + getPrimary(NsiliFilterFactory.EQ, INT)));
  }

  @Test
  public void testPropertyIsNotEqualToFloatLiteral() {
    String filter = filterDelegate.propertyIsNotEqualTo(PROPERTY, FLOAT);
    assertThat(filter, is(NsiliFilterFactory.NOT + getPrimary(NsiliFilterFactory.EQ, FLOAT)));
  }

  @Test
  public void testPropertyIsNotEqualToDoubleLiteral() {
    String filter = filterDelegate.propertyIsNotEqualTo(PROPERTY, (double) FLOAT);
    assertThat(filter, is(NsiliFilterFactory.NOT + getPrimary(NsiliFilterFactory.EQ, FLOAT)));
  }

  @Test
  public void testPropertyIsNotEqualToBooleanLiteral() {
    String filter = filterDelegate.propertyIsNotEqualTo(PROPERTY, false);
    assertThat(
        filter,
        is(
            NsiliFilterFactory.NOT
                + getPrimary(
                    NsiliFilterFactory.EQ,
                    NsiliFilterDelegate.SQ + FALSE + NsiliFilterDelegate.SQ)));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testPropertyIsNotEqualToByteArrayLiteral() {
    filterDelegate.propertyIsNotEqualTo(PROPERTY, PROPERTY.getBytes());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testPropertyIsNotEqualToObjectLiteral() {
    filterDelegate.propertyIsNotEqualTo(PROPERTY, (Object) PROPERTY);
  }

  @Test
  public void testPropertyIsGreaterThanStringLiteral() {
    String filter = filterDelegate.propertyIsGreaterThan(PROPERTY, ATTRIBUTE);
    assertThat(
        filter,
        is(
            getPrimary(
                NsiliFilterFactory.GT,
                NsiliFilterDelegate.SQ + ATTRIBUTE + NsiliFilterDelegate.SQ)));
  }

  @Test
  public void testPropertyIsGreaterThanDateLiteral() {
    String filter = filterDelegate.propertyIsGreaterThan(PROPERTY, DATE);
    assertThat(
        filter,
        is(
            getPrimary(
                NsiliFilterFactory.GT,
                NsiliFilterDelegate.SQ + DATE_STRING + NsiliFilterDelegate.SQ)));
  }

  @Test
  public void testPropertyIsGreaterThanIntLiteral() {
    String filter = filterDelegate.propertyIsGreaterThan(PROPERTY, INT);
    assertThat(filter, is(getPrimary(NsiliFilterFactory.GT, INT)));
  }

  @Test
  public void testPropertyIsGreaterThanShortLiteral() {
    String filter = filterDelegate.propertyIsGreaterThan(PROPERTY, (short) INT);
    assertThat(filter, is(getPrimary(NsiliFilterFactory.GT, INT)));
  }

  @Test
  public void testPropertyIsGreaterThanLongLiteral() {
    String filter = filterDelegate.propertyIsGreaterThan(PROPERTY, (long) INT);
    assertThat(filter, is(getPrimary(NsiliFilterFactory.GT, INT)));
  }

  @Test
  public void testPropertyIsGreaterThanFloatLiteral() {
    String filter = filterDelegate.propertyIsGreaterThan(PROPERTY, FLOAT);
    assertThat(filter, is(getPrimary(NsiliFilterFactory.GT, FLOAT)));
  }

  @Test
  public void testPropertyIsGreaterThanDoubleLiteral() {
    String filter = filterDelegate.propertyIsGreaterThan(PROPERTY, (double) FLOAT);
    assertThat(filter, is(getPrimary(NsiliFilterFactory.GT, FLOAT)));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testPropertyIsGreaterThanBooleanLiteral() {
    filterDelegate.propertyIsGreaterThan(PROPERTY, false);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testPropertyIsGreaterThanByteArrayLiteral() {
    filterDelegate.propertyIsGreaterThan(PROPERTY, PROPERTY.getBytes());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testPropertyIsGreaterThanObjectLiteral() {
    filterDelegate.propertyIsGreaterThan(PROPERTY, (Object) PROPERTY);
  }

  @Test
  public void testPropertyIsGreaterThanOrEqualToStringLiteral() {
    String filter = filterDelegate.propertyIsGreaterThanOrEqualTo(PROPERTY, ATTRIBUTE);
    assertThat(
        filter,
        is(
            getPrimary(
                NsiliFilterFactory.GTE,
                NsiliFilterDelegate.SQ + ATTRIBUTE + NsiliFilterDelegate.SQ)));
  }

  @Test
  public void testPropertyIsGreaterThanOrEqualToDateLiteral() {
    String filter = filterDelegate.propertyIsGreaterThanOrEqualTo(PROPERTY, DATE);
    assertThat(
        filter,
        is(
            getPrimary(
                NsiliFilterFactory.GTE,
                NsiliFilterDelegate.SQ + DATE_STRING + NsiliFilterDelegate.SQ)));
  }

  @Test
  public void testPropertyIsGreaterThanOrEqualToIntLiteral() {
    String filter = filterDelegate.propertyIsGreaterThanOrEqualTo(PROPERTY, INT);
    assertThat(filter, is(getPrimary(NsiliFilterFactory.GTE, INT)));
  }

  @Test
  public void testPropertyIsGreaterThanOrEqualToShortLiteral() {
    String filter = filterDelegate.propertyIsGreaterThanOrEqualTo(PROPERTY, (short) INT);
    assertThat(filter, is(getPrimary(NsiliFilterFactory.GTE, INT)));
  }

  @Test
  public void testPropertyIsGreaterThanOrEqualToLongLiteral() {
    String filter = filterDelegate.propertyIsGreaterThanOrEqualTo(PROPERTY, (long) INT);
    assertThat(filter, is(getPrimary(NsiliFilterFactory.GTE, INT)));
  }

  @Test
  public void testPropertyIsGreaterThanOrEqualToFloatLiteral() {
    String filter = filterDelegate.propertyIsGreaterThanOrEqualTo(PROPERTY, FLOAT);
    assertThat(filter, is(getPrimary(NsiliFilterFactory.GTE, FLOAT)));
  }

  @Test
  public void testPropertyIsGreaterThanOrEqualToDoubleLiteral() {
    String filter = filterDelegate.propertyIsGreaterThanOrEqualTo(PROPERTY, (double) FLOAT);
    assertThat(filter, is(getPrimary(NsiliFilterFactory.GTE, FLOAT)));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testPropertyIsGreaterThanOrEqualToBooleanLiteral() {
    filterDelegate.propertyIsGreaterThanOrEqualTo(PROPERTY, false);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testPropertyIsGreaterThanOrEqualToByteArrayLiteral() {
    filterDelegate.propertyIsGreaterThanOrEqualTo(PROPERTY, PROPERTY.getBytes());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testPropertyIsGreaterThanOrEqualToObjectLiteral() {
    filterDelegate.propertyIsGreaterThanOrEqualTo(PROPERTY, (Object) PROPERTY);
  }

  @Test
  public void testPropertyIsLessThanStringLiteral() {
    String filter = filterDelegate.propertyIsLessThan(PROPERTY, ATTRIBUTE);
    assertThat(
        filter,
        is(
            getPrimary(
                NsiliFilterFactory.LT,
                NsiliFilterDelegate.SQ + ATTRIBUTE + NsiliFilterDelegate.SQ)));
  }

  @Test
  public void testPropertyIsLessThanDateLiteral() {
    String filter = filterDelegate.propertyIsLessThan(PROPERTY, DATE);
    assertThat(
        filter,
        is(
            getPrimary(
                NsiliFilterFactory.LT,
                NsiliFilterDelegate.SQ + DATE_STRING + NsiliFilterDelegate.SQ)));
  }

  @Test
  public void testPropertyIsLessThanIntLiteral() {
    String filter = filterDelegate.propertyIsLessThan(PROPERTY, INT);
    assertThat(filter, is(getPrimary(NsiliFilterFactory.LT, INT)));
  }

  @Test
  public void testPropertyIsLessThanShortLiteral() {
    String filter = filterDelegate.propertyIsLessThan(PROPERTY, (short) INT);
    assertThat(filter, is(getPrimary(NsiliFilterFactory.LT, INT)));
  }

  @Test
  public void testPropertyIsLessThanLongLiteral() {
    String filter = filterDelegate.propertyIsLessThan(PROPERTY, (long) INT);
    assertThat(filter, is(getPrimary(NsiliFilterFactory.LT, INT)));
  }

  @Test
  public void testPropertyIsLessThanFloatLiteral() {
    String filter = filterDelegate.propertyIsLessThan(PROPERTY, FLOAT);
    assertThat(filter, is(getPrimary(NsiliFilterFactory.LT, FLOAT)));
  }

  @Test
  public void testPropertyIsLessThanDoubleLiteral() {
    String filter = filterDelegate.propertyIsLessThan(PROPERTY, (double) FLOAT);
    assertThat(filter, is(getPrimary(NsiliFilterFactory.LT, FLOAT)));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testPropertyIsLessThanBooleanLiteral() {
    filterDelegate.propertyIsLessThan(PROPERTY, false);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testPropertyIsLessThanByteArrayLiteral() {
    filterDelegate.propertyIsLessThan(PROPERTY, PROPERTY.getBytes());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testPropertyIsLessThanObjectLiteral() {
    filterDelegate.propertyIsLessThan(PROPERTY, (Object) PROPERTY);
  }

  @Test
  public void testPropertyIsLessThanOrEqualToStringLiteral() {
    String filter = filterDelegate.propertyIsLessThanOrEqualTo(PROPERTY, ATTRIBUTE);
    assertThat(
        filter,
        is(
            getPrimary(
                NsiliFilterFactory.LTE,
                NsiliFilterDelegate.SQ + ATTRIBUTE + NsiliFilterDelegate.SQ)));
  }

  @Test
  public void testPropertyIsLessThanOrEqualToDateLiteral() {
    String filter = filterDelegate.propertyIsLessThanOrEqualTo(PROPERTY, DATE);
    assertThat(
        filter,
        is(
            getPrimary(
                NsiliFilterFactory.LTE,
                NsiliFilterDelegate.SQ + DATE_STRING + NsiliFilterDelegate.SQ)));
  }

  @Test
  public void testPropertyIsLessThanOrEqualToIntLiteral() {
    String filter = filterDelegate.propertyIsLessThanOrEqualTo(PROPERTY, INT);
    assertThat(filter, is(getPrimary(NsiliFilterFactory.LTE, INT)));
  }

  @Test
  public void testPropertyIsLessThanOrEqualToShortLiteral() {
    String filter = filterDelegate.propertyIsLessThanOrEqualTo(PROPERTY, (short) INT);
    assertThat(filter, is(getPrimary(NsiliFilterFactory.LTE, INT)));
  }

  @Test
  public void testPropertyIsLessThanOrEqualToLongLiteral() {
    String filter = filterDelegate.propertyIsLessThanOrEqualTo(PROPERTY, (long) INT);
    assertThat(filter, is(getPrimary(NsiliFilterFactory.LTE, INT)));
  }

  @Test
  public void testPropertyIsLessThanOrEqualToFloatLiteral() {
    String filter = filterDelegate.propertyIsLessThanOrEqualTo(PROPERTY, FLOAT);
    assertThat(filter, is(getPrimary(NsiliFilterFactory.LTE, FLOAT)));
  }

  @Test
  public void testPropertyIsLessThanOrEqualToDoubleLiteral() {
    String filter = filterDelegate.propertyIsLessThanOrEqualTo(PROPERTY, (double) FLOAT);
    assertThat(filter, is(getPrimary(NsiliFilterFactory.LTE, FLOAT)));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testPropertyIsLessThanOrEqualToBooleanLiteral() {
    filterDelegate.propertyIsLessThanOrEqualTo(PROPERTY, false);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testPropertyIsLessThanOrEqualToByteArrayLiteral() {
    filterDelegate.propertyIsLessThanOrEqualTo(PROPERTY, PROPERTY.getBytes());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testPropertyIsLessThanOrEqualToObjectLiteral() {
    filterDelegate.propertyIsLessThanOrEqualTo(PROPERTY, (Object) PROPERTY);
  }

  @Test
  public void testPropertyBetweenStringLiterals() {
    String filter = filterDelegate.propertyIsBetween(PROPERTY, ATTRIBUTE, ATTRIBUTE);
    assertThat(
        filter, is(getPrimaryBetween(PROPERTY, "'" + ATTRIBUTE + "'", "'" + ATTRIBUTE + "'")));
  }

  @Test
  public void testPropertyBetweenIntLiterals() {
    String filter = filterDelegate.propertyIsBetween(PROPERTY, INT, INT);
    assertThat(filter, is(getPrimaryBetween(PROPERTY, String.valueOf(INT), String.valueOf(INT))));
  }

  @Test
  public void testPropertyBetweenShortLiterals() {
    String filter = filterDelegate.propertyIsBetween(PROPERTY, (short) INT, (short) INT);
    assertThat(filter, is(getPrimaryBetween(PROPERTY, String.valueOf(INT), String.valueOf(INT))));
  }

  @Test
  public void testPropertyBetweenLongLiterals() {
    String filter = filterDelegate.propertyIsBetween(PROPERTY, (long) INT, (long) INT);
    assertThat(filter, is(getPrimaryBetween(PROPERTY, String.valueOf(INT), String.valueOf(INT))));
  }

  @Test
  public void testPropertyBetweenFloatLiterals() {
    String filter = filterDelegate.propertyIsBetween(PROPERTY, FLOAT, FLOAT);
    assertThat(
        filter, is(getPrimaryBetween(PROPERTY, String.valueOf(FLOAT), String.valueOf(FLOAT))));
  }

  @Test
  public void testPropertyBetweenDoubleLiterals() {
    String filter = filterDelegate.propertyIsBetween(PROPERTY, (double) FLOAT, (double) FLOAT);
    assertThat(
        filter, is(getPrimaryBetween(PROPERTY, String.valueOf(FLOAT), String.valueOf(FLOAT))));
  }

  @Test
  public void testPropertyNull() {
    String filter = filterDelegate.propertyIsNull(PROPERTY);
    assertThat(
        filter,
        is(
            NsiliFilterFactory.NOT
                + getPrimary(NsiliFilterDelegate.EMPTY_STRING, NsiliFilterFactory.EXISTS)));
  }

  @Test
  public void testPropertyLike() {
    String filter = filterDelegate.propertyIsLike(ANY_TEXT, ATTRIBUTE, false);

    List<AttributeInformation> attributeInformationList =
        generateAttributeInformation().get(NsiliConstants.NSIL_ALL_VIEW);
    StringBuilder result = new StringBuilder();
    for (AttributeInformation attributeInformation : attributeInformationList) {
      if (attributeInformation.attribute_type.equals(AttributeType.TEXT)
          && attributeInformation.attribute_domain.discriminator() == DomainType.TEXT_VALUE) {
        result.append(
            getPrimary(
                    attributeInformation.attribute_name,
                    NsiliFilterFactory.LIKE,
                    NsiliFilterDelegate.SQ
                        + NsiliFilterDelegate.WILDCARD
                        + ATTRIBUTE
                        + NsiliFilterDelegate.WILDCARD
                        + NsiliFilterDelegate.SQ)
                + NsiliFilterFactory.OR);
      }
    }
    String resultString = result.toString();
    resultString = resultString.substring(0, result.length() - 4);
    assertThat(filter, is(resultString));

    assertThat(filter, not(containsString(NsiliConstants.ADVANCED_GEOSPATIAL)));
  }

  @Test
  public void testPropertyLikeNullQueryableAttributes() {
    NsiliFilterDelegate filterDelegate =
        new NsiliFilterDelegate(null, NsiliConstants.NSIL_ALL_VIEW);
    String filter = filterDelegate.propertyIsLike(ANY_TEXT, ATTRIBUTE, false);
    assertThat(filter, is(NsiliFilterDelegate.EMPTY_STRING));
  }

  @Test
  public void testPropertyLikeNullQueryableAttributesForView() {
    Map<String, List<AttributeInformation>> map = new HashMap<>();
    map.put(NsiliConstants.NSIL_ALL_VIEW, null);
    NsiliFilterDelegate filterDelegate = new NsiliFilterDelegate(map, NsiliConstants.NSIL_ALL_VIEW);
    String filter = filterDelegate.propertyIsLike(ANY_TEXT, ATTRIBUTE, false);
    assertThat(filter, is(NsiliFilterDelegate.EMPTY_STRING));
  }

  @Test
  public void testPropertyLikeEmptyAttributesForView() {
    Map<String, List<AttributeInformation>> map = new HashMap<>();
    List<AttributeInformation> list = new ArrayList<>();
    map.put(NsiliConstants.NSIL_ALL_VIEW, list);
    NsiliFilterDelegate filterDelegate = new NsiliFilterDelegate(map, NsiliConstants.NSIL_ALL_VIEW);
    String filter = filterDelegate.propertyIsLike(ANY_TEXT, ATTRIBUTE, false);
    assertThat(filter, is(NsiliFilterDelegate.EMPTY_STRING));
  }

  @Test
  public void testAndOneElementList() {
    List<String> filterList = new ArrayList<>();
    filterList.add(filterDelegate.propertyIsBetween(PROPERTY, INT, INT));
    String filter = filterDelegate.and(filterList);
    assertThat(filter, is(getPrimaryBetween(PROPERTY, String.valueOf(INT), String.valueOf(INT))));
  }

  @Test
  public void testAndEmptyList() {
    List<String> filterList = new ArrayList<>();
    String filter = filterDelegate.and(filterList);
    assertThat(StringUtils.isEmpty(filter), is(true));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testAndInvalidFilter() {
    List<String> filterList = new ArrayList<>();
    filterList.add(filterDelegate.propertyIsEqualTo(PROPERTY, ATTRIBUTE.getBytes()));
    filterDelegate.and(filterList);
  }

  @Test
  public void testOrEmptyFilter() {
    List<String> filterList = new ArrayList<>();
    String filter = filterDelegate.or(filterList);
    assertThat(StringUtils.isEmpty(filter), is(true));
  }

  @Test
  public void testOrOneElementList() {
    List<String> filterList = new ArrayList<>();
    filterList.add(filterDelegate.propertyIsBetween(PROPERTY, INT, INT));
    String filter = filterDelegate.or(filterList);
    assertThat(filter, is(getPrimaryBetween(PROPERTY, String.valueOf(INT), String.valueOf(INT))));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testOrInvalidFilter() {
    List<String> filterList = new ArrayList<>();
    filterList.add(filterDelegate.propertyIsEqualTo(PROPERTY, ATTRIBUTE.getBytes()));
    filterDelegate.or(filterList);
  }

  @Test
  public void testNullQueryableAttributes() {
    NsiliFilterDelegate filterDelegate =
        new NsiliFilterDelegate(null, NsiliConstants.NSIL_ALL_VIEW);
    String filter = filterDelegate.propertyIsLike(PROPERTY, ATTRIBUTE, false);
    assertThat(filter, is(NsiliFilterDelegate.EMPTY_STRING));
  }

  @Test
  public void testNullAttributesList() {
    NsiliFilterDelegate filterDelegate =
        new NsiliFilterDelegate(generateAttributeInformation(), PROPERTY);
    String filter = filterDelegate.propertyIsLike(PROPERTY, ATTRIBUTE, false);
    assertThat(filter, is(NsiliFilterDelegate.EMPTY_STRING));
  }

  @Test
  public void testBeforeUnsupportedTemporal() {
    String filter = filterDelegate.before(PROPERTY, DATE);
    assertThat(filter, is(NsiliFilterDelegate.EMPTY_STRING));
  }

  @Test
  public void testBeforeSupportedTemporal() {
    String filter = filterDelegate.before(DATE_PROPERTY, DATE);
    assertThat(
        filter,
        is(
            getPrimary(
                NsiliConstants.NSIL_CARD + "." + NsiliConstants.DATE_TIME_MODIFIED,
                NsiliFilterFactory.LTE,
                NsiliFilterDelegate.SQ + DATE_STRING + NsiliFilterDelegate.SQ)));
  }

  @Test
  public void testAfterUnsupportedTemporal() {
    String filter = filterDelegate.after(PROPERTY, DATE);
    assertThat(filter, is(NsiliFilterDelegate.EMPTY_STRING));
  }

  @Test
  public void testAfterSupportedTemporal() {
    String filter = filterDelegate.after(DATE_PROPERTY, DATE);
    assertThat(
        filter,
        is(
            getPrimary(
                NsiliConstants.NSIL_CARD + "." + NsiliConstants.DATE_TIME_MODIFIED,
                NsiliFilterFactory.GTE,
                NsiliFilterDelegate.SQ + DATE_STRING + NsiliFilterDelegate.SQ)));
  }

  @Test
  public void testDuringUnsupported() {
    String filter = filterDelegate.during(PROPERTY, DATE, DATE);
    assertThat(filter, is(NsiliFilterDelegate.EMPTY_STRING));
  }

  @Test
  public void testDuringSupported() {
    String filter = filterDelegate.during(DATE_PROPERTY, DATE, DATE);
    assertThat(StringUtils.isEmpty(filter), is(false));
  }

  @Test
  public void testDisjointUnsupported() {
    String filter = filterDelegate.disjoint(PROPERTY, WKT);
    assertThat(filter, is(NsiliFilterDelegate.EMPTY_STRING));
  }

  @Test
  public void testDisjointSupported() {
    String filter = filterDelegate.disjoint(Metacard.ANY_GEO, WKT);
    assertThat(StringUtils.isEmpty(filter), is(false));
    assertThat(filter, containsString(POLYGON));
    assertThat(filter, containsString(OUTSIDE));
  }

  @Test
  public void testWithinUnsupported() {
    String filter = filterDelegate.within(PROPERTY, WKT);
    assertThat(filter, is(NsiliFilterDelegate.EMPTY_STRING));
  }

  @Test
  public void testWithinSupported() {
    String filter = filterDelegate.within(Metacard.ANY_GEO, WKT);
    assertThat(StringUtils.isEmpty(filter), is(false));
    assertThat(filter, containsString(POLYGON));
    assertThat(filter, containsString(INSIDE));
  }

  @Test
  public void testIntersectsUnsupported() {
    String filter = filterDelegate.intersects(PROPERTY, WKT);
    assertThat(filter, is(NsiliFilterDelegate.EMPTY_STRING));
  }

  @Test
  public void testIntersectsSupported() {
    String filter = filterDelegate.intersects(Metacard.ANY_GEO, WKT);
    assertThat(StringUtils.isEmpty(filter), is(false));
    assertThat(filter, containsString(POLYGON));
    assertThat(filter, containsString(INTERSECT));
  }

  @Test
  public void testDWithinUnsupported() {
    String filter = filterDelegate.dwithin(PROPERTY, WKT, FLOAT);
    assertThat(filter, is(NsiliFilterDelegate.EMPTY_STRING));
  }

  @Test
  public void testDWithinSupported() {
    String filter = filterDelegate.dwithin(Metacard.ANY_GEO, WKT, FLOAT);
    assertThat(StringUtils.isEmpty(filter), is(false));
    assertThat(filter, containsString(POLYGON));
    assertThat(filter, containsString(WITHIN));
    assertThat(filter, containsString(WKT_DISTANCE));
  }

  @Test
  public void testBeyondUnsupported() {
    String filter = filterDelegate.beyond(PROPERTY, WKT, FLOAT);
    assertThat(filter, is(NsiliFilterDelegate.EMPTY_STRING));
  }

  @Test
  public void testBeyondSupported() {
    String filter = filterDelegate.beyond(Metacard.ANY_GEO, WKT, FLOAT);
    assertThat(StringUtils.isEmpty(filter), is(false));
    assertThat(filter, containsString(POLYGON));
    assertThat(filter, containsString(BEYOND));
    assertThat(filter, containsString(WKT_DISTANCE));
  }

  @Test
  public void testBadWktString() {
    String filter = filterDelegate.beyond(Metacard.ANY_GEO, PROPERTY, FLOAT);
    assertThat(filter, is(NsiliFilterDelegate.EMPTY_STRING));
  }

  private static Map<String, List<AttributeInformation>> generateAttributeInformation() {
    List<AttributeInformation> attributeInformationList = new ArrayList<>();

    Domain domain = new Domain();

    domain.t(36);
    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.NSIL_COMMON + "." + NsiliConstants.IDENTIFIER_UUID,
            AttributeType.TEXT,
            domain,
            NsiliFilterDelegate.EMPTY_STRING,
            NsiliFilterDelegate.EMPTY_STRING,
            RequirementMode.MANDATORY,
            NsiliFilterDelegate.EMPTY_STRING,
            false,
            true));

    domain = new Domain();
    DateRange dateRange = new DateRange();
    domain.d(dateRange);
    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.NSIL_CARD + "." + NsiliConstants.DATE_TIME_MODIFIED,
            AttributeType.TEXT,
            domain,
            NsiliFilterDelegate.EMPTY_STRING,
            NsiliFilterDelegate.EMPTY_STRING,
            RequirementMode.MANDATORY,
            NsiliFilterDelegate.EMPTY_STRING,
            false,
            true));

    domain = new Domain();
    domain.g(RECTANGLE_DOMAIN);
    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.NSIL_COVERAGE + "." + NsiliConstants.SPATIAL_GEOGRAPHIC_REF_BOX,
            AttributeType.UCOS_RECTANGLE,
            domain,
            NsiliFilterDelegate.EMPTY_STRING,
            NsiliFilterDelegate.EMPTY_STRING,
            RequirementMode.OPTIONAL,
            NsiliFilterDelegate.EMPTY_STRING,
            true,
            true));

    domain = new Domain();
    domain.ir(new IntegerRange(0, 100));
    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.NSIL_IMAGERY + "." + NsiliConstants.NUMBER_OF_BANDS,
            AttributeType.INTEGER,
            domain,
            NsiliFilterDelegate.EMPTY_STRING,
            NsiliFilterDelegate.EMPTY_STRING,
            RequirementMode.MANDATORY,
            NsiliFilterDelegate.EMPTY_STRING,
            true,
            true));

    HashMap<String, List<AttributeInformation>> map = new HashMap<>();
    map.put(NsiliConstants.NSIL_ALL_VIEW, attributeInformationList);
    return map;
  }

  private static AttributeInformation createAttributeInformation(
      String attributeName,
      AttributeType attributeType,
      Domain domain,
      String units,
      String reference,
      RequirementMode requirementMode,
      String description,
      boolean sortable,
      boolean updateable) {
    return new AttributeInformation(
        attributeName,
        attributeType,
        domain,
        units,
        reference,
        requirementMode,
        description,
        sortable,
        updateable);
  }

  private String getPrimary(String operator, Object attribute) {
    return getPrimary(NsiliConstants.NSIL_CARD + "." + PROPERTY, operator, attribute);
  }

  private String getPrimary(String property, String operator, Object attribute) {
    return NsiliFilterFactory.LP + property + operator + attribute + NsiliFilterFactory.RP;
  }

  private String getPrimaryBetween(String property, String lower, String upper) {
    return NsiliFilterFactory.LP
        + NsiliConstants.NSIL_CARD
        + "."
        + property
        + NsiliFilterFactory.GTE
        + lower
        + NsiliFilterFactory.AND
        + NsiliConstants.NSIL_CARD
        + "."
        + property
        + NsiliFilterFactory.LTE
        + upper
        + NsiliFilterFactory.RP;
  }

  private static Date getDate() {
    Calendar calendar = new GregorianCalendar(YEAR, MONTH, DAY);
    calendar.setTimeZone(TimeZone.getTimeZone(NsiliFilterDelegate.UTC));
    return calendar.getTime();
  }
}
