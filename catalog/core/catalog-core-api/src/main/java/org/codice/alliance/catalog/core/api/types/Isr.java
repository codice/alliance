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
package org.codice.alliance.catalog.core.api.types;

/**
 * <b> This code is experimental. While this interface is functional and tested, it may change or be
 * removed in a future version of the library. </b>
 */
public interface Isr {

  /** Attribute name for accessing the Cloud Cover percentage for this Metacard. */
  String CLOUD_COVER = "isr.cloud-cover";

  /**
   * Attribute name for accessing whether the imagery has been processed for VMTI for this Metacard.
   */
  String VIDEO_MOVING_TARGET_INDICATOR_PROCESSED = "isr.vmti-processed";

  /** Attribute name for accessing the ISR comment for this Metacard. */
  String COMMENTS = "isr.comments";

  /** Attribute name for accessing the dwell location for this Metacard. */
  String DWELL_LOCATION = "isr.dwell-location";

  /**
   * Attribute name for accessing whether the exploitation was automatically generated for this
   * Metacard.
   */
  String EXPLOTATION_AUTO_GENERATED = "isr.exploitation-auto-generated";

  /** Attribute name for accessing the exploitation level for this Metacard. */
  String EXPLOITATION_LEVEL = "isr.exploitation-level";

  /** Attribute name for accessing the exploitation subjective quality code for this Metacard. */
  String EXPLOITATION_SUBJECTIVE_QUALITY_CODE = "isr.exploitation-subjective-quality-code";

  /** Attribute name for accessing the MTI Job ID for this Metacard. */
  String MOVING_TARGET_INDICATOR_JOB_ID = "isr.mti-job-id";

  /** Attribute name for accessing the frequency in hertz for this Metacard. */
  String FREQUENCY_HERTZ = "isr.frequency-hertz";

  /** Attribute name for accessing the Image ID for this Metacard. */
  String IMAGE_ID = "isr.image-id";

  /**
   * Attribute name for accessing the JC3IEDM (Joint Consultation, Command and Control Information
   * Exchange Data Model) ID for this Metacard.
   */
  String JC3IEDM_ID = "isr.jc3iedm-id";

  /** Attribute name for accessing the Mission ID for this Metacard. */
  String MISSION_ID = "isr.mission-id";

  /** Attribute name for accessing the Nato Reporting Code for this Metacard. */
  String NATO_REPORTING_CODE = "isr.nato-reporting-code";

  /** Attribute name for accessing the NIIRS rating for this Metacard. */
  String NATIONAL_IMAGERY_INTERPRETABILITY_RATING_SCALE = "isr.niirs";

  /** Attribute name for accessing the organizational unit for this Metacard. */
  String ORGANIZATIONAL_UNIT = "isr.organizational-unit";

  /** Attribute name for accessing the original source for the metadata for this Metacard. */
  String ORIGINAL_SOURCE = "isr.original-source";

  /** Attribute name for accessing the platform ID for this Metacard. */
  String PLATFORM_ID = "isr.platform-id";

  /** Attribute name for accessing the platform name for this Metacard. */
  String PLATFORM_NAME = "isr.platform-name";

  /** Attribute name for accessing the report entity alias for this Metacard. */
  String REPORT_ENTITY_ALIAS = "isr.report-entity-alias";

  /** Attribute name for accessing the report entity name for this Metacard. */
  String REPORT_ENTITY_NAME = "isr.report-entity-name";

  /** Attribute name for accessing the report entity type for this Metacard. */
  String REPORT_ENTITY_TYPE = "isr.report-entity-type";

  /** Attribute name for accessing the report info rating for this Metacard. */
  String REPORT_INFO_RATING = "isr.report-info-rating";

  /** Attribute name for accessing the report situation type for this Metacard. */
  String REPORT_SITUATION_TYPE = "isr.report-situation-type";

  /** Attribute name for accessing the report serial number for this Metacard. */
  String REPORT_SERIAL_NUMBER = "isr.report-serial-number";

  /** Attribute name for accessing the report type for this Metacard. */
  String REPORT_TYPE = "isr.report-type";

  /** Attribute name for accessing the report priority for this Metacard. */
  String REPORT_PRIORITY = "isr.report-priority";

  /**
   * Attribute name for accessing the entity requesting action for the metadata of this Metacard.
   */
  String REQUEST_FOR_INFORMATION_FOR_ACTION = "isr.rfi-for-action";

  /**
   * Attribute name for accessing the entity requesting that has interest in the metadata of this
   * Metacard.
   */
  String REQUEST_FOR_INFORMATION_FOR_INFORMATION = "isr.rfi-for-information";

  /** Attribute name for accessing the RFI serial number for this Metacard. */
  String REQUEST_FOR_INFORMATION_SERIAL_NUMBER = "isr.rfi-serial-number";

  /** Attribute name for accessing the RFI status for this Metacard. */
  String REQUEST_FOR_INFORMATION_STATUS = "isr.rfi-status";

  /** Attribute name for accessing the RFI workflow status for this Metacard. */
  String REQUEST_FOR_INFORMATION_WORKFLOW_STATUS = "isr.rfi-workflow-status";

  /** Attribute name for accessing the sensor ID for this Metacard. */
  String SENSOR_ID = "isr.sensor-id";

  /** Attribute name for accessing the sensor type for this Metacard. */
  String SENSOR_TYPE = "isr.sensor-type";

  /** Attribute name for accessing the snow cover existence for this Metacard. <br> */
  String SNOW_COVER = "isr.snow-cover";

  /** Attribute name for accessing the minimum snow depth (centimeters) for this Metacard. <br> */
  String SNOW_DEPTH_MIN_CENTIMETERS = "isr.snow-depth-min-centimeters";

  /** Attribute name for accessing the maximum snow depth (centimeters) for this Metacard. <br> */
  String SNOW_DEPTH_MAX_CENTIMETERS = "isr.snow-depth-max-centimeters";

  /** Attribute name for accessing the target category code for this Metacard. <br> */
  String TARGET_CATEGORY_CODE = "isr.target-category-code";

  /** Attribute name for accessing the target ID for this Metacard. */
  String TARGET_ID = "isr.target-id";

  /** Attribute name for accessing the target report count for this Metacard. */
  String TARGET_REPORT_COUNT = "isr.target-report-count";

  /** Attribute name for accessing the task comments for this Metacard. */
  String TASK_COMMENTS = "isr.task-comments";

  /** Attribute name for accessing the task ID for this Metacard. */
  String TASK_ID = "isr.task-id";

  /** Attribute name for accessing the task status for this Metacard. */
  String TASK_STATUS = "isr.task-status";

  /** Attribute name for accessing the CBRN alarm classification for this Metacard. */
  String CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_ALARM_CLASSIFICATION =
      "isr.cbrn-alarm-classification";

  /** Attribute name for accessing the CBRN category for this Metacard. */
  String CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_CATEGORY = "isr.cbrn-category";

  /** Attribute name for accessing the CBRN incident number for this Metacard. */
  String CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_INCIDENT_NUMBER = "isr.cbrn-incident-number";

  /** Attribute name for accessing the CBRN operation name for this Metacard. */
  String CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_OPERATION_NAME = "isr.cbrn-operation-name";

  /** Attribute name for accessing the CBRN type for this Metacard. */
  String CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_TYPE = "isr.cbrn-type";

  /** Attribute name for accessing the CBRN substance for this Metacard. */
  String CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_SUBSTANCE = "isr.cbrn-substance";

  /** Attribute name for accessing the TDL platform number for this Metacard. */
  String TACTICAL_DATA_LINK_PLATFORM = "isr.tdl-platform-number";

  /** Attribute name for accessing the TDL activity number for this Metacard. */
  String TACTICAL_DATA_LINK_ACTIVITY = "isr.tdl-activity";

  /** Attribute name for accessing the TDL track number for this Metacard. */
  String TACTICAL_DATA_LINK_TRACK_NUMBER = "isr.tdl-track-number";

  /** Attribute name for accessing the TDL message number for this Metacard. */
  String TACTICAL_DATA_LINK_MESSAGE_NUMBER = "isr.tdl-message-number";

  /** Attribute name for accessing the MISM level for this Metacard. */
  String VIDEO_MOTION_IMAGERY_SYSTEMS_MATRIX_LEVEL = "isr.video-mism-level";

  /** Attribute name for accessing the ISR category for this Metacard. */
  String CATEGORY = "isr.category";

  /*  The following attribute names are experimental and may change. */

  /** Attribute name for accessing the ISR data quality for this Metacard. */
  String DATA_QUALITY = "ext.isr.data-quality";
}
