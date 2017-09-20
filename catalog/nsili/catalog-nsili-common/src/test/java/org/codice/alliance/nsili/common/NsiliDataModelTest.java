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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.codice.alliance.nsili.common.GIAS.Association;
import org.codice.alliance.nsili.common.GIAS.AttributeInformation;
import org.codice.alliance.nsili.common.GIAS.ConceptualAttributeType;
import org.codice.alliance.nsili.common.datamodel.NsiliDataModel;
import org.junit.Test;

public class NsiliDataModelTest {

  protected NsiliDataModel nsiliDataModel = new NsiliDataModel();

  @Test
  public void testAliasCategories() {
    List<String> aliasCategories = nsiliDataModel.getAliasCategories();
    assertThat(aliasCategories.size(), is(6));
  }

  @Test
  public void testAliases() {
    List<Pair<String, String>> aliases =
        nsiliDataModel.getAliasesForCategory(NsiliConstants.STANAG_4545);
    assertThat(aliases.size(), is(26));

    aliases = nsiliDataModel.getAliasesForCategory(NsiliConstants.NACT_L16);
    assertThat(aliases.size(), is(4));

    aliases = nsiliDataModel.getAliasesForCategory(NsiliConstants.NSIL_CORE);
    assertThat(aliases.size(), is(22));

    aliases = nsiliDataModel.getAliasesForCategory(NsiliConstants.STANAG_5516);
    assertThat(aliases.size(), is(1));

    aliases = nsiliDataModel.getAliasesForCategory(NsiliConstants.STANAG_4609);
    assertThat(aliases.size(), is(8));

    aliases = nsiliDataModel.getAliasesForCategory(NsiliConstants.STANAG_4607);
    assertThat(aliases.size(), is(11));
  }

  @Test
  public void testConceptualAttrs() {
    List<Pair<ConceptualAttributeType, String>> attrs =
        nsiliDataModel.getConceptualAttrsForView(NsiliConstants.NSIL_ALL_VIEW);
    assertThat(attrs.size(), is(8));

    attrs = nsiliDataModel.getConceptualAttrsForView(NsiliConstants.NSIL_IMAGERY_VIEW);
    assertThat(attrs.size(), is(8));

    attrs = nsiliDataModel.getConceptualAttrsForView(NsiliConstants.NSIL_GMTI_VIEW);
    assertThat(attrs.size(), is(8));

    attrs = nsiliDataModel.getConceptualAttrsForView(NsiliConstants.NSIL_MESSAGE_VIEW);
    assertThat(attrs.size(), is(8));

    attrs = nsiliDataModel.getConceptualAttrsForView(NsiliConstants.NSIL_VIDEO_VIEW);
    assertThat(attrs.size(), is(8));

    attrs = nsiliDataModel.getConceptualAttrsForView(NsiliConstants.NSIL_ASSOCIATION_VIEW);
    assertThat(attrs.size(), is(2));

    attrs = nsiliDataModel.getConceptualAttrsForView(NsiliConstants.NSIL_REPORT_VIEW);
    assertThat(attrs.size(), is(8));

    attrs = nsiliDataModel.getConceptualAttrsForView(NsiliConstants.NSIL_TDL_VIEW);
    assertThat(attrs.size(), is(8));

    attrs = nsiliDataModel.getConceptualAttrsForView(NsiliConstants.NSIL_CCIRM_VIEW);
    assertThat(attrs.size(), is(8));
  }

  @Test
  public void testAssociations() {
    List<Association> associations = nsiliDataModel.getAssociations();
    assertThat(associations.size(), is(6));

    for (Association association : associations) {
      assertThat(association.attribute_info.length, is(12));
    }
  }

  @Test
  public void testNsiliMandatoryAttrs() {
    Map<String, List<String>> mandatoryAttrs =
        nsiliDataModel.getRequiredAttrsForView(NsiliConstants.NSIL_ALL_VIEW);
    List<String> commonAttrs = mandatoryAttrs.get(NsiliConstants.NSIL_COMMON);
    assertThat(commonAttrs, notNullValue());
    assertThat(commonAttrs.size(), is(2));
  }

  @Test
  public void testNsiliAmd2Attributes() {
    List<AttributeInformation> attributesForView =
        nsiliDataModel.getAttributesForView(NsiliConstants.NSIL_ALL_VIEW);
    assertThat(attributesForView, notNullValue());

    boolean advancedGeoSpatialExists = false;
    int numEntityNodeAttrs = 0;
    for (AttributeInformation attributeInformation : attributesForView) {
      if (attributeInformation.attribute_name.contains(NsiliConstants.ADVANCED_GEOSPATIAL)) {
        advancedGeoSpatialExists = true;
      } else if (attributeInformation.attribute_name.contains(NsiliConstants.NSIL_ENTITY)) {
        numEntityNodeAttrs++;
      }
    }
    assertThat(advancedGeoSpatialExists, is(true));
    assertThat(numEntityNodeAttrs, is(3));
  }
}
