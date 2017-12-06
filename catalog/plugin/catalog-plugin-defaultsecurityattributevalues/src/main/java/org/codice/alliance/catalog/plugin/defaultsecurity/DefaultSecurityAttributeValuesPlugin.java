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

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import ddf.catalog.data.Attribute;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang.StringUtils;
import org.codice.alliance.catalog.core.api.impl.types.SecurityAttributes;
import org.codice.alliance.catalog.core.api.types.Security;
import org.codice.ddf.security.SystemHighAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This plugin sets security attributes on {@link Metacard}s to system high attribute values if none
 * of the policies were able to apply security markings to this {@link Metacard}.
 *
 * <p>The configuration for this plugin defines the mapping for 6 security {@link Attribute} names
 * to system high attribute names.
 *
 * <p>Each one of the 6 configuration keys corresponds to a security {@link Attribute} name:
 *
 * <table>
 * <tr>
 *     <td> Configuration key </td>                                       <td> {@link Attribute} name </td>
 * </tr>
 * <tr>
 *     <td> {@value CLASSIFICATION_CONFIGURATION_KEY} </td>               <td> {@value Security#CLASSIFICATION} </td>
 * </tr>
 * <tr>
 *     <td> {@value RELEASABILITY_CONFIGURATION_KEY} </td>                <td> {@value Security#RELEASABILITY} </td>
 * </tr>
 * <tr>
 *     <td> {@value CODEWORDS_CONFIGURATION_KEY} </td>                    <td> {@value Security#CODEWORDS} </td>
 * </tr>
 * <tr>
 *     <td> {@value DISSEMINATION_CONTROLS_CONFIGURATION_KEY} </td>       <td> {@value Security#DISSEMINATION_CONTROLS} </td>
 * </tr>
 * <tr>
 *     <td> {@value OTHER_DISSEMINATION_CONTROLS_CONFIGURATION_KEY} </td> <td> {@value Security#OTHER_DISSEMINATION_CONTROLS} </td>
 * </tr>
 * <tr>
 *     <td> {@value OWNER_PRODUCER_CONFIGURATION_KEY} </td>               <td> {@value Security#OWNER_PRODUCER} </td>
 * </tr>
 * </table>
 *
 * <p>For example, say the value for the key={@value CLASSIFICATION_CONFIGURATION_KEY} in the
 * configuration is "Clearance" when a {@link Metacard} is processed by this plugin. The value for
 * the system high attribute named "Clearance" is "U". The {@link Attribute} named {@value
 * Security#CLASSIFICATION} will be set to "U" on that {@link Metacard}.
 *
 * <p>If the system high attribute does not exist, the corresponding security {@link Attribute} will
 * not be set on any processed {@link Metacard}s, and processing will not be interrupted.
 */
public class DefaultSecurityAttributeValuesPlugin implements PreIngestPlugin {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(DefaultSecurityAttributeValuesPlugin.class);

  private static final String CLASSIFICATION_CONFIGURATION_KEY = "classification";

  private static final String RELEASABILITY_CONFIGURATION_KEY = "releasability";

  private static final String CODEWORDS_CONFIGURATION_KEY = "codewords";

  private static final String DISSEMINATION_CONTROLS_CONFIGURATION_KEY = "disseminationControls";

  private static final String OTHER_DISSEMINATION_CONTROLS_CONFIGURATION_KEY =
      "otherDisseminationControls";

  private static final String OWNER_PRODUCER_CONFIGURATION_KEY = "ownerProducer";

  private static final String DEFAULT_MARKINGS_TAG = "defaultMarkings";

  private final SecurityAttributes securityAttributes;

  private final SystemHighAttributes systemHighAttributes;

  /**
   * After initialization, this map will always have 6 entries, one for each of the {@link
   * Attribute} names. It is {@code volatile} and not synchronized because it could be re-assigned
   * in {@link DefaultSecurityAttributeValuesPlugin#update(Map)} while being iterated through in
   * {@link DefaultSecurityAttributeValuesPlugin#process(CreateRequest)}.
   */
  private volatile Map<String, String> metacardAttributeNameToSystemHighAttributeNameMap;

  public DefaultSecurityAttributeValuesPlugin(
      SecurityAttributes securityAttributes,
      SystemHighAttributes systemHighAttributes,
      Map<String, String> initialValues) {
    this.securityAttributes = notNull(securityAttributes, "SecurityAttributes may not be null");
    this.systemHighAttributes =
        notNull(systemHighAttributes, "SystemHighAttributes may not be null");
    update(initialValues);
  }

  /** Updates cached default system high attribute values. */
  public void update(Map<String, String> properties) {
    notEmpty(properties, "properties may not be empty");

    final Map<String, String> newMetacardAttributeNameToConfigurationValueMap = new HashMap<>();

    newMetacardAttributeNameToConfigurationValueMap.put(
        Security.CLASSIFICATION, notEmpty(properties.get(CLASSIFICATION_CONFIGURATION_KEY)));
    newMetacardAttributeNameToConfigurationValueMap.put(
        Security.RELEASABILITY, notEmpty(properties.get(RELEASABILITY_CONFIGURATION_KEY)));
    newMetacardAttributeNameToConfigurationValueMap.put(
        Security.CODEWORDS, notEmpty(properties.get(CODEWORDS_CONFIGURATION_KEY)));
    newMetacardAttributeNameToConfigurationValueMap.put(
        Security.DISSEMINATION_CONTROLS,
        notEmpty(properties.get(DISSEMINATION_CONTROLS_CONFIGURATION_KEY)));
    newMetacardAttributeNameToConfigurationValueMap.put(
        Security.OTHER_DISSEMINATION_CONTROLS,
        notEmpty(properties.get(OTHER_DISSEMINATION_CONTROLS_CONFIGURATION_KEY)));
    newMetacardAttributeNameToConfigurationValueMap.put(
        Security.OWNER_PRODUCER, notEmpty(properties.get(OWNER_PRODUCER_CONFIGURATION_KEY)));

    metacardAttributeNameToSystemHighAttributeNameMap =
        newMetacardAttributeNameToConfigurationValueMap;
  }

  private Metacard addDefaults(final Metacard metacard) {
    if (policiesHaveAlreadyAppliedSecurityMarkings(metacard)) {
      return metacard;
    }

    if (isRegistryMetacard(metacard)) {
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
        createRequest
            .getMetacards()
            .stream()
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
    return metacardAttributeNameToSystemHighAttributeNameMap
        .keySet()
        .stream()
        .anyMatch(attributeName -> metacard.getAttribute(attributeName) != null);
  }

  private boolean isRegistryMetacard(final Metacard metacard) {
    String registryTag = "registry";
    String registryRemoteTag = "registry-remote";
    if (!metacard.getTags().contains(registryTag)
        && !metacard.getTags().contains(registryRemoteTag)) {
      return false;
    }

    Attribute registryAttr = metacard.getAttribute("registry.registry-id");
    if (registryAttr == null || registryAttr.getValue() == null) {
      return false;
    }

    String registryId = registryAttr.getValue().toString();

    return !StringUtils.isEmpty(registryId);
  }

  private boolean doesNotHaveAnyOfTheSecurityAttributeDescriptors(final Metacard metacard) {
    return securityAttributes
        .getAttributeDescriptors()
        .stream()
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
}
