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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.types.Associations;
import ddf.catalog.data.types.Contact;
import ddf.catalog.data.types.Core;
import ddf.catalog.data.types.DateTime;
import ddf.catalog.data.types.Location;
import ddf.catalog.data.types.Media;
import ddf.catalog.data.types.Topic;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Date;
import org.codice.alliance.catalog.core.api.types.Security;
import org.codice.alliance.ddms.types.Ddms20MetacardType;
import org.codice.alliance.ddms.types.RestrictionAttributes;
import org.codice.ddf.transformer.xml.streaming.Gml3ToWkt;
import org.codice.ddf.transformer.xml.streaming.impl.Gml3ToWktImpl;
import org.junit.Before;
import org.junit.Test;

public class Ddms20InputTransformerTest {

  private final Gml3ToWkt gml3ToWkt = Gml3ToWktImpl.newGml3ToWkt();

  private Ddms20InputTransformer ddms20InputTransformer;

  @Before
  public void beforeTest() {
    ddms20InputTransformer = new Ddms20InputTransformer(gml3ToWkt);
  }

  private InputStream loadTestResource(String path) {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
  }

  @Test
  public void testTransformFullDdms() throws Exception {
    Metacard metacard =
        ddms20InputTransformer.transform(loadTestResource("ddms-full.xml"), "0123456789abcdef");

    // Core attributes
    assertThat(metacard.getAttribute(Core.ID).getValue(), is("0123456789abcdef"));
    assertThat(metacard.getAttribute(Core.TITLE).getValue(), is("DDMS Title"));
    assertThat(
        metacard.getLocation(),
        containsString("POLYGON ((1.0 4.0, 2.0 4.0, 2.0 3.0, 1.0 3.0, 1.0 4.0))"));
    assertThat(metacard.getLocation(), containsString("POLYGON ((0 0, 0 0, 0 0, 0 0))"));
    assertThat(metacard.getLocation(), containsString("POINT (0 0))"));
    assertThat(
        metacard.getAttribute(Core.RESOURCE_URI).getValue(),
        is("https://site.example/metadata/{id}"));
    assertThat(
        metacard.getAttribute(Core.RESOURCE_DOWNLOAD_URL).getValue(),
        is("https://site.example/resource/{id}"));
    assertThat(metacard.getAttribute(Core.RESOURCE_SIZE).getValue(), is("42"));
    assertThat(metacard.getAttribute(Core.DESCRIPTION).getValue(), is("DDMS Description"));
    assertThat(
        ((Date) metacard.getAttribute(Core.CREATED).getValue()).getTime(),
        is(ZonedDateTime.parse("2017-05-08T20:23:41.284-04:00").toInstant().toEpochMilli()));
    assertThat(metacard.getAttribute(Core.LANGUAGE).getValue(), is("eng"));
    assertThat(
        metacard.getAttribute(Core.DATATYPE).getValue(), is("http://purl.org/dc/dcmitype/Text"));

    // Associated attributes
    assertThat(metacard.getAttribute(Associations.RELATED).getValues(), hasItem("{related_id}"));
    assertThat(metacard.getAttribute(Associations.DERIVED).getValues(), hasItem("{derived_id}"));
    assertThat(
        metacard.getAttribute(Associations.EXTERNAL).getValues(),
        hasItem("https://anothersite.example/resource/{external_id}"));
    assertThat(
        metacard.getAttribute(Associations.EXTERNAL).getValues(),
        not(hasItem("https://site.example/resource/{id}")));

    // Datetime attributes
    assertThat(metacard.getAttribute(DateTime.NAME).getValue(), is("Current Time"));
    assertThat(
        ((Date) metacard.getAttribute(DateTime.START).getValue()).getTime(),
        is(ZonedDateTime.parse("2017-05-08T20:23:41.284-04:00").toInstant().toEpochMilli()));
    assertThat(
        ((Date) metacard.getAttribute(DateTime.END).getValue()).getTime(),
        is(ZonedDateTime.parse("2018-05-08T20:23:41.284-04:00").toInstant().toEpochMilli()));

    // Contact attributes
    assertThat(metacard.getAttribute(Contact.CREATOR_NAME).getValue(), is("Creator Service"));
    assertThat(
        metacard.getAttribute(Contact.CREATOR_EMAIL).getValue(), is("creatorservice@site.example"));
    assertThat(metacard.getAttribute(Contact.CREATOR_PHONE).getValue(), is("555-0000"));
    assertThat(
        metacard.getAttribute(Contact.PUBLISHER_NAME).getValues(),
        hasItems("Publisher Service", "Publisher Organization"));
    assertThat(
        metacard.getAttribute(Contact.PUBLISHER_EMAIL).getValues(),
        hasItems("publisherservice@site.example", "publisherorg@site.example"));
    assertThat(
        metacard.getAttribute(Contact.PUBLISHER_PHONE).getValues(),
        hasItems("(555) 555-1111", "555-555-2222"));
    assertThat(
        metacard.getAttribute(Contact.CONTRIBUTOR_NAME).getValue(), is("Contributor Organization"));
    assertThat(
        metacard.getAttribute(Contact.CONTRIBUTOR_EMAIL).getValue(),
        is("contributororg@site.example"));
    assertThat(metacard.getAttribute(Contact.CONTRIBUTOR_PHONE).getValue(), is("1-555-555-3333"));
    assertThat(metacard.getAttribute(Contact.POINT_OF_CONTACT_NAME).getValue(), is("John Doe"));
    assertThat(
        metacard.getAttribute(Contact.POINT_OF_CONTACT_EMAIL).getValue(),
        is("john.doe@site.example"));
    assertThat(
        metacard.getAttribute(Contact.POINT_OF_CONTACT_PHONE).getValue(), is("+1-555-555-4444"));

    // Location attributes
    assertThat(metacard.getAttribute(Location.COUNTRY_CODE).getValues(), hasItem("USA"));
    assertThat(
        metacard.getAttribute(Location.COORDINATE_REFERENCE_SYSTEM_NAME).getValues(),
        hasItems("EPSG:4326", "EPSG:4979"));

    // Media attributes
    assertThat(metacard.getAttribute(Media.FORMAT).getValue(), is("digital"));
    assertThat(metacard.getAttribute(Media.TYPE).getValue(), is("text/xml"));

    // Topic attributes
    assertThat(
        metacard.getAttribute(Topic.CATEGORY).getValues(),
        hasItems(
            "Subject Category Label 1",
            "Subject Category Label 2",
            "Type Value 1",
            "Type Value 2"));
    assertThat(metacard.getAttribute(Topic.KEYWORD).getValues(), hasItems("keyword1", "keyword2"));
    assertThat(
        metacard.getAttribute(Topic.VOCABULARY).getValues(),
        hasItems(
            "Subject Category Qualifier 1",
            "Subject Category Qualifier 2",
            "Type Qualifier 1",
            "Type Qualifier 2"));

    // Security attributes
    assertThat(metacard.getAttribute(Security.CLASSIFICATION).getValue(), is("U"));
    assertThat(metacard.getAttribute(Security.RELEASABILITY).getValue(), is("releasableTo"));
    assertThat(metacard.getAttribute(Security.OWNER_PRODUCER).getValue(), is("ownerProducer"));
    assertThat(
        metacard.getAttribute(Security.DISSEMINATION_CONTROLS).getValue(),
        is("disseminationControl"));
    assertThat(metacard.getAttribute(Security.CODEWORDS).getValue(), is("sciControl"));
    assertThat(
        metacard.getAttribute(Security.OTHER_DISSEMINATION_CONTROLS).getValue(),
        is("nonIcMarkings"));

    // Restriction attributes
    assertThat(metacard.getAttribute(RestrictionAttributes.COPYRIGHT_APPLIES).getValue(), is(true));
    assertThat(
        metacard.getAttribute(RestrictionAttributes.INTELLECTUAL_PROPERTY).getValue(), is(true));
    assertThat(metacard.getAttribute(RestrictionAttributes.PRIVACY_ACT).getValue(), is(true));

    // Ddms attributes
    assertThat(
        ((Date) metacard.getAttribute(Ddms20MetacardType.INFO_CUT_OFF).getValue()).getTime(),
        is(ZonedDateTime.parse("2019-08-13T10:34:11Z").toInstant().toEpochMilli()));
    assertThat(
        ((Date) metacard.getAttribute(Ddms20MetacardType.POSTED).getValue()).getTime(),
        is(ZonedDateTime.parse("2019-08-13T10:34:11Z").toInstant().toEpochMilli()));
    assertThat(metacard.getAttribute(Ddms20MetacardType.LOCATION_NAME).getValues(), hasItem("AOI"));
    assertThat(
        metacard.getAttribute(Ddms20MetacardType.LOCATION_REGION).getValue(), is("AOI Region"));
    assertThat(metacard.getAttribute(Ddms20MetacardType.BE_NUMBER).getValue(), is("beNumber"));
    assertThat(
        metacard.getAttribute(Ddms20MetacardType.SUBTITLE).getValues(),
        hasItems("DDMS Subtitle 1", "DDMS Subtitle 2"));
  }
}
