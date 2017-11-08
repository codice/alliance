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
package org.codice.alliance.nsili.mockserver.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.codice.alliance.nsili.common.GIAS.AttributeInformation;
import org.codice.alliance.nsili.common.GIAS.AttributeType;
import org.codice.alliance.nsili.common.GIAS.DateRange;
import org.codice.alliance.nsili.common.GIAS.Domain;
import org.codice.alliance.nsili.common.GIAS.FloatingPointRange;
import org.codice.alliance.nsili.common.GIAS.IntegerRange;
import org.codice.alliance.nsili.common.GIAS.RequirementMode;
import org.codice.alliance.nsili.common.GIAS.View;
import org.codice.alliance.nsili.common.NsiliConstants;
import org.codice.alliance.nsili.common.UCO.AbsTime;
import org.codice.alliance.nsili.common.UCO.Coordinate2d;
import org.codice.alliance.nsili.common.UCO.Date;
import org.codice.alliance.nsili.common.UCO.Rectangle;
import org.codice.alliance.nsili.common.UCO.Time;

public class AttributeInformationGenerator {

  private static final List<String> VIEWS = Arrays.asList(NsiliConstants.NSIL_ALL_VIEW);

  private static final AbsTime EARLIEST =
      new AbsTime(
          new Date((short) 1, (short) 1, (short) 1970), new Time((short) 18, (short) 0, (short) 0));

  private static final AbsTime LATEST =
      new AbsTime(
          new Date((short) 1, (short) 1, (short) 2020), new Time((short) 18, (short) 0, (short) 0));

  private static final DateRange DATE_RANGE = new DateRange(EARLIEST, LATEST);

  private static final String[] IMAGERY_DOMAIN = {
    "VIS", "SL", "TI", "FL", "RD", "EO", "OP", "HR", "HS", "CP", "BP", "SAR", "SARIQ", "IR", "MS",
    "FP", "MRI", "XRAY", "CAT", "VD", "BARO", "CURRENT", "DEPTH", "WIND", "MAP", "PAT", "LEG",
    "DTEM", "MATR", "LOCG"
  };

  private static final String[] DECOMPRESSION_TECHNIQUE = {
    "NC", "NM", "C1", "M1", "I1", "C3", "M3", "C4", "M4", "C5", "M5", "C8", "M8"
  };

  private static final String[] CLASSIFICATION_DOMAIN = {
    "COSMIC TOP SECRET", "SECRET", "CONFIDENTIAL", "RESTRICTED", "UNCLASSIFIED", "NO CLASSIFICATION"
  };

  private static final String[] MESSAGE_TYPE = {"XMPP"};

  private static final String[] ENCODING_SCHEMES = {"264ON2", "MPEG-2"};

  private static final String[] VIDEO_CATEGORIES = {"VIS", "IR", "MS", "HS"};

  private static final FloatingPointRange[] FLOATING_POINT_RANGE = {
    new FloatingPointRange(0.0, 4294967295.0)
  };

  private static final Rectangle RECTANGLE_DOMAIN =
      new Rectangle(new Coordinate2d(0.0, 0.0), new Coordinate2d(0.0, 0.0));

  public static View[] generateViewNames() {
    View[] result = new View[VIEWS.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = new View(VIEWS.get(i), true, new String[0]);
    }
    return result;
  }

  public static AttributeInformation[] getAttributesForView(String view) {
    if (view.equals(VIEWS.get(0))) {
      return generateMandatoryNSIL_ALL_VIEWAttributeList();
    }
    return new AttributeInformation[0];
  }

  // All attributes here are queryable
  private static AttributeInformation[] generateMandatoryNSIL_ALL_VIEWAttributeList() {
    List<AttributeInformation> attributeInformationList = new ArrayList<>();

    Domain domain = new Domain();
    domain.t(36);
    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.IDENTIFIER_UUID,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "",
            false,
            true));

    domain = new Domain();
    domain.l(
        NsiliConstants.getContentStrings()
            .toArray(new String[NsiliConstants.getContentStrings().size()]));
    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.TYPE,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "",
            false,
            true));

    domain = new Domain();
    domain.t(159);
    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.TARGET_NUMBER,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "",
            false,
            true));

    domain = new Domain();
    domain.t(200);
    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.SOURCE,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "",
            false,
            true));

    domain = new Domain();
    domain.t(40);
    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.IDENTIFIER_MISSION,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "",
            false,
            true));

    domain = new Domain();
    domain.d(DATE_RANGE);
    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.DATE_TIME_DECLARED,
            AttributeType.UCOS_ABS_TIME,
            domain,
            "UTC",
            "",
            RequirementMode.OPTIONAL,
            "",
            true,
            true));

    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.DATE_TIME_MODIFIED,
            AttributeType.UCOS_ABS_TIME,
            domain,
            "UTC",
            "",
            RequirementMode.OPTIONAL,
            "",
            false,
            true));

    domain = new Domain();
    domain.g(RECTANGLE_DOMAIN);
    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.SPATIAL_GEOGRAPHIC_REF_BOX,
            AttributeType.UCOS_RECTANGLE,
            domain,
            "",
            "",
            RequirementMode.OPTIONAL,
            "",
            true,
            true));

    domain = new Domain();
    domain.fps(FLOATING_POINT_RANGE);
    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.IDENTIFIER_JOB,
            AttributeType.FLOATING_POINT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "",
            false,
            true));

    domain = new Domain();
    domain.ir(new IntegerRange(0, 2147483647));
    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.NUMBER_OF_TARGET_REPORTS,
            AttributeType.INTEGER,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "",
            false,
            true));

    domain = new Domain();
    domain.l(IMAGERY_DOMAIN);
    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.CATEGORY,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "",
            false,
            true));

    domain = new Domain();
    domain.l(DECOMPRESSION_TECHNIQUE);
    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.DECOMPRESSION_TECHNIQUE,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "",
            false,
            true));

    domain = new Domain();
    domain.t(10);
    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.IDENTIFIER,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "",
            false,
            true));

    domain = new Domain();
    domain.ir(new IntegerRange(0, 99999));
    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.NUMBER_OF_BANDS,
            AttributeType.INTEGER,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "",
            false,
            true));

    domain = new Domain();
    domain.t(200);
    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.RECIPIENT,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "",
            false,
            true));

    domain = new Domain();
    domain.t(2048);
    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.MESSAGE_BODY,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "",
            false,
            true));

    domain = new Domain();
    domain.l(MESSAGE_TYPE);
    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.MESSAGE_TYPE,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "",
            false,
            true));

    domain = new Domain();
    domain.l(CLASSIFICATION_DOMAIN);
    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.CLASSIFICATION,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "",
            true,
            true));

    domain = new Domain();
    domain.t(20);
    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.POLICY,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "",
            true,
            true));

    domain = new Domain();
    domain.t(50);
    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.RELEASABILITY,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "",
            true,
            true));

    domain = new Domain();
    domain.t(0);
    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.PART_IDENTIFIER,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "",
            false,
            true));

    domain = new Domain();
    domain.t(200);
    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.CREATOR,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "",
            true,
            true));

    domain = new Domain();
    domain.l(ENCODING_SCHEMES);
    attributeInformationList.add(
        createAttributeInformation(
            NsiliConstants.ENCODING_SCHEME,
            AttributeType.TEXT,
            domain,
            "",
            "",
            RequirementMode.MANDATORY,
            "",
            false,
            true));

    AttributeInformation[] attributeInformationArray =
        new AttributeInformation[attributeInformationList.size()];
    for (int i = 0; i < attributeInformationArray.length; i++) {
      attributeInformationArray[i] = attributeInformationList.get(i);
    }

    return attributeInformationArray;
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
}
