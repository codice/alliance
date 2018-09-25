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
package org.codice.alliance.nsili.common;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import ddf.catalog.filter.proxy.builder.GeotoolsFilterBuilder;
import org.junit.Test;
import org.opengis.filter.Filter;

public class BqsConverterTest {

  private static final String BASIC_BQS_UUID_QUERY = "(NSIL_COMMON.identifierUUID like 'Test')";

  private static final String BASIC_BQS_OR_QUERY =
      "NSIL_COMMON.identifierUUID like 'Test' or NSIL_COMMON.targetNumber like 'Test'";

  private static final String BASIC_BQS_AND_QUERY =
      "(NSIL_COMMON.identifierUUID like 'Test' or NSIL_COMMON.targetNumber like 'Test') and NSIL_CARD.dateTimeModified >= '2016/03/14 06:58:31' and x > 15.3";

  private static final String BASIC_BQS_AND_QUERY_WITH_PAREN =
      "(NSIL_COMMON.identifierUUID like 'Test' or NSIL_COMMON.targetNumber like 'Test') and (NSIL_CARD.dateTimeModified >= '2016/03/14 06:58:31') and (x > -15.3)";

  private static final String BASIC_BQS_PAREN_TEST2 =
      "(NSIL_COMMON.identifierUUID like 'Test' or NSIL_COMMON.targetNumber like 'Test') and (NSIL_CARD.dateTimeModified >= '2016/03/14')";

  private static final String BASIC_BQS_GEO_POLY =
      "NSIL_COMMON.identifierMission like 'Test' and (NSIL_COVERAGE.spatialGeographicReferenceBox intersect POLYGON(46.155441760892586,81.76504326406543,48.16459468926409,161.75538233465647,2.8040686823204646,146.30988701631455,-0.4877657735999418,92.31643605259531,46.155441760892586,81.76504326406543))";

  private static final String BASIC_BQS_GEO_RECT =
      "NSIL_COMMON.identifierMission like 'Test' and (NSIL_COVERAGE.spatialGeographicReferenceBox intersect RECTANGLE(46.155441760892586,81.76504326406543,-0.4877657735999418,92.31643605259531))";

  private static final String BASIC_BQS_GEO_LINE =
      "NSIL_COMMON.identifierMission like 'Test' and (NSIL_COVERAGE.spatialGeographicReferenceBox intersect LINE(46.155441760892586,81.76504326406543,48.16459468926409,161.75538233465647,2.8040686823204646,146.30988701631455,-0.4877657735999418,92.31643605259531,46.155441760892586,81.76504326406543))";

  private static final String BASIC_BQS_GEO_POINT =
      "NSIL_COMMON.identifierMission like 'Test' and (NSIL_COVERAGE.spatialGeographicReferenceBox intersect POINT(46.155441760892586,81.76504326406543))";

  private static final String BASIC_BQS_GEO_CIRCLE =
      "NSIL_COMMON.identifierMission like 'Test' and (NSIL_COVERAGE.spatialGeographicReferenceBox intersect CIRCLE(46.155441760892586,81.76504326406543,25.6 meters))";

  private static final String BASIC_BQS_GEO_ELLIPSE =
      "NSIL_COMMON.identifierMission like 'Test' and (NSIL_COVERAGE.spatialGeographicReferenceBox intersect ELLIPSE(46.155441760892586,81.76504326406543,122500.5 meters,65000.3 meters,33))";

  private static final String BASIC_BQS_GEO_POINT_DMS =
      "NSIL_COMMON.identifierMission like 'Test' and (NSIL_COVERAGE.spatialGeographicReferenceBox intersect POINT(81:45:33.2N,146:25:01.8W))";

  private static final String BASIC_BQS_GEO_WITHIN =
      "NSIL_COMMON.identifierMission like 'Test' and (NSIL_COVERAGE.spatialGeographicReferenceBox within 6000 meters of POINT(46.155441760892586,81.76504326406543))";

  private static final String BASIC_BQS_GEO_BEYOND =
      "NSIL_COMMON.identifierMission like 'Test' and (NSIL_COVERAGE.spatialGeographicReferenceBox beyond 6 statute miles of CIRCLE(46.155441760892586,81.76504326406543,25.6 meters))";

  private static final String BQS_TEST_MANY_FIELDS =
      "((NSIL_COMMON.identifierUUID like 'Test') or (NSIL_COMMON.targetNumber like 'Test') or (NSIL_COMMON.source like 'Test') or "
          + "(NSIL_COMMON.identifierMission like 'Test') or (NSIL_IMAGERY.category like 'Test') or "
          + "(NSIL_IMAGERY.decompressionTechnique like 'Test') or (NSIL_CARD.identifier like 'Test') or "
          + "(NSIL_MESSAGE.recipient like 'Test') or (NSIL_MESSAGE.messageBody like 'Test') or "
          + "(NSIL_MESSAGE.messageType like 'Test') or (NSIL_FILE.creator like 'Test') or "
          + "(NSIL_VIDEO.encodingScheme like 'Test')) and (NSIL_CARD.dateTimeModified >= '2016/03/14 06:58:31')";

  private static final String TEST_BQS_NON_REQ_FIELD =
      "NSIL_CARD.identifier like '%' AND (not NSIL_PRODUCT:NSIL_CARD.status = 'OBSOLETE')";

  private static final String TEST_BQS_SIMPLE_PAREN =
      "(NSIL_CARD.identifier like '%') or (NSIL_CARD.status = '%')";

  private static final String TEST_BQS_SIMPLE_PAREN2 =
      "(NSIL_CARD.identifier like '%' or NSIL_CARD.identifier like 'blah' and NSIL_CARD.identifier like 'two') and (NSIL_CARD.status = '%')";

  private static final String TEST_BQS_QUOTE_STR = "NSIL_CARD.identifier LIKE 'TEST_LAB_DDF_$_X'";

  private static final String TEST_BQS_MASSIVE =
      "((NSIL_CARD.identifier like '%') or (NSIL_CARD.status = '%') or (NSIL_CARD.publisher like '%') "
          + "or (NSIL_CARD.sourceLibrary like '%') or (NSIL_COMMON.descriptionAbstract like '%') "
          + "or (NSIL_COMMON.identifierMission like '%') or (NSIL_COMMON.identifierUUID like '%') "
          + "or (NSIL_COMMON.language like '%') or (NSIL_COMMON.source like '%') "
          + "or (NSIL_COMMON.subjectCategoryTarget like '%') or (NSIL_COMMON.targetNumber like '%') "
          + "or (NSIL_COMMON.type = '%') or (NSIL_COVERAGE.spatialCountryCode like '%') "
          + "or (NSIL_FILE.archiveInformation like '%') or (NSIL_FILE.creator like '%') "
          + "or (NSIL_FILE.format like '%') or (NSIL_FILE.formatVersion like '%') "
          + "or (NSIL_FILE.productURL like '%') or (NSIL_FILE.title like '%') "
          + "or (NSIL_IMAGERY.category = '%') or (NSIL_IMAGERY.comments like '%') "
          + "or (NSIL_IMAGERY.decompressionTechnique = '%') "
          + "or (NSIL_IMAGERY.identifier like '%') or (NSIL_IMAGERY.title like '%') "
          + "or (NSIL_MESSAGE.recipient like '%') or (NSIL_MESSAGE.subject like '%') "
          + "or (NSIL_MESSAGE.messageBody like '%') or (NSIL_MESSAGE.messageType = '%') "
          + "or (NSIL_METADATASECURITY.classification = '%') "
          + "or (NSIL_METADATASECURITY.policy like '%') "
          + "or (NSIL_METADATASECURITY.releasability like '%') "
          + "or (NSIL_PART.partIdentifier like '%') or (NSIL_RELATED_FILE.creator like '%') "
          + "or (NSIL_RELATED_FILE.fileType like '%') or (NSIL_RELATED_FILE.URL like '%') "
          + "or (NSIL_RELATION.amplification like '%') or (NSIL_RELATION.contributor like '%') "
          + "or (NSIL_RELATION.description like '%') or (NSIL_RELATION.relationship = '%') "
          + "or (NSIL_SECURITY.classification = '%') or (NSIL_SECURITY.policy like '%') "
          + "or (NSIL_SECURITY.releasability like '%') or (NSIL_STREAM.archiveInformation like '%') "
          + "or (NSIL_STREAM.creator like '%') or (NSIL_STREAM.standard = '%') "
          + "or (NSIL_STREAM.standardVersion like '%') or (NSIL_STREAM.sourceURL like '%') "
          + "or (NSIL_VIDEO.category = '%') or (NSIL_VIDEO.encodingScheme = '%') "
          + "or (NSIL_VIDEO.metadataEncodingScheme = '%') or (NSIL_VIDEO.scanningMode = '%') "
          + "or (NSIL_APPROVAL.approvedBy like '%') or (NSIL_APPROVAL.status = '%') "
          + "or (EXPLOITATION_INFO.description like '%') or (EXPLOITATION_INFO.subjectiveQualityCode = '%') "
          + "or (NSIL_SDS.operationalStatus = '%') or (NSIL_TDL.messageNumber = '%') "
          + "or (NSIL_TDL.trackNumber like '%') or (NSIL_RFI.forAction like '%') "
          + "or (NSIL_RFI.forInformation like '%') or (NSIL_RFI.serialNumber like '%') "
          + "or (NSIL_RFI.status = '%') or (NSIL_RFI.workflowStatus = '%') or (NSIL_CXP.status = '%') "
          + "or (NSIL_REPORT.originatorsRequestSerialNumber like '%') or (NSIL_REPORT.priority = '%') "
          + "or (NSIL_REPORT.type = '%') or (NSIL_TASK.comments like '%') or (NSIL_TASK.status = '%')) "
          + "and (not NSIL_PRODUCT:NSIL_CARD.status = 'OBSOLETE')";

  private static final String BQS_SOURCE_LIBRARY =
      "(NSIL_CARD.sourceLibrary like '%') or (NSIL_CARD.status = '%')";

  private static final String BQS_AND_SOURCE_LIBRARY =
      "(NSIL_CARD.sourceLibrary LIKE 'TEST LAB DDF Space' OR"
          + " NSIL_CARD.sourceLibrary LIKE 'TEST_LAB_DDF_2_9') AND"
          + " (NSIL_CARD.dateTimeModified > '1970/01/01 00:00:00') AND"
          + " (NSIL_CARD.sourceLibrary <> 'TEST-REMOTE')";

  private static final String BQS_ONLY_SOURCE_LIBRARY =
      "NSIL_CARD.sourceLibrary LIKE 'TEST LAB DDF Space'";

  private static final String BQS_ONLY_SOURCE_TERMS =
      "((NSIL_CARD.sourceLibrary LIKE 'TEST LAB DDF Space')) OR"
          + " (NSIL_CARD.sourceLibrary <> 'TEST-REMOTE')";

  private BqsConverter bqsConverter = new BqsConverter(new GeotoolsFilterBuilder(), true);

  @Test
  public void testBasicBqsUUID() {
    Filter filter = bqsConverter.convertBQSToDDF(BASIC_BQS_UUID_QUERY);
    assertThat(filter, notNullValue());
    assertThat(filter.toString(), is("[ id is like Test ]"));
  }

  @Test
  public void testBqsOr() {
    Filter filter = bqsConverter.convertBQSToDDF(BASIC_BQS_OR_QUERY);
    assertThat(filter, notNullValue());
    assertThat(filter.toString(), is("[[ id is like Test ] OR [ targetNumber is like Test ]]"));
  }

  @Test
  public void testBqsAnd() {
    Filter filter = bqsConverter.convertBQSToDDF(BASIC_BQS_AND_QUERY);
    assertThat(filter, notNullValue());
    assertThat(
        filter.toString(), containsString("[[ id is like Test ] OR [ targetNumber is like Test ]"));
  }

  @Test
  public void testBqsAndParensQuery() {
    Filter filter = bqsConverter.convertBQSToDDF(BASIC_BQS_AND_QUERY_WITH_PAREN);
    assertThat(filter, notNullValue());
    assertThat(
        filter.toString(),
        containsString("[[ id is like Test ] OR [ targetNumber is like Test ]]"));
  }

  @Test
  public void testBqsParen2() {
    Filter filter = bqsConverter.convertBQSToDDF(BASIC_BQS_PAREN_TEST2);
    assertThat(filter, notNullValue());
    assertThat(
        filter.toString(),
        containsString("AND [[ id is like Test ] OR [ targetNumber is like Test ]]"));
  }

  @Test
  public void testGeoPoly() {
    Filter filter = bqsConverter.convertBQSToDDF(BASIC_BQS_GEO_POLY);
    assertThat(filter, notNullValue());
    assertThat(
        filter.toString(),
        containsString("[ identifierMission is like Test ] AND [ location intersects"));
  }

  @Test
  public void testGeoRect() {
    Filter filter = bqsConverter.convertBQSToDDF(BASIC_BQS_GEO_RECT);
    assertThat(filter, notNullValue());
    assertThat(
        filter.toString(),
        containsString("[ identifierMission is like Test ] AND [ location intersects"));
  }

  @Test
  public void testGeoLine() {
    Filter filter = bqsConverter.convertBQSToDDF(BASIC_BQS_GEO_LINE);
    assertThat(filter, notNullValue());
    assertThat(
        filter.toString(),
        containsString("[ identifierMission is like Test ] AND [ location intersects"));
  }

  @Test
  public void testGeoPoint() {
    Filter filter = bqsConverter.convertBQSToDDF(BASIC_BQS_GEO_POINT);
    assertThat(filter, notNullValue());
    assertThat(
        filter.toString(),
        containsString("[ identifierMission is like Test ] AND [ location intersects"));
  }

  @Test
  public void testGeoCircle() {
    Filter filter = bqsConverter.convertBQSToDDF(BASIC_BQS_GEO_CIRCLE);
    assertThat(filter, notNullValue());
    assertThat(
        filter.toString(),
        containsString("[ identifierMission is like Test ] AND [ location dwithin"));
  }

  @Test
  public void testGeoEllipse() {
    Filter filter = bqsConverter.convertBQSToDDF(BASIC_BQS_GEO_ELLIPSE);
    assertThat(filter, notNullValue());
    assertThat(
        filter.toString(),
        containsString("[ identifierMission is like Test ] AND [ location intersects"));
  }

  @Test
  public void testGeoPointDMS() {
    Filter filter = bqsConverter.convertBQSToDDF(BASIC_BQS_GEO_POINT_DMS);
    assertThat(filter, notNullValue());
    assertThat(
        filter.toString(),
        containsString("[ identifierMission is like Test ] AND [ location intersects"));
  }

  @Test
  public void testGeoWithin() {
    Filter filter = bqsConverter.convertBQSToDDF(BASIC_BQS_GEO_WITHIN);
    assertThat(filter, notNullValue());
    assertThat(
        filter.toString(),
        containsString("[ identifierMission is like Test ] AND [ location dwithin"));
  }

  @Test
  public void testGeoBeyond() {
    Filter filter = bqsConverter.convertBQSToDDF(BASIC_BQS_GEO_BEYOND);
    assertThat(filter, notNullValue());
    assertThat(
        filter.toString(),
        containsString("[ identifierMission is like Test ] AND [ NOT [ location dwithin"));
  }

  @Test
  public void testBqsManyFields() {
    Filter filter = bqsConverter.convertBQSToDDF(BQS_TEST_MANY_FIELDS);
    assertThat(filter, notNullValue());
    assertThat(
        filter.toString(),
        containsString(
            "AND [[ id is like Test ] OR [ targetNumber is like Test ] OR [ NSILISource is like Test ] OR [ identifierMission is like Test ] OR [ imageryCategory is like Test ] OR [ decompressionTechnique is like Test ] OR [ id is like Test ] OR [ recipient is like Test ] OR [ description is like Test ] OR [ messageType is like Test ] OR [ point-of-contact is like Test ] OR [ encodingScheme is like Test ]"));
  }

  @Test
  public void testBqsNonReqField() {
    Filter filter = bqsConverter.convertBQSToDDF(TEST_BQS_NON_REQ_FIELD);
    assertThat(filter, notNullValue());
    assertThat(filter.toString(), is("[[ id is like * ] AND [ NOT [ status is like OBSOLETE ] ]]"));
  }

  @Test
  public void testBqsSimpleParen() {
    Filter filter = bqsConverter.convertBQSToDDF(TEST_BQS_SIMPLE_PAREN);

    assertThat(filter, notNullValue());
    assertThat(filter.toString(), is("[[ id is like * ] OR [ status is like * ]]"));
  }

  @Test
  public void testBqsSimpleParen2() {
    Filter filter = bqsConverter.convertBQSToDDF(TEST_BQS_SIMPLE_PAREN2);

    assertThat(filter, notNullValue());
    assertThat(
        filter.toString(),
        is(
            "[[ status is like * ] AND [[ id is like * ] OR [[ id is like blah ] AND [ id is like two ]]]]"));
  }

  @Test
  public void testBqsMassive() {
    Filter filter = bqsConverter.convertBQSToDDF(TEST_BQS_MASSIVE);

    assertThat(filter, notNullValue());
    assertThat(filter.toString(), containsString("[ NOT [ status is like OBSOLETE ] ]"));
  }

  @Test
  public void testQuotes() {
    Filter filter = bqsConverter.convertBQSToDDF(TEST_BQS_QUOTE_STR);

    assertThat(filter, notNullValue());
    assertThat(filter.toString(), is("[ id is like TEST_LAB_DDF_$_X ]"));
  }

  @Test
  public void testNoSourceLibrary() {
    Filter filter = bqsConverter.convertBQSToDDF(BQS_SOURCE_LIBRARY);

    assertThat(filter, notNullValue());
    assertThat(filter.toString(), is("[[ status is like * ]]"));
  }

  @Test
  public void testSourceLibrary() {
    BqsConverter bqsConverter = new BqsConverter(new GeotoolsFilterBuilder(), false);
    Filter filter = bqsConverter.convertBQSToDDF(BQS_SOURCE_LIBRARY);

    assertThat(filter, notNullValue());
    assertThat(filter.toString(), is("[[ sourceId is like * ] OR [ status is like * ]]"));
  }

  @Test
  public void testSourceLibraryAnd() {
    Filter filter = bqsConverter.convertBQSToDDF(BQS_AND_SOURCE_LIBRARY);

    assertThat(filter, notNullValue());
    assertThat(filter.toString(), not(containsString("AND")));
  }

  @Test
  public void testSourceLibraryAndInclude() {
    BqsConverter bqsConverter = new BqsConverter(new GeotoolsFilterBuilder(), false);
    Filter filter = bqsConverter.convertBQSToDDF(BQS_AND_SOURCE_LIBRARY);

    assertThat(filter, notNullValue());
    assertThat(
        filter.toString(),
        containsString(
            "AND [ NOT [ sourceId is like TEST-REMOTE ] ] AND [[ sourceId is like TEST LAB DDF Space ] OR [ sourceId is like TEST_LAB_DDF_2_9 ]]"));
  }

  @Test
  public void testMinimalReturnFilter() {
    BqsConverter bqsConverter = new BqsConverter(new GeotoolsFilterBuilder(), true);
    Filter filter = bqsConverter.convertBQSToDDF(BQS_ONLY_SOURCE_LIBRARY);

    assertThat(filter, notNullValue());
    assertThat(filter.toString(), containsString("anyText = *"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNoFilterBuilder() {
    new BqsConverter(null, false);
  }

  @Test
  public void testIsFilterValid() {
    BqsConverter bqsConverter = new BqsConverter(new GeotoolsFilterBuilder(), true);
    Filter filter = bqsConverter.convertBQSToDDF(BQS_ONLY_SOURCE_TERMS);
    assertThat(filter, notNullValue());
    assertThat(filter.toString(), containsString("anyText = *"));
  }
}
