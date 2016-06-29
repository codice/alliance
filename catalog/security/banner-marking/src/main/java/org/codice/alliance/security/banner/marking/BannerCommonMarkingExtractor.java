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
 **/
package org.codice.alliance.security.banner.marking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;

public class BannerCommonMarkingExtractor extends MarkingExtractor {

    public static final String SECURITY_CLASSIFICATION = "security.classification";

    public static final String SECURITY_RELEASABILITY = "security.releasability";

    public static final String SECURITY_CODEWORDS = "security.codewords";

    public static final String SECURITY_DISSEMINATION_CONTROLS = "security.dissemination-controls";

    public static final String SECURITY_OWNER_PRODUCER = "security.owner-producer";

    public static final String SECURITY_CLASSIFICATION_SYSTEM = "security.classification-system";

    public BannerCommonMarkingExtractor() {
        Map<String, BiFunction<Metacard, BannerMarkings, Attribute>> tempMap = new HashMap<>();

        tempMap.put(SECURITY_CLASSIFICATION, this::processClassMarking);
        tempMap.put(SECURITY_RELEASABILITY, this::processReleasability);
        tempMap.put(SECURITY_CODEWORDS, this::processCodewords);
        tempMap.put(SECURITY_DISSEMINATION_CONTROLS, this::processDissem);

        // At the present moment, we are going to be filling the security.owner-producer and
        // security.classification-system fields with the same content, the trigraph/tetragraph
        // of the originating country/organization. That may diverge in the future
        tempMap.put(SECURITY_OWNER_PRODUCER, this::processOwnerProducer);
        tempMap.put(SECURITY_CLASSIFICATION_SYSTEM, this::processClassSystem);

        setAttProcessors(tempMap);
    }

    Attribute processClassMarking(Metacard metacard, BannerMarkings bannerMarkings) {
        List<String> classifications = Lists.newArrayList();
        Attribute currAttr = metacard.getAttribute(SECURITY_CLASSIFICATION);
        if (currAttr != null) {
            classifications.add((String) currAttr.getValue());
        }
        classifications.add(
                translateClassification(bannerMarkings.getClassification(), bannerMarkings.isNato(),
                        bannerMarkings.getNatoQualifier()));
        return new AttributeImpl(SECURITY_CLASSIFICATION,
                ImmutableList.<String>copyOf(classifications));
    }

    Attribute processReleasability(Metacard metacard, BannerMarkings bannerMarkings) {
        Attribute currAttr = metacard.getAttribute(SECURITY_RELEASABILITY);
        if (currAttr != null) {
            return new AttributeImpl(SECURITY_RELEASABILITY,
                    dedupedList(bannerMarkings.getRelTo(), currAttr.getValues()));
        } else {
            return new AttributeImpl(SECURITY_RELEASABILITY,
                    ImmutableList.<String>copyOf(bannerMarkings.getRelTo()));
        }
    }

    Attribute processCodewords(Metacard metacard, BannerMarkings bannerMarkings) {
        List<String> sciControls = new ArrayList<>();
        for (BannerMarkings.SciControl sci : bannerMarkings.getSciControls()) {
            if (sci.getCompartments()
                    .isEmpty()) {
                sciControls.add(sci.getControl());
                continue;
            }
            for (Map.Entry<String, List<String>> comp : sci.getCompartments()
                    .entrySet()) {
                StringBuilder sb = new StringBuilder(sci.getControl());
                sb.append("-");
                sb.append(comp.getKey());
                if (comp.getValue()
                        .isEmpty()) {
                    sciControls.add(sb.toString());
                    continue;
                }
                sb.append(comp.getValue()
                        .stream()
                        .collect(Collectors.joining(" ", " ", "")));
                sciControls.add(sb.toString());
            }
        }

        Attribute currAttr = metacard.getAttribute(SECURITY_CODEWORDS);
        if (currAttr != null) {
            return new AttributeImpl(SECURITY_CODEWORDS,
                    dedupedList(sciControls, currAttr.getValues()));
        } else {
            return new AttributeImpl(SECURITY_CODEWORDS, ImmutableList.<String>copyOf(sciControls));
        }
    }

    Attribute processDissem(Metacard metacard, BannerMarkings bannerMarkings) {
        List<String> dissem = bannerMarkings.getDisseminationControls()
                .stream()
                .map(BannerMarkings.DissemControl::getName)
                .collect(Collectors.toList());

        Attribute currAttr = metacard.getAttribute(SECURITY_DISSEMINATION_CONTROLS);
        if (currAttr != null) {
            return new AttributeImpl(SECURITY_DISSEMINATION_CONTROLS,
                    dedupedList(dissem, currAttr.getValues()));
        } else {
            return new AttributeImpl(SECURITY_DISSEMINATION_CONTROLS,
                    ImmutableList.<String>copyOf(dissem));
        }
    }

    Attribute processOwnerProducer(Metacard metacard, BannerMarkings bannerMarkings) {
        return processClassOrOwnerProducer(metacard, bannerMarkings, SECURITY_OWNER_PRODUCER);
    }

    Attribute processClassSystem(Metacard metacard, BannerMarkings bannerMarkings) {
        return processClassOrOwnerProducer(metacard, bannerMarkings,
                SECURITY_CLASSIFICATION_SYSTEM);
    }

    private Attribute processClassOrOwnerProducer(Metacard metacard, BannerMarkings bannerMarkings,
            String key) {
        switch (bannerMarkings.getType()) {
        case US:
            return new AttributeImpl(key, ImmutableList.<String>of("USA"));
        case FGI:
            if (bannerMarkings.getFgiAuthority().equals("COSMIC")) {
                return new AttributeImpl(key, ImmutableList.<String>of("NATO"));
            } else {
                return new AttributeImpl(key,
                        ImmutableList.<String>of(bannerMarkings.getFgiAuthority()));
            }
        case JOINT:
            return new AttributeImpl(key,
                    ImmutableList.<String>copyOf(bannerMarkings.getJointAuthorities()));
        default:
            return metacard.getAttribute(key);
        }
    }
}
