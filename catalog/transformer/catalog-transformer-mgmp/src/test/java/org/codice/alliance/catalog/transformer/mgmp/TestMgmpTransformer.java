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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.codice.alliance.catalog.core.api.impl.types.IsrAttributes;
import org.codice.alliance.catalog.core.api.impl.types.SecurityAttributes;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.catalog.core.api.types.Security;
import org.codice.ddf.spatial.ogc.csw.catalog.common.GmdConstants;
import org.junit.Before;
import org.junit.Test;

import ddf.catalog.data.AttributeRegistry;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.MetacardTypeImpl;
import ddf.catalog.data.impl.types.AssociationsAttributes;
import ddf.catalog.data.impl.types.ContactAttributes;
import ddf.catalog.data.impl.types.LocationAttributes;
import ddf.catalog.data.impl.types.MediaAttributes;
import ddf.catalog.data.impl.types.TopicAttributes;
import ddf.catalog.data.impl.types.ValidationAttributes;
import ddf.catalog.data.types.Associations;
import ddf.catalog.data.types.Contact;
import ddf.catalog.data.types.Core;
import ddf.catalog.data.types.DateTime;
import ddf.catalog.data.types.Location;
import ddf.catalog.data.types.Media;
import ddf.catalog.data.types.Topic;

public class TestMgmpTransformer {

    private static final List<Serializable> CATEGORY_LIST = Arrays.asList(
            "imageryBaseMapsEarthCover",
            "intelligenceMilitary");

    private static final List<String> KEYWORD_LIST = Arrays.asList("Mapping and navigation",
            "Hydrography",
            "Standard Series",
            "Raster",
            "Georeferenced");

    private static final List<Serializable> COUNTRIES_LIST = Arrays.asList("GBR", "FRA");

    private static final List<Serializable> SECURITY_LIST = Arrays.asList("NATO", "AUS");

    private static final String METACARD_LOCATION =
            "POLYGON ((57.12890625 52.86581372042818, 57.12890625 60.37857530322721, 77.51953125 60.37857530322721, 77.51953125 52.86581372042818, 57.12890625 52.86581372042818))";

    private static final String METACARD_BPOLYGON_LOCATION =
            "POLYGON ((15.825012 52.189365, 15.81318 52.189365, 15.81318 52.1929, 15.81318 52.193029, 15.81318 52.193979, 15.81318 52.198309, 15.81318 52.198961, 15.812659 52.198961, 15.812659 52.200576, 15.81213 52.200576, 15.81213 52.201856, 15.811346 52.201856, 15.811346 52.203864, 15.814785 52.203864, 15.817481 52.203864, 15.825012 52.203864, 15.825012 52.196916, 15.825012 52.189365))";

    private static final String METACARD_DESCRIPTION =
            "A large scale plan of an airfield outlining information related to that specific airfield.";

    private static final String SERVICE = "service";

    private static final String ISR_CATEGORY_1 = "physicalMeasurement";

    private static final String ISR_CATEGORY_2 = "image";

    private static final String ISR_CATEGORY_3 = "video";

    private static final String ISR_COMMENTS_1 = "height above reference surface in something else";

    private static final String ISR_COMMENTS_2 =
            "optical reflective surface mgmp image description";

    private static final String ISR_COMMENTS_3 = "monochromatic value of pixel";

    private static final String ISR_COMMENTS_4 = "horizontal video, reflective surface";

    private static final String ORGANISATION_NAME = "Codice Foundation";

    private static final String METACARD_ID = "a metacard";

    private static final MetacardType MGMP_METACARD_TYPE = getMgmpMetacardType();

    private static final Double ALTITUDE_1 = 305.0;

    private static final Double ALTITUDE_2 = 312.0;

    private static final String OFFICIAL_SENSITIVE = "officialSensitive";

    private static final String NATO_SECRET = "NATOSecret";

    private static final String OFFICIAL = "official";

    private static final String UNCLASSIFIED = "unclassified";

    private AttributeRegistry mockAttributeRegistry;

    @Before
    public void setUp() {
        mockAttributeRegistry = mock(AttributeRegistry.class);
    }

    @Test
    public void testInputTransformerWithFullMgmpMetacard() throws Exception {
        InputStream fileInput =
                TestMgmpTransformer.class.getResourceAsStream("/full_data_mgmp.xml");
        MgmpTransformer mgmpTransformer = new MgmpTransformer(MGMP_METACARD_TYPE,
                mockAttributeRegistry);
        Metacard metacard = mgmpTransformer.transform(fileInput);

        assertThat(metacard.getTitle(), is("Surrey TLM"));
        assertThat(metacard.getId(), is("4d40336f-70c2-488c-835a-1cff24b90620"));
        assertThat(convertDate(metacard.getModifiedDate()), is("2014-10-31 12:00:00 UTC"));
        assertThat(convertDate(metacard.getExpirationDate()), is("2014-12-11 00:00:00 UTC"));
        assertThat(convertDate(metacard.getCreatedDate()), is("2000-12-01 00:00:00 UTC"));
        assertThat(convertDate(metacard.getEffectiveDate()), is("1998-01-01 07:00:00 UTC"));
        Date createdDate = (Date) metacard.getAttribute(Core.METACARD_CREATED)
                .getValue();
        Date modifiedDate = (Date) metacard.getAttribute(Core.METACARD_MODIFIED)
                .getValue();
        assertThat(convertDate(createdDate), is("2015-07-24 00:00:00 UTC"));
        assertThat(convertDate(modifiedDate), is("2015-07-24 00:00:00 UTC"));

        /*  MGMP LanguageCode is lowercase only */
        assertThat(metacard.getAttribute(Core.LANGUAGE)
                .getValues(), hasItems("ger", "eng"));

        assertThat(metacard.getContentTypeName(), is(SERVICE));

        assertThat(metacard.getAttribute(Core.DATATYPE)
                .getValue(), is(SERVICE));

        List<Serializable> countryCodes = metacard.getAttribute(Location.COUNTRY_CODE)
                .getValues();
        assertThat(countryCodes, hasItems(COUNTRIES_LIST.get(0), COUNTRIES_LIST.get(1)));

        assertThat(metacard.getLocation(), is(METACARD_LOCATION));

        assertThat(metacard.getAttribute(Core.DESCRIPTION)
                .getValue(), is(METACARD_DESCRIPTION));

        assertThat(metacard.getAttribute(Isr.CATEGORY)
                .getValues(), hasItems(ISR_CATEGORY_1, ISR_CATEGORY_2, ISR_CATEGORY_3));

        assertThat(metacard.getAttribute(Isr.COMMENTS)
                .getValues(), hasItems(ISR_COMMENTS_1,
                ISR_COMMENTS_2,
                ISR_COMMENTS_3,
                ISR_COMMENTS_4));

        List<Serializable> keywords = metacard.getAttribute(Topic.KEYWORD)
                .getValues();
        assertThat(keywords, hasItems(KEYWORD_LIST.get(0),
                KEYWORD_LIST.get(1),
                KEYWORD_LIST.get(2),
                KEYWORD_LIST.get(3),
                KEYWORD_LIST.get(4)));

        assertThat(metacard.getAttribute(Location.ALTITUDE)
                .getValues(), hasItems(ALTITUDE_1, ALTITUDE_2));

        assertThat(metacard.getAttribute(Contact.POINT_OF_CONTACT_NAME)
                .getValues(), hasItem(ORGANISATION_NAME));

        assertThat(metacard.getAttribute(Contact.POINT_OF_CONTACT_PHONE)
                .getValues(), hasItem("(+44) 213 332313 (Ext 4883)"));

        assertThat(metacard.getAttribute(Contact.POINT_OF_CONTACT_EMAIL)
                .getValues(), hasItem("queen.elizabeth.two@gov.uk"));

        assertThat(metacard.getAttribute(Contact.POINT_OF_CONTACT_ADDRESS)
                .getValues(),
                hasItem("Buckingham Palace London Westminster SW1A 1AA United Kingdom"));

        URI uri = new URI("https://www.example.com");
        assertThat(metacard.getAttribute(Core.RESOURCE_URI)
                .getValue(), is(uri.toString()));

        List<Serializable> temporals = metacard.getAttribute(DateTime.START)
                .getValues();
        temporals.addAll(metacard.getAttribute(DateTime.END)
                .getValues());
        List<String> dates = new ArrayList<>();
        for (Serializable serializable : temporals) {
            Date temporal = (Date) serializable;
            dates.add(convertDate(temporal));
        }

        assertThat(dates, hasItems("2000-01-01 00:00:00 UTC",
                "2015-11-07 11:00:00 UTC",
                "2007-05-07 00:00:00 UTC",
                "2015-11-08 14:00:00 UTC"));

        assertThat(metacard.getAttribute(Contact.PUBLISHER_NAME)
                .getValues(), hasItem(ORGANISATION_NAME));

        assertThat(metacard.getAttribute(Contact.PUBLISHER_PHONE)
                .getValues(), hasItem("+44 (0)321 321 1122"));

        assertThat(metacard.getAttribute(Contact.PUBLISHER_EMAIL)
                .getValues(), hasItem("theresa.may@gov.uk"));

        assertThat(metacard.getAttribute(Contact.PUBLISHER_ADDRESS)
                        .getValues(), hasItem(
                        "10 Downing Street London Westminster SW1A 2AA United Kingdom"));

        assertThat(metacard.getAttribute(Media.FORMAT)
                .getValue(), is("s57"));

        assertThat(metacard.getAttribute(Media.FORMAT_VERSION)
                .getValue(), is("3.1"));

        List<Serializable> crsCodes =
                metacard.getAttribute(Location.COORDINATE_REFERENCE_SYSTEM_CODE)
                        .getValues();
        assertThat(crsCodes, hasItems("EPSG:4326",
                "EPSG:27700",
                "EPSG:1088",
                "EPSG:6277",
                "EPSG:9807",
                "EPSG:5612",
                "UK-DIGEST:OGB",
                "UK-DIGEST:MSL",
                "UK-DIGEST:AA",
                "UK-DIGEST:TC",
                "UK-DIGEST:ND"));

        assertThat(metacard.getAttribute(GmdConstants.RESOURCE_STATUS)
                .getValues(), hasItems("planned", "completed"));

        assertThat(metacard.getAttribute(Isr.NATIONAL_IMAGERY_INTERPRETABILITY_RATING_SCALE)
                .getValue(), is(2.0));

        assertThat(metacard.getAttribute(Security.METADATA_CLASSIFICATION)
                .getValue(), is(OFFICIAL));

        assertThat(metacard.getAttribute(Security.METADATA_RELEASABILITY)
                .getValues(), hasItems(SECURITY_LIST.get(0), SECURITY_LIST.get(1)));

        assertThat(metacard.getAttribute(Security.METADATA_DISSEMINATION)
                .getValues(), hasItems(MgmpConstants.RELEASABLE_TO));

        assertThat(metacard.getAttribute(Security.METADATA_ORIGINATOR_CLASSIFICATION)
                .getValue(), is(UNCLASSIFIED));

        assertThat(metacard.getAttribute(Security.RESOURCE_CLASSIFICATION)
                .getValue(), is(OFFICIAL_SENSITIVE));

        assertThat(metacard.getAttribute(Security.RESOURCE_RELEASABILITY)
                .getValues(), hasItems(SECURITY_LIST.get(0), SECURITY_LIST.get(1)));
        assertThat(metacard.getAttribute(Security.RESOURCE_DISSEMINATION)
                .getValues(), hasItems(MgmpConstants.RELEASABLE_TO));

        assertThat(metacard.getAttribute(Security.RESOURCE_ORIGINATOR_CLASSIFICATION)
                .getValue(), is(NATO_SECRET));

        assertThat(metacard.getAttribute(Isr.CLOUD_COVER)
                .getValue(), is(13.0));

        assertThat(metacard.getAttribute(Associations.RELATED)
                .getValues(), hasItem("00000000-0000-0000-0000-000000000077"));

        List<Serializable> categories = metacard.getAttribute(Topic.CATEGORY)
                .getValues();
        assertThat(categories, hasItems(CATEGORY_LIST.get(0), CATEGORY_LIST.get(1)));

        assertThat(metacard.getAttribute(Isr.DATA_QUALITY)
                .getValues(), hasItems("Resource Grade : Something statement",
                "relative vertical error : Authoritative",
                "linear map accuracy at 90 % signifcance level : 159"));
    }

    @Test
    public void testInputTransformerWithEmptyMgmpMetacard() throws Exception {
        InputStream fileInput = TestMgmpTransformer.class.getResourceAsStream("/empty_mgmp.xml");
        MgmpTransformer mgmpTransformer = new MgmpTransformer(MGMP_METACARD_TYPE,
                mockAttributeRegistry);
        Metacard metacard = mgmpTransformer.transform(fileInput);
        assertThat(metacard, notNullValue());
    }

    @Test
    public void testInputTransformerWithSampleMgmpMetacard() throws Exception {
        InputStream fileInput = TestMgmpTransformer.class.getResourceAsStream("/sample_mgmp_1.xml");
        MgmpTransformer mgmpTransformer = new MgmpTransformer(MGMP_METACARD_TYPE,
                mockAttributeRegistry);
        Metacard metacard = mgmpTransformer.transform(fileInput, METACARD_ID);

        assertThat(metacard.getAttribute(Security.RESOURCE_CLASSIFICATION)
                .getValue(), is(OFFICIAL_SENSITIVE));
        assertThat(metacard.getAttribute(Security.RESOURCE_RELEASABILITY)
                .getValues(), hasItems("UK"));
        assertThat(metacard.getAttribute(Security.RESOURCE_DISSEMINATION)
                .getValues(), hasItems(MgmpConstants.EYES_DISCRETION));
        assertThat(metacard.getAttribute(Security.RESOURCE_ORIGINATOR_CLASSIFICATION)
                .getValue(), is(NATO_SECRET));

        assertThat(metacard.getAttribute(Security.METADATA_CLASSIFICATION)
                .getValue(), is(OFFICIAL));
        assertThat(metacard.getAttribute(Security.METADATA_RELEASABILITY)
                .getValues(), hasItems(SECURITY_LIST.get(0), SECURITY_LIST.get(1)));
        assertThat(metacard.getAttribute(Security.METADATA_DISSEMINATION)
                .getValues(), hasItems(MgmpConstants.EYES_ONLY));
        assertThat(metacard.getAttribute(Security.METADATA_ORIGINATOR_CLASSIFICATION)
                .getValue(), is(UNCLASSIFIED));

        assertThat(metacard.getAttribute(Core.LANGUAGE)
                .getValues(), is(MgmpConstants.DEFAULT_LANGUAGE));
        assertThat(metacard.getAttribute(Isr.NATIONAL_IMAGERY_INTERPRETABILITY_RATING_SCALE),
                nullValue());
        assertThat(metacard.getAttribute(Isr.CLOUD_COVER), nullValue());

        assertThat(metacard.getAttribute(Location.ALTITUDE), nullValue());
        Date createdDate = (Date) metacard.getAttribute(Core.METACARD_CREATED)
                .getValue();
        Date modifiedDate = (Date) metacard.getAttribute(Core.METACARD_MODIFIED)
                .getValue();
        assertThat(convertDate(createdDate), is("2015-11-04 10:00:00 UTC"));
        assertThat(convertDate(modifiedDate), is("2015-11-04 10:00:00 UTC"));
        assertThat(metacard.getLocation(), is(METACARD_BPOLYGON_LOCATION));

        assertThat(metacard.getAttribute(Core.RESOURCE_DOWNLOAD_URL), nullValue());
    }

    @Test
    public void testInputTransformerWithSampleMgmpMetacardEdgeCases() throws Exception {
        InputStream fileInput = TestMgmpTransformer.class.getResourceAsStream("/sample_mgmp_2.xml");
        MgmpTransformer mgmpTransformer = new MgmpTransformer(MGMP_METACARD_TYPE,
                mockAttributeRegistry);
        Metacard metacard = mgmpTransformer.transform(fileInput, METACARD_ID);

        assertThat(metacard.getAttribute(Security.RESOURCE_CLASSIFICATION)
                .getValue(), is(OFFICIAL_SENSITIVE));
        assertThat(metacard.getAttribute(Security.RESOURCE_RELEASABILITY), nullValue());
        assertThat(metacard.getAttribute(Security.RESOURCE_DISSEMINATION), nullValue());
        assertThat(metacard.getAttribute(Security.RESOURCE_ORIGINATOR_CLASSIFICATION)
                .getValue(), is(NATO_SECRET));

        assertThat(metacard.getAttribute(Isr.NATIONAL_IMAGERY_INTERPRETABILITY_RATING_SCALE),
                nullValue());
    }

    private static MetacardType getMgmpMetacardType() {
        return new MetacardTypeImpl("mgmpMetacardType", Arrays.asList(new ValidationAttributes(),
                new SecurityAttributes(),
                new ContactAttributes(),
                new LocationAttributes(),
                new MediaAttributes(),
                new IsrAttributes(),
                new TopicAttributes(),
                new AssociationsAttributes()));
    }

    private String convertDate(Date date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(date);
    }
}
