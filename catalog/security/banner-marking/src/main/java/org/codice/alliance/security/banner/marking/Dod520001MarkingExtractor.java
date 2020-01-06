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
package org.codice.alliance.security.banner.marking;

import com.google.common.collect.ImmutableList;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.codice.alliance.catalog.core.api.impl.types.Dod520001Attributes;
import org.codice.alliance.catalog.core.api.types.Dod520001;

/**
 * Processes US Markings in conformance with DoD Guildlines for banner markings.
 *
 * <p>These markings are in addition to the common set of markings as defined in our taxonomy and
 * managed by the {@link BannerCommonMarkingExtractor}.
 *
 * @see <a href="http://www.dtic.mil/whs/directives/corres/pdf/520001_vol2.pdf">DOD Manual Number
 *     5200.01, Volume 2</a>
 */
public class Dod520001MarkingExtractor extends MarkingExtractor {

  private static final String ACCM_PREFIX = "ACCM-";

  private static final String FGI_PREFIX = "FGI ";

  private Set<AttributeDescriptor> attributeDescriptors =
      new Dod520001Attributes().getAttributeDescriptors();

  public Dod520001MarkingExtractor() {
    Map<String, BiFunction<Metacard, BannerMarkings, Attribute>> tempMap = new HashMap<>();

    tempMap.put(Dod520001.SECURITY_DOD5200_SAP, this::processSap);
    tempMap.put(Dod520001.SECURITY_DOD5200_AEA, this::processAea);
    tempMap.put(Dod520001.SECURITY_DOD5200_DODUCNI, this::processDodUcni);
    tempMap.put(Dod520001.SECURITY_DOD5200_DOEUCNI, this::processDoeUcni);
    tempMap.put(Dod520001.SECURITY_DOD5200_FGI, this::processFgi);
    tempMap.put(Dod520001.SECURITY_DOD5200_OTHER_DISSEM, this::processOtherDissem);

    setAttProcessors(tempMap);
  }

  @Override
  public Set<AttributeDescriptor> getMetacardAttributes() {
    return attributeDescriptors;
  }

  Attribute processSap(Metacard metacard, BannerMarkings bannerMarkings) {
    Attribute currAttr = metacard.getAttribute(Dod520001.SECURITY_DOD5200_SAP);
    SapControl sapControl = bannerMarkings.getSapControl();
    if (sapControl == null) {
      return currAttr;
    }

    return new AttributeImpl(
        Dod520001.SECURITY_DOD5200_SAP,
        (List<? extends Serializable>) ImmutableList.<String>of(sapControl.toString()));
  }

  Attribute processAea(Metacard metacard, BannerMarkings bannerMarkings) {
    Attribute currAttr = metacard.getAttribute(Dod520001.SECURITY_DOD5200_AEA);
    AeaMarking aeaMarking = bannerMarkings.getAeaMarking();
    if (aeaMarking == null) {
      return currAttr;
    }

    return new AttributeImpl(
        Dod520001.SECURITY_DOD5200_AEA,
        (List<? extends Serializable>) ImmutableList.<String>of(aeaMarking.toString()));
  }

  Attribute processDodUcni(Metacard metacard, BannerMarkings bannerMarkings) {
    if (bannerMarkings.getDodUcni()) {
      return new AttributeImpl(
          Dod520001.SECURITY_DOD5200_DODUCNI,
          (List<? extends Serializable>)
              ImmutableList.<String>of("DOD UNCLASSIFIED CONTROLLED NUCLEAR INFORMATION"));
    }
    return metacard.getAttribute(Dod520001.SECURITY_DOD5200_DODUCNI);
  }

  Attribute processDoeUcni(Metacard metacard, BannerMarkings bannerMarkings) {
    if (bannerMarkings.getDoeUcni()) {
      return new AttributeImpl(
          Dod520001.SECURITY_DOD5200_DOEUCNI,
          (List<? extends Serializable>)
              ImmutableList.<String>of("DOE UNCLASSIFIED CONTROLLED NUCLEAR INFORMATION"));
    }
    return metacard.getAttribute(Dod520001.SECURITY_DOD5200_DOEUCNI);
  }

  Attribute processFgi(Metacard metacard, BannerMarkings bannerMarkings) {
    Attribute currAttr = metacard.getAttribute(Dod520001.SECURITY_DOD5200_FGI);
    List<String> fgiCountryCodes = bannerMarkings.getUsFgiCountryCodes();

    // There is a distinction between a null set of FGI country codes and an empty set of
    // FGI country codes. In the former case, there are no FGI markings present; in the
    // latter, there is an FGI marking with no listed countries/organizations.
    if (fgiCountryCodes == null) {
      return currAttr;
    }

    String fgi = fgiCountryCodes.stream().collect(Collectors.joining(" ", FGI_PREFIX, "")).trim();
    return new AttributeImpl(
        Dod520001.SECURITY_DOD5200_FGI,
        (List<? extends Serializable>) ImmutableList.<String>of(fgi));
  }

  Attribute processOtherDissem(Metacard metacard, BannerMarkings bannerMarkings) {
    List<String> otherDissem =
        bannerMarkings
            .getOtherDissemControl()
            .stream()
            .map(OtherDissemControl::getName)
            .collect(Collectors.toList());
    if (!CollectionUtils.isEmpty(bannerMarkings.getAccm())) {
      otherDissem.addAll(
          bannerMarkings
              .getAccm()
              .stream()
              .map(accm -> ACCM_PREFIX + accm)
              .collect(Collectors.toList()));
    }

    Attribute currAttr = metacard.getAttribute(Dod520001.SECURITY_DOD5200_OTHER_DISSEM);
    if (currAttr != null) {
      return new AttributeImpl(
          Dod520001.SECURITY_DOD5200_OTHER_DISSEM, dedupedList(otherDissem, currAttr.getValues()));
    } else {
      return new AttributeImpl(
          Dod520001.SECURITY_DOD5200_OTHER_DISSEM,
          (List<? extends Serializable>) ImmutableList.<String>copyOf(otherDissem));
    }
  }
}
