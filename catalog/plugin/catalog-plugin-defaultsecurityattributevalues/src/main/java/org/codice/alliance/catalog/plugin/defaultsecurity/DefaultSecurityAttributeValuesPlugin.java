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
package org.codice.alliance.catalog.plugin.defaultsecurity;

import static org.apache.commons.lang3.Validate.notNull;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.impl.MetacardTypeImpl;
import ddf.catalog.operation.CreateRequest;
import ddf.catalog.operation.DeleteRequest;
import ddf.catalog.operation.UpdateRequest;
import ddf.catalog.operation.impl.CreateRequestImpl;
import ddf.catalog.plugin.PreIngestPlugin;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.codice.alliance.catalog.core.api.impl.types.SecurityAttributes;
import org.codice.ddf.security.SystemHighAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This plugin sets security attributes on {@link Metacard}s to system high attribute values if none
 * of the policies were able to apply security markings to this {@link Metacard}.
 *
 * <p>The configuration for this plugin defines the mapping for metacard security attributes to
 * system high attributes
 *
 * <p>If the system high attribute does not exist, the corresponding security {@link
 * ddf.catalog.data.Attribute} will not be set on any processed {@link Metacard}s, and processing
 * will not be interrupted.
 */
public class DefaultSecurityAttributeValuesPlugin implements PreIngestPlugin {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(DefaultSecurityAttributeValuesPlugin.class);

  private static final String DEFAULT_MARKINGS_TAG = "defaultMarkings";

  private final SecurityAttributes securityAttributes;

  private final SystemHighAttributes systemHighAttributes;

  /**
   * After initialization, this map is {@code volatile} and not synchronized because it could be
   * re-assigned in {@link DefaultSecurityAttributeValuesPlugin#setAttributeMappings(List)} while
   * being iterated through in {@link DefaultSecurityAttributeValuesPlugin#process(CreateRequest)}.
   */
  private volatile Map<String, String> metacardAttributeNameToSystemHighAttributeNameMap;

  public DefaultSecurityAttributeValuesPlugin(
      SecurityAttributes securityAttributes,
      SystemHighAttributes systemHighAttributes,
      Map<String, String> initialValues) {
    this.securityAttributes = notNull(securityAttributes, "SecurityAttributes may not be null");
    this.systemHighAttributes =
        notNull(systemHighAttributes, "SystemHighAttributes may not be null");
    this.metacardAttributeNameToSystemHighAttributeNameMap = initialValues;
  }

  private Metacard addDefaults(final Metacard metacard) {
    if (policiesHaveAlreadyAppliedSecurityMarkings(metacard)) {
      return metacard;
    }

    if (!isResourceMetacard(metacard)) {
      return metacard;
    }

    final MetacardImpl extendedMetacard;
    if (doesNotHaveAnyOfTheSecurityAttributeDescriptors(metacard)) {
      extendedMetacard = addAllOfTheSecurityAttributeDescriptors(metacard);
    } else {
      extendedMetacard = new MetacardImpl(metacard);
    }

    final Set<String> updatedTags = new HashSet<>(metacard.getTags());
    for (Map.Entry<String, String> entry :
        metacardAttributeNameToSystemHighAttributeNameMap.entrySet()) {
      final String metacardAttributeName = entry.getKey();
      final String systemHighAttributeName = entry.getValue();

      if (extendedMetacard.getMetacardType().getAttributeDescriptor(metacardAttributeName)
          != null) {
        final Set<String> systemHighAttributeValues =
            systemHighAttributes.getValues(systemHighAttributeName);
        if (!systemHighAttributeValues.isEmpty()) {
          extendedMetacard.setAttribute(
              new AttributeImpl(
                  metacardAttributeName,
                  (List<Serializable>) new ArrayList<Serializable>(systemHighAttributeValues)));
          updatedTags.add(DEFAULT_MARKINGS_TAG);
        } else {
          LOGGER.debug(
              "Not setting default for {} metacard attribute because couldn't find system high attribute {}.",
              metacardAttributeName,
              systemHighAttributeName);
        }
      } else {
        LOGGER.debug(
            "Not setting default for {} metacard attribute because there is not a corresponding AttributeDescriptor",
            metacardAttributeName);
      }
    }
    extendedMetacard.setTags(updatedTags);

    return extendedMetacard;
  }

  @Override
  public CreateRequest process(CreateRequest createRequest) {
    List<Metacard> updatedMetacards =
        createRequest.getMetacards().stream()
            .filter(Objects::nonNull)
            .map(this::addDefaults)
            .collect(Collectors.toList());
    return new CreateRequestImpl(
        updatedMetacards, createRequest.getProperties(), createRequest.getStoreIds());
  }

  @Override
  public UpdateRequest process(UpdateRequest updateRequest) {
    return updateRequest;
  }

  @Override
  public DeleteRequest process(DeleteRequest input) {
    return input;
  }

  private boolean policiesHaveAlreadyAppliedSecurityMarkings(Metacard metacard) {
    return metacardAttributeNameToSystemHighAttributeNameMap.keySet().stream()
        .anyMatch(attributeName -> metacard.getAttribute(attributeName) != null);
  }

  private boolean isResourceMetacard(final Metacard metacard) {
    return metacard.getTags().contains(Metacard.DEFAULT_TAG);
  }

  private boolean doesNotHaveAnyOfTheSecurityAttributeDescriptors(final Metacard metacard) {
    return securityAttributes.getAttributeDescriptors().stream()
        .noneMatch(ad -> metacard.getMetacardType().getAttributeDescriptors().contains(ad));
  }

  @NotNull
  private MetacardImpl addAllOfTheSecurityAttributeDescriptors(final Metacard metacard) {
    return new MetacardImpl(
        metacard,
        new MetacardTypeImpl(
            metacard.getMetacardType().getName(),
            metacard.getMetacardType(),
            securityAttributes.getAttributeDescriptors()));
  }

  public void setAttributeMappings(List<String> attributeMappings) {
    if (attributeMappings != null) {
      metacardAttributeNameToSystemHighAttributeNameMap.clear();
      for (String mapping : attributeMappings) {
        String[] parts = mapping.trim().split("=");
        if (parts.length == 2) {
          metacardAttributeNameToSystemHighAttributeNameMap.put(parts[0], parts[1]);
        }
      }
    }
  }
}
