/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.catalog.plugin.defaultsecurity;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.codice.alliance.catalog.core.api.impl.types.SecurityAttributes;
import org.codice.alliance.catalog.core.api.types.Security;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.saml2.core.AttributeStatement;

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

public class DefaultSecurityAttributeValuesPlugin implements PreIngestPlugin {

    private final SecurityAttributes securityAttributes;

    public static final String DEFAULTMARKINGS = "defaultMarkings";

    private static final Map<String, Set<String>> SYS_HIGH_TO_METACARD_ATTRIBUTE_MAPPING =
            new HashMap<>();

    public DefaultSecurityAttributeValuesPlugin(SecurityAttributes securityAttributes) {
        this.securityAttributes = securityAttributes;
    }

    Subject getSystemSubject() {
        return org.codice.ddf.security.common.Security.getInstance()
                .getSystemSubject();
    }

    /**
     * Retrieves the system high attributes and the mapping of the system attribute names to
     * metacard security markings. Returns a hash map of the metacard markings
     * to the value of its corresponding system attribute.
     *
     * @return Map of the metacard security markings to the value of their corresponding system high
     * attribute.
     */
    private Map<String, Attribute> getHighwaterSecurityMarkings() {
        Map<String, Attribute> securityMarkings = new HashMap<>();
        Subject system = org.codice.ddf.security.common.Security.runAsAdmin(this::getSystemSubject);
        SecurityAssertion assertion = system.getPrincipals()
                .oneByType(SecurityAssertion.class);
        List<AttributeStatement> attributeStatements = assertion.getAttributeStatements();
        for (AttributeStatement curStatement : attributeStatements) {
            for (org.opensaml.saml.saml2.core.Attribute attribute : curStatement.getAttributes()) {
                Collection<String> attributeNames = SYS_HIGH_TO_METACARD_ATTRIBUTE_MAPPING.get(
                        attribute.getName());

                if (attributeNames == null || attributeNames.isEmpty()) {
                    continue;
                }
                //if a user attribute is assigned to multiple metacard attributes add its values to each
                for (String attributeName : attributeNames) {
                    Collection<Serializable> values = attribute.getAttributeValues()
                            .stream()
                            .filter(curValue -> curValue instanceof XSString)
                            .map(XSString.class::cast)
                            .map(XSString::getValue)
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                    if (securityMarkings.get(attributeName) != null) {
                        values.addAll(securityMarkings.get(attributeName)
                                .getValues());
                    }
                    securityMarkings.put(attributeName,
                            new AttributeImpl(attributeName,
                                    (List<Serializable>) ImmutableList.copyOf(values)));
                }
            }
        }
        return securityMarkings;
    }

    /**
     * Adds default system high-water markings in the event that none of the policies were able to apply security markings to this metacard.
     *
     * @return Map of the metacard security markings to the value of their corresponding system high
     * attribute.
     */
    public Metacard addDefaults(Metacard metacard) {

        Map policyMap = (Map) metacard.getAttribute(Metacard.SECURITY)
                .getValue();
        if (policyMap != null && !policyMap.isEmpty()) {
            return metacard;
        }

        final Metacard extendedMetacard;
        MetacardImpl metacardImpl;
        if (!securityAttributes.getAttributeDescriptors()
                .stream()
                .anyMatch(ad -> metacard.getMetacardType()
                        .getAttributeDescriptors()
                        .contains(ad))) {
            metacardImpl = new MetacardImpl(metacard,
                    new MetacardTypeImpl(metacard.getMetacardType()
                            .getName(),
                            metacard.getMetacardType(),
                            securityAttributes.getAttributeDescriptors()));
        } else {
            metacardImpl = new MetacardImpl(metacard);
        }
        Set<String> updatedTags = new HashSet<>(metacard.getTags());
        updatedTags.add(DEFAULTMARKINGS);
        metacardImpl.setTags(updatedTags);
        extendedMetacard = metacardImpl;

        Map<String, Attribute> securityMarkings = getHighwaterSecurityMarkings();
        securityMarkings.keySet()
                .stream()
                .filter(securityMarking -> extendedMetacard.getMetacardType()
                        .getAttributeDescriptor(securityMarking) != null)
                .forEach(securityMarking -> {
                    extendedMetacard.setAttribute(securityMarkings.get(securityMarking));
                });
        return extendedMetacard;
    }

    public void setClassification(String classification) {
        addMapping(classification, Security.CLASSIFICATION);
    }

    private void addMapping(String userAttribute, String metacardAttribute) {
        Set<String> metacardAttributes =
                SYS_HIGH_TO_METACARD_ATTRIBUTE_MAPPING.get(userAttribute) != null ?
                        SYS_HIGH_TO_METACARD_ATTRIBUTE_MAPPING.get(userAttribute) :
                        new LinkedHashSet<>();
        metacardAttributes.add(metacardAttribute);
        SYS_HIGH_TO_METACARD_ATTRIBUTE_MAPPING.put(userAttribute, metacardAttributes);
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
        List<Metacard> updatedMetacards = createRequest.getMetacards()
                .stream()
                .filter(Objects::nonNull)
                .map(this::addDefaults)
                .collect(Collectors.toList());
        return new CreateRequestImpl(updatedMetacards,
                createRequest.getProperties(),
                createRequest.getStoreIds());
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
}
