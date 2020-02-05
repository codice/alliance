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
package org.codice.alliance.libs.klv;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.impl.AttributeImpl;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

public class GeometryUtilityTest {

  private static final String FIELD = "field";

  private WKTReader wktReader;

  private WKTWriter wktWriter;

  @Before
  public void setup() {
    wktReader = new WKTReader();
    wktWriter = new WKTWriter();
  }

  @Test
  public void testBasicUnion() throws ParseException {

    Attribute attribute =
        new AttributeImpl(
            FIELD,
            Arrays.asList(
                "POLYGON (( 0 0, 10 0, 10 10, 0 10, 0 0))",
                "POLYGON (( 5 5, 15 5, 15 15, 5 15, 5 5))"));

    Optional<String> optionalWkt =
        GeometryUtility.createUnionOfGeometryAttribute(
            wktReader, wktWriter, attribute, new GeometryOperator.Context());

    Geometry actual = wktReader.read(optionalWkt.get()).norm();

    Geometry expected =
        wktReader.read("POLYGON (( 0 0, 10 0, 10 5, 15 5, 15 15, 5 15, 5 10, 0 10, 0 0 ))").norm();

    assertThat(actual, is(expected));
  }

  @Test
  public void testEmptyData() {

    Attribute attribute = new AttributeImpl(FIELD, Collections.emptyList());

    Optional<String> optionalWkt =
        GeometryUtility.createUnionOfGeometryAttribute(
            wktReader, wktWriter, attribute, new GeometryOperator.Context());

    assertThat(optionalWkt.isPresent(), is(false));
  }

  @Test
  public void testBadData() {
    Attribute attribute =
        new AttributeImpl(
            FIELD, Collections.singletonList("POLYGON (( x 0, 10 0, 10 10, 0 10, 0 0))"));

    Optional<String> optionalWkt =
        GeometryUtility.createUnionOfGeometryAttribute(
            wktReader, wktWriter, attribute, new GeometryOperator.Context());

    assertThat(optionalWkt.isPresent(), is(false));
  }

  @Test
  public void testGoodDataFollowedByBadData() throws ParseException {

    Attribute attribute =
        new AttributeImpl(
            FIELD,
            Arrays.asList(
                "POLYGON (( 0 0, 10 0, 10 10, 0 10, 0 0))",
                "POLYGON (( x 5, 15 5, 15 15, 5 15, 5 5))"));

    Optional<String> optionalWkt =
        GeometryUtility.createUnionOfGeometryAttribute(
            wktReader, wktWriter, attribute, new GeometryOperator.Context());

    Geometry actual = wktReader.read(optionalWkt.get()).norm();

    Geometry expected = wktReader.read("POLYGON (( 0 0, 10 0, 10 10, 0 10, 0 0))").norm();

    assertThat(actual, is(expected));
  }

  @Test
  public void testBadDataFollowedByGoodData() throws ParseException {

    Attribute attribute =
        new AttributeImpl(
            FIELD,
            Arrays.asList(
                "POLYGON (( x 0, 10 0, 10 10, 0 10, 0 0))",
                "POLYGON (( 5 5, 15 5, 15 15, 5 15, 5 5))"));

    Optional<String> optionalWkt =
        GeometryUtility.createUnionOfGeometryAttribute(
            wktReader, wktWriter, attribute, new GeometryOperator.Context());

    Geometry actual = wktReader.read(optionalWkt.get()).norm();

    Geometry expected = wktReader.read("POLYGON (( 5 5, 15 5, 15 15, 5 15, 5 5))").norm();

    assertThat(actual, is(expected));
  }

  @Test
  public void testBadDataFollowedByBadData() throws ParseException {

    Attribute attribute =
        new AttributeImpl(
            FIELD,
            Arrays.asList(
                "POLYGON (( x 0, 10 0, 10 10, 0 10, 0 0))",
                "POLYGON (( x 5, 15 5, 15 15, 5 15, 5 5))"));

    Optional<String> optionalWkt =
        GeometryUtility.createUnionOfGeometryAttribute(
            wktReader, wktWriter, attribute, new GeometryOperator.Context());

    assertThat(optionalWkt.isPresent(), is(false));
  }

  @Test
  public void testUnionWithInvalidGeo() throws ParseException {
    String polygonWithHole =
        "POLYGON ((-2.009211 51.199649, -1.99911 51.231578, -1.915058 51.240884, "
            + "-1.9201379049078626 51.228738313629776, -1.895395 51.226235, "
            + "-1.9781007533907866 51.196335611502384, -2.009211 51.199649), "
            + "(-1.9880383632176948 51.194513502505224, -1.9848251778950785 "
            + "51.19299587819516, -1.9838038434104859 51.1939685777043, "
            + "-1.9880383632176948 51.194513502505224))";
    Attribute attribute = new AttributeImpl(FIELD, Arrays.asList(polygonWithHole));

    Optional<String> optionalWkt =
        GeometryUtility.createUnionOfGeometryAttribute(
            wktReader, wktWriter, attribute, new GeometryOperator.Context());

    Geometry actual = wktReader.read(optionalWkt.get()).norm();

    assertThat(actual.isValid(), is(true));
  }

  @Test
  public void testAttributeToLineStringWithNullGeometry() {

    Attribute attribute =
        new AttributeImpl(FIELD, Arrays.asList("POINT ( 0 0 )", "POINT ( 10 10 )"));

    GeometryOperator geometryOperator = mock(GeometryOperator.class);
    when(geometryOperator.apply(any(), any())).thenReturn(null);

    String lineString =
        GeometryUtility.attributeToLineString(
            attribute, geometryOperator, new GeometryOperator.Context());

    verify(geometryOperator).apply(any(), any());

    assertThat(lineString, is(nullValue()));
  }

  @Test
  public void testAttributeToLineString() {

    Attribute attribute =
        new AttributeImpl(FIELD, Arrays.asList("POINT ( 0 0 )", "POINT ( 10 10 )"));

    String lineString =
        GeometryUtility.attributeToLineString(
            attribute, GeometryOperator.IDENTITY, new GeometryOperator.Context());

    assertThat(lineString, is("LINESTRING (0 0, 10 10)"));
  }

  @Test
  public void testAttributeToLineStringWithSomeBadData() {

    Attribute attribute = new AttributeImpl(FIELD, Arrays.asList("POINT ( 0 0 )", "POINT ( xxx )"));

    String lineString =
        GeometryUtility.attributeToLineString(
            attribute, GeometryOperator.IDENTITY, new GeometryOperator.Context());

    assertThat(lineString, is("POINT (0 0)"));
  }

  @Test
  public void testAttributeToLineStringWithBadData() {

    Attribute attribute = new AttributeImpl(FIELD, Arrays.asList("POINT ( yyy )", "POINT ( xxx )"));

    String lineString =
        GeometryUtility.attributeToLineString(
            attribute, GeometryOperator.IDENTITY, new GeometryOperator.Context());

    assertThat(lineString, is((String) null));
  }

  @Test
  public void testAttributeToLineStringWithEmptyData() {

    Attribute attribute = new AttributeImpl(FIELD, Collections.emptyList());

    String lineString =
        GeometryUtility.attributeToLineString(
            attribute, GeometryOperator.IDENTITY, new GeometryOperator.Context());

    assertThat(lineString, is((String) null));
  }
}
