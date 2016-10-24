/**
 * Copyright (c) Codice Foundation
 * <p/>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.video.stream.mpegts.metacard;

import static org.apache.commons.lang.Validate.notNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

import org.codice.alliance.catalog.core.api.types.Security;
import org.codice.alliance.catalog.core.internal.api.classification.SecurityClassificationService;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;

/**
 * Get the Security.CLASSIFICATION strings from the parent and child metacards, select the
 * string with the highest security rating, and set the parent metacard Security.CLASSIFICATION
 * with that rating. Uses {@link SecurityClassificationService#getSecurityClassificationComparator()}
 * to get a comparator that will be used to sort the security strings from the lowest to highest
 * security rating.
 */
public class SecurityClassificationMetacardUpdater implements MetacardUpdater {

    private SecurityClassificationService securityClassificationService;

    /**
     * @param securityClassificationService must be non-null
     */
    public SecurityClassificationMetacardUpdater(
            SecurityClassificationService securityClassificationService) {
        notNull(securityClassificationService, "securityClassificationService must be non-null");
        this.securityClassificationService = securityClassificationService;
    }

    @Override
    public String toString() {
        return "SecurityClassificationMetacardUpdater{}";
    }

    @Override
    public void update(Metacard parent, Metacard child) {

        Comparator<String> comparator =
                securityClassificationService.getSecurityClassificationComparator();

        Stream.of(parent, child)
                .map(this::getAttribute)
                .filter(Objects::nonNull)
                .map(Attribute::getValues)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .max(comparator)
                .ifPresent(classification -> {
                    parent.setAttribute(new AttributeImpl(Security.CLASSIFICATION, classification));
                });
    }

    private Attribute getAttribute(Metacard metacard) {
        return metacard.getAttribute(Security.CLASSIFICATION);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
