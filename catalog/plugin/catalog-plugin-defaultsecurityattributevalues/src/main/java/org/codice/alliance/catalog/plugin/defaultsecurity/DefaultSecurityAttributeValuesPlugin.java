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

import com.google.common.collect.ImmutableList;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.impl.MetacardTypeImpl;
import ddf.catalog.operation.CreateRequest;
import ddf.catalog.operation.DeleteRequest;
import ddf.catalog.operation.UpdateRequest;
import ddf.catalog.operation.impl.CreateRequestImpl;
import ddf.catalog.plugin.PluginExecutionException;
import ddf.catalog.plugin.PreIngestPlugin;
import ddf.catalog.plugin.StopProcessingException;
import ddf.security.Subject;
import ddf.security.assertion.SecurityAssertion;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.codice.alliance.catalog.core.api.impl.types.SecurityAttributes;
import org.codice.alliance.catalog.core.api.types.Security;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.saml2.core.AttributeStatement;

public class DefaultSecurityAttributeValuesPlugin implements PreIngestPlugin {

  private final SecurityAttributes securityAttributes;

  private final Supplier<Subject> subjectSupplier;

  private Map<String, Attribute> securityMarkings;

  public static final String DEFAULTMARKINGS = "defaultMarkings";

  static final String CLASSIFICATION_KEY = "classification";

  static final String CODEWORDS_KEY = "codewords";

  static final String RELEASABILITY_KEY = "releasability";

  static final String DISSEMINATION_CONTROLS_KEY = "disseminationControls";

  static final String OTHER_DISSEMINATION_CONTROLS_KEY = "otherDisseminationControls";

  static final String OWNER_PRODUCER_KEY = "ownerProducer";

  private static final Map<String, Set<String>> SYS_HIGH_TO_METACARD_ATTRIBUTE_MAPPING =
      new HashMap<>();

  public DefaultSecurityAttributeValuesPlugin(
      SecurityAttributes securityAttributes, Map<String, Object> initialValues) {
    this(
        securityAttributes,
        initialValues,
        () -> org.codice.ddf.security.common.Security.getInstance().getSystemSubject());
  }

  public DefaultSecurityAttributeValuesPlugin(
      SecurityAttributes securityAttributes,
      Map<String, Object> initialValues,
      Supplier<Subject> subjectSupplier) {
    this.securityAttributes = securityAttributes;
    this.subjectSupplier = subjectSupplier;

    update(initialValues);
  }

  public void update(Map<String, Object> properties) {
    if (properties != null && !properties.isEmpty()) {
      setClassification((String) properties.get(CLASSIFICATION_KEY));
      setReleasability((String) properties.get(RELEASABILITY_KEY));
      setCodewords((String) properties.get(CODEWORDS_KEY));
      setDisseminationControls((String) properties.get(DISSEMINATION_CONTROLS_KEY));
      setOtherDisseminationControls((String) properties.get(OTHER_DISSEMINATION_CONTROLS_KEY));
      setOwnerProducer((String) properties.get(OWNER_PRODUCER_KEY));
    }

    this.securityMarkings = getHighwaterSecurityMarkings();
  }

  /**
   * Adds default system high-water markings in the event that none of the policies were able to
   * apply security markings to this metacard.
   *
   * @return Map of the metacard security markings to the value of their corresponding system high
   *     attribute.
   */
  public Metacard addDefaults(Metacard metacard) {

    if (securityMarkings
        .keySet()
        .stream()
        .anyMatch(attributeName -> metacard.getAttribute(attributeName) != null)) {
      return metacard;
    }

    if (isRegistryMetacard(metacard)) {
      return metacard;
    }

    final Metacard extendedMetacard;
    MetacardImpl metacardImpl;
    if (securityAttributes
        .getAttributeDescriptors()
        .stream()
        .noneMatch(ad -> metacard.getMetacardType().getAttributeDescriptors().contains(ad))) {
      metacardImpl =
          new MetacardImpl(
              metacard,
              new MetacardTypeImpl(
                  metacard.getMetacardType().getName(),
                  metacard.getMetacardType(),
                  securityAttributes.getAttributeDescriptors()));
    } else {
      metacardImpl = new MetacardImpl(metacard);
    }
    Set<String> updatedTags = new HashSet<>(metacard.getTags());
    updatedTags.add(DEFAULTMARKINGS);
    metacardImpl.setTags(updatedTags);
    extendedMetacard = metacardImpl;

    securityMarkings
        .keySet()
        .stream()
        .filter(
            securityMarking ->
                extendedMetacard.getMetacardType().getAttributeDescriptor(securityMarking) != null)
        .forEach(
            securityMarking -> {
              extendedMetacard.setAttribute(securityMarkings.get(securityMarking));
            });
    return extendedMetacard;
  }

  public void setClassification(String classification) {
    addMapping(classification, Security.CLASSIFICATION);
  }

  public void setReleasability(String releasability) {
    addMapping(releasability, Security.RELEASABILITY);
  }

  public void setCodewords(String codewords) {
    addMapping(codewords, Security.CODEWORDS);
  }

  public void setDisseminationControls(String disseminationControls) {
    addMapping(disseminationControls, Security.DISSEMINATION_CONTROLS);
  }

  public void setOtherDisseminationControls(String otherDisseminationControls) {
    addMapping(otherDisseminationControls, Security.OTHER_DISSEMINATION_CONTROLS);
  }

  public void setOwnerProducer(String ownerProducer) {
    addMapping(ownerProducer, Security.OWNER_PRODUCER);
  }

  @Override
  public CreateRequest process(CreateRequest createRequest)
      throws PluginExecutionException, StopProcessingException {
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
  public UpdateRequest process(UpdateRequest updateRequest)
      throws PluginExecutionException, StopProcessingException {
    return updateRequest;
  }

  @Override
  public DeleteRequest process(DeleteRequest input)
      throws PluginExecutionException, StopProcessingException {
    return input;
  }

  private void addMapping(String userAttribute, String metacardAttribute) {
    Set<String> metacardAttributes =
        SYS_HIGH_TO_METACARD_ATTRIBUTE_MAPPING.get(userAttribute) != null
            ? SYS_HIGH_TO_METACARD_ATTRIBUTE_MAPPING.get(userAttribute)
            : new LinkedHashSet<>();
    metacardAttributes.add(metacardAttribute);
    SYS_HIGH_TO_METACARD_ATTRIBUTE_MAPPING.put(userAttribute, metacardAttributes);
  }

  /**
   * Retrieves the system high attributes and the mapping of the system attribute names to metacard
   * security markings. Returns a hash map of the metacard markings to the value of its
   * corresponding system attribute.
   *
   * @return Map of the metacard security markings to the value of their corresponding system high
   *     attribute.
   */
  private Map<String, Attribute> getHighwaterSecurityMarkings() {
    Map<String, Attribute> securityMarkings = new HashMap<>();
    Subject system =
        org.codice.ddf.security.common.Security.getInstance().runAsAdmin(subjectSupplier::get);
    SecurityAssertion assertion = system.getPrincipals().oneByType(SecurityAssertion.class);
    List<AttributeStatement> attributeStatements = assertion.getAttributeStatements();
    for (AttributeStatement curStatement : attributeStatements) {
      for (org.opensaml.saml.saml2.core.Attribute attribute : curStatement.getAttributes()) {
        Collection<String> attributeNames =
            SYS_HIGH_TO_METACARD_ATTRIBUTE_MAPPING.get(attribute.getName());

        if (attributeNames == null || attributeNames.isEmpty()) {
          continue;
        }
        // if a user attribute is assigned to multiple metacard attributes add its values to each
        for (String attributeName : attributeNames) {
          Collection<Serializable> values =
              attribute
                  .getAttributeValues()
                  .stream()
                  .filter(curValue -> curValue instanceof XSString)
                  .map(XSString.class::cast)
                  .map(XSString::getValue)
                  .collect(Collectors.toCollection(LinkedHashSet::new));
          if (securityMarkings.get(attributeName) != null) {
            values.addAll(securityMarkings.get(attributeName).getValues());
          }
          securityMarkings.put(
              attributeName,
              new AttributeImpl(attributeName, (List<Serializable>) ImmutableList.copyOf(values)));
        }
      }
    }
    return securityMarkings;
  }

  private boolean isRegistryMetacard(Metacard metacard) {
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
}
