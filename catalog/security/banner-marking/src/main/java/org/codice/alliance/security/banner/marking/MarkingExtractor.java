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

import static org.codice.alliance.security.banner.marking.BannerMarkings.ClassificationLevel.SECRET;
import static org.codice.alliance.security.banner.marking.BannerMarkings.ClassificationLevel.TOP_SECRET;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import ddf.catalog.content.operation.ContentMetadataExtractor;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MarkingExtractor implements ContentMetadataExtractor {
  private static final Logger LOGGER = LoggerFactory.getLogger(MarkingExtractor.class);

  private Map<String, BiFunction<Metacard, BannerMarkings, Attribute>> attProcessors;

  private Set<AttributeDescriptor> attributeDescriptors;

  @Override
  public void process(String input, Metacard metacard) {
    process(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), metacard);
  }

  @Override
  public void process(InputStream input, Metacard metacard) {
    BannerMarkings bannerMarkings = null;
    try {
      Optional<String> bannerLine =
          new BufferedReader(new InputStreamReader(input))
              .lines()
              .map(String::trim)
              .filter(s -> !s.isEmpty())
              .findFirst();

      if (bannerLine.isPresent()) {
        bannerMarkings = BannerMarkings.parseMarkings(bannerLine.get());
      }
    } catch (MarkingsValidationException e) {
      LOGGER.debug("Errors validating document markings", e);
    }

    if (bannerMarkings == null) {
      return;
    }

    for (BiFunction<Metacard, BannerMarkings, Attribute> attFunc : attProcessors.values()) {
      metacard.setAttribute(attFunc.apply(metacard, bannerMarkings));
    }
  }

  @Override
  public Set<AttributeDescriptor> getMetacardAttributes() {
    return attributeDescriptors;
  }

  public String translateClassification(
      BannerMarkings.ClassificationLevel classLevel, boolean isNato, String natoQualifier) {
    if (!isNato) {
      return classLevel.getShortName();
    }

    StringBuilder builder = new StringBuilder();
    if (classLevel == TOP_SECRET) {
      builder.append("CTS");
    } else {
      builder.append("N").append(classLevel.getShortName());
    }

    if (!StringUtils.isEmpty(natoQualifier)) {
      if (natoQualifier.equals("BOHEMIA")) {
        builder.append("-B");
      }
      if (natoQualifier.equals("BALK")) {
        builder.append("-BALK");
      }
      if (natoQualifier.equals("ATOMAL")) {
        builder.append("A");
        // And handle the one odd case
        if (classLevel == SECRET) {
          builder.append("T");
        }
      }
    }

    return builder.toString();
  }

  protected void setAttProcessors(
      Map<String, BiFunction<Metacard, BannerMarkings, Attribute>> attProcessors) {
    this.attProcessors = ImmutableMap.copyOf(attProcessors);

    attributeDescriptors =
        ImmutableSet.copyOf(
            attProcessors
                .keySet()
                .stream()
                .map(
                    s ->
                        new AttributeDescriptorImpl(
                            s, false, true, false, true, BasicTypes.STRING_TYPE))
                .collect(Collectors.toSet()));
  }

  protected List<Serializable> dedupedList(
      Collection<? extends Serializable> collA, Collection<? extends Serializable> collB) {
    HashSet<Serializable> union = new HashSet<>(collA);
    union.addAll(collB);
    return ImmutableList.copyOf(union);
  }
}
