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
import com.google.common.collect.ImmutableMap;
import ddf.catalog.Constants;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codice.alliance.catalog.core.api.impl.types.SecurityAttributes;
import org.codice.alliance.catalog.core.api.types.Security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BannerCommonMarkingExtractor extends MarkingExtractor {

  private static final Logger INGEST_LOGGER = LoggerFactory.getLogger(Constants.INGEST_LOGGER_NAME);

  private Set<AttributeDescriptor> attributeDescriptors =
      new SecurityAttributes().getAttributeDescriptors();

  static final String SECURITY_CONFLICT_MESSAGE =
      "Extracted security attribute %s from the banner markings"
          + " does not match value pre-existing on the metacard. The"
          + " metacard will not be created.";

  final Map<String, BiFunction<Metacard, BannerMarkings, Attribute>> securityMap =
      ImmutableMap.<String, BiFunction<Metacard, BannerMarkings, Attribute>>builder()
          .put(Security.CLASSIFICATION, this::processClassMarking)
          .put(Security.RELEASABILITY, this::processReleasability)
          .put(Security.CODEWORDS, this::processCodewords)
          .put(Security.DISSEMINATION_CONTROLS, this::processDissem)
          // At the present moment, we are going to be filling the security.owner-producer and
          // security.classification-system fields with the same content, the trigraph/tetragraph
          // of the originating country/organization. That may diverge in the future
          .put(Security.OWNER_PRODUCER, this::processOwnerProducer)
          .put(Security.CLASSIFICATION_SYSTEM, this::processClassSystem)
          .build();

  public BannerCommonMarkingExtractor() {
    setAttProcessors(securityMap);
  }

  @Override
  public Set<AttributeDescriptor> getMetacardAttributes() {
    return attributeDescriptors;
  }

  private void checkSecurityAttribute(String name, Serializable oldVal, Serializable newVal) {
    if (!isSecurityAttribute(name) || StringUtils.isBlank((String) oldVal)) {
      return;
    }

    if (!newVal.equals(oldVal)) {
      throwSecurityMismatchException(name);
    }
  }

  private void checkSecurityAttributes(
      String name, List<Serializable> oldVals, List<Serializable> newVals) {
    if (!isSecurityAttribute(name) || CollectionUtils.isEmpty(oldVals)) {
      return;
    }

    // assert lists are the same
    if (oldVals.size() == newVals.size()) {
      oldVals.forEach(
          val -> {
            if (!newVals.contains(val)) {
              throwSecurityMismatchException(name);
            }
          });
    } else {
      throwSecurityMismatchException(name);
    }
  }

  private void throwSecurityMismatchException(String attrName) {
    String errorMessage = String.format(SECURITY_CONFLICT_MESSAGE, attrName);
    INGEST_LOGGER.error(errorMessage);
    throw new MarkingMismatchException(errorMessage);
  }

  private boolean isSecurityAttribute(String attrName) {
    return securityMap.keySet().contains(attrName);
  }

  public Attribute processClassMarking(Metacard metacard, BannerMarkings bannerMarkings) {
    Attribute currAttr = metacard.getAttribute(Security.CLASSIFICATION);
    String extractedClassValue =
        translateClassification(
            bannerMarkings.getClassification(),
            bannerMarkings.isNato(),
            bannerMarkings.getNatoQualifier());
    if (currAttr != null) {
      checkSecurityAttribute(Security.CLASSIFICATION, currAttr.getValue(), extractedClassValue);
    }
    return new AttributeImpl(Security.CLASSIFICATION, extractedClassValue);
  }

  Attribute processReleasability(Metacard metacard, BannerMarkings bannerMarkings) {
    Attribute currAttr = metacard.getAttribute(Security.RELEASABILITY);
    List<Serializable> extractedRelValues =
        (List<Serializable>) (List<?>) bannerMarkings.getRelTo();
    if (currAttr != null) {
      checkSecurityAttributes(Security.RELEASABILITY, currAttr.getValues(), extractedRelValues);
    }
    return new AttributeImpl(Security.RELEASABILITY, extractedRelValues);
  }

  Attribute processCodewords(Metacard metacard, BannerMarkings bannerMarkings) {
    List<Serializable> sciControls = new ArrayList<>();
    for (SciControl sci : bannerMarkings.getSciControls()) {
      if (sci.getCompartments().isEmpty()) {
        sciControls.add(sci.getControl());
        continue;
      }
      for (Map.Entry<String, List<String>> comp : sci.getCompartments().entrySet()) {
        StringBuilder sb = new StringBuilder(sci.getControl());
        sb.append("-");
        sb.append(comp.getKey());
        if (comp.getValue().isEmpty()) {
          sciControls.add(sb.toString());
          continue;
        }
        sb.append(comp.getValue().stream().collect(Collectors.joining(" ", " ", "")));
        sciControls.add(sb.toString());
      }
    }

    Attribute currAttr = metacard.getAttribute(Security.CODEWORDS);
    if (currAttr != null) {
      checkSecurityAttributes(Security.CODEWORDS, sciControls, currAttr.getValues());
    }
    return new AttributeImpl(Security.CODEWORDS, sciControls);
  }

  Attribute processDissem(Metacard metacard, BannerMarkings bannerMarkings) {
    List<Serializable> dissem =
        bannerMarkings.getDisseminationControls().stream()
            .map(DissemControl::getName)
            .collect(Collectors.toList());

    Attribute currAttr = metacard.getAttribute(Security.DISSEMINATION_CONTROLS);
    if (currAttr != null) {
      checkSecurityAttributes(Security.DISSEMINATION_CONTROLS, dissem, currAttr.getValues());
    }
    return new AttributeImpl(Security.DISSEMINATION_CONTROLS, dissem);
  }

  Attribute processOwnerProducer(Metacard metacard, BannerMarkings bannerMarkings) {
    return processClassOrOwnerProducer(metacard, bannerMarkings, Security.OWNER_PRODUCER);
  }

  Attribute processClassSystem(Metacard metacard, BannerMarkings bannerMarkings) {
    return processClassOrOwnerProducer(metacard, bannerMarkings, Security.CLASSIFICATION_SYSTEM);
  }

  private Attribute processClassOrOwnerProducer(
      Metacard metacard, BannerMarkings bannerMarkings, String key) {
    Attribute currAttr = metacard.getAttribute(key);
    switch (bannerMarkings.getType()) {
      case US:
        if (currAttr != null) {
          checkSecurityAttribute(key, currAttr.getValue(), "USA");
        }
        return new AttributeImpl(
            key, (List<? extends Serializable>) ImmutableList.<String>of("USA"));
      case FGI:
        if (bannerMarkings.getFgiAuthority().equals("COSMIC")) {
          if (currAttr != null) {
            checkSecurityAttribute(key, currAttr.getValue(), "NATO");
          }
          return new AttributeImpl(
              key, (List<? extends Serializable>) ImmutableList.<String>of("NATO"));
        } else {
          if (currAttr != null) {
            checkSecurityAttribute(key, currAttr.getValue(), bannerMarkings.getFgiAuthority());
          }
          return new AttributeImpl(
              key,
              (List<? extends Serializable>)
                  ImmutableList.<String>of(bannerMarkings.getFgiAuthority()));
        }
      case JOINT:
        if (currAttr != null) {
          checkSecurityAttributes(
              key,
              currAttr.getValues(),
              (List<Serializable>) (List<?>) bannerMarkings.getJointAuthorities());
        }
        return new AttributeImpl(
            key,
            (List<? extends Serializable>)
                ImmutableList.<String>copyOf(bannerMarkings.getJointAuthorities()));
      default:
        return metacard.getAttribute(key);
    }
  }
}
