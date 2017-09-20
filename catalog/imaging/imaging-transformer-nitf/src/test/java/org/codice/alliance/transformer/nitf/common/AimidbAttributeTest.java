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
package org.codice.alliance.transformer.nitf.common;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.tre.Tre;
import org.junit.Before;
import org.junit.Test;

public class AimidbAttributeTest {

  private static final String ACQUISITION_DATE = "20160920131430";

  private static final String MISSION_NO = "UNKN";

  private static final String MISSION_IDENTIFICATION = "NOT AVAIL.";

  private static final String FLIGHT_NO = "01";

  private static final String OP_NUM = "100";

  private static final String CURRENT_SEGMENT = "AA";

  private static final String REPRO_NUM = "10";

  private static final String REPLAY = "000";

  private static final String START_TILE_COLUMN = "100";

  private static final String START_TILE_ROW = "10000";

  private static final String END_SEGMENT = "AA";

  private static final String END_TILE_COLUMN = "100";

  private static final String END_TILE_ROW = "10000";

  private static final String FIPS_COUNTRY_CODE = "US";

  private static final String LOCATION = "4559N23345W";

  private Tre tre;

  @Before
  public void setUp() {
    tre = mock(Tre.class);
  }

  @Test
  public void testAimidbAttriubte() {
    AimidbAttribute.getAttributes()
        .forEach(attribute -> assertThat(attribute.getShortName(), notNullValue()));
    AimidbAttribute.getAttributes()
        .forEach(attribute -> assertThat(attribute.getLongName(), notNullValue()));
  }

  @Test
  public void testReplay() throws NitfFormatException {
    when(tre.getFieldValue(AimidbAttribute.REPLAY_ATTRIBUTE.getShortName())).thenReturn(REPLAY);

    String operationNumber =
        (String) AimidbAttribute.REPLAY_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(operationNumber, is(REPLAY));
  }

  @Test
  public void testMissionIdentification() throws NitfFormatException {
    when(tre.getFieldValue(AimidbAttribute.MISSION_IDENTIFICATION_ATTRIBUTE.getShortName()))
        .thenReturn(MISSION_IDENTIFICATION);

    String missionIdentification =
        (String) AimidbAttribute.MISSION_IDENTIFICATION_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(missionIdentification, is(MISSION_IDENTIFICATION));
  }

  @Test
  public void testEndSegment() throws NitfFormatException {
    when(tre.getFieldValue(AimidbAttribute.END_SEGMENT_ATTRIBUTE.getShortName()))
        .thenReturn(END_SEGMENT);

    String endSegment =
        (String) AimidbAttribute.END_SEGMENT_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(endSegment, is(END_SEGMENT));
  }

  @Test
  public void testStartTileColumn() throws NitfFormatException {
    when(tre.getFieldValue(AimidbAttribute.START_TILE_COLUMN_ATTRIBUTE.getShortName()))
        .thenReturn(START_TILE_COLUMN);

    Integer startTileColumn =
        (Integer) AimidbAttribute.START_TILE_COLUMN_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(startTileColumn, is(Integer.parseInt(START_TILE_COLUMN)));
  }

  @Test
  public void testStartTileRow() throws NitfFormatException {
    when(tre.getFieldValue(AimidbAttribute.START_TILE_ROW_ATTRIBUTE.getShortName()))
        .thenReturn(START_TILE_ROW);

    Integer startTileRow =
        (Integer) AimidbAttribute.START_TILE_ROW_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(startTileRow, is(Integer.parseInt(START_TILE_ROW)));
  }

  @Test
  public void testEndTileColumn() throws NitfFormatException {
    when(tre.getFieldValue(AimidbAttribute.END_TILE_COLUMN_ATTRIBUTE.getShortName()))
        .thenReturn(END_TILE_COLUMN);

    Integer endTileColumn =
        (Integer) AimidbAttribute.END_TILE_COLUMN_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(endTileColumn, is(Integer.parseInt(END_TILE_COLUMN)));
  }

  @Test
  public void testEndTileRow() throws NitfFormatException {
    when(tre.getFieldValue(AimidbAttribute.END_TILE_ROW_ATTRIBUTE.getShortName()))
        .thenReturn(END_TILE_ROW);

    Integer endTileRow =
        (Integer) AimidbAttribute.END_TILE_ROW_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(endTileRow, is(Integer.parseInt(END_TILE_ROW)));
  }

  @Test
  public void testOperationNumber() throws NitfFormatException {
    when(tre.getFieldValue(AimidbAttribute.OPERATION_NUMBER_ATTRIBUTE.getShortName()))
        .thenReturn(OP_NUM);

    Integer operationNumber =
        (Integer) AimidbAttribute.OPERATION_NUMBER_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(operationNumber, is(Integer.parseInt(OP_NUM)));
  }

  @Test
  public void testCurrentSegment() throws NitfFormatException {
    when(tre.getFieldValue(AimidbAttribute.CURRENT_SEGMENT_ATTRIBUTE.getShortName()))
        .thenReturn(CURRENT_SEGMENT);

    String currentSegment =
        (String) AimidbAttribute.CURRENT_SEGMENT_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(currentSegment, is(CURRENT_SEGMENT));
  }

  @Test
  public void testReprocessNumber() throws NitfFormatException {
    when(tre.getFieldValue(AimidbAttribute.REPROCESS_NUMBER_ATTRIBUTE.getShortName()))
        .thenReturn(REPRO_NUM);

    Integer reprocessNumber =
        (Integer) AimidbAttribute.REPROCESS_NUMBER_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(reprocessNumber, is(Integer.parseInt(REPRO_NUM)));
  }

  @Test
  public void testCountryCode() throws NitfFormatException {
    when(tre.getFieldValue(AimidbAttribute.COUNTRY_CODE_ATTRIBUTE.getShortName()))
        .thenReturn(FIPS_COUNTRY_CODE);

    String countryCode =
        (String) AimidbAttribute.COUNTRY_CODE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(countryCode, is(FIPS_COUNTRY_CODE));
  }

  @Test
  public void testAcquisitionDate() throws NitfFormatException {
    when(tre.getFieldValue(AimidbAttribute.ACQUISITION_DATE_ATTRIBUTE.getShortName()))
        .thenReturn(ACQUISITION_DATE);

    String acquisitionDate =
        (String) AimidbAttribute.ACQUISITION_DATE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(acquisitionDate, is(ACQUISITION_DATE));
  }

  @Test
  public void testMissionNo() throws NitfFormatException {
    when(tre.getFieldValue(AimidbAttribute.MISSION_NUMBER_ATTRIBUTE.getShortName()))
        .thenReturn(MISSION_NO);

    String missionNumber =
        (String) AimidbAttribute.MISSION_NUMBER_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(missionNumber, is(MISSION_NO));
  }

  @Test
  public void testLocation() throws NitfFormatException {
    when(tre.getFieldValue(AimidbAttribute.LOCATION_ATTRIBUTE.getShortName())).thenReturn(LOCATION);

    String location = (String) AimidbAttribute.LOCATION_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(location, is(LOCATION));
  }

  @Test
  public void testFlightNumber() throws NitfFormatException {
    when(tre.getFieldValue(AimidbAttribute.FLIGHT_NUMBER_ATTRIBUTE.getShortName()))
        .thenReturn(FLIGHT_NO);

    String flightNumber =
        (String) AimidbAttribute.FLIGHT_NUMBER_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(flightNumber, is(FLIGHT_NO));
  }
}
