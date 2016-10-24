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
package org.codice.alliance.catalog.core.internal.impl.classification;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.Holder;

import org.codice.alliance.catalog.core.internal.api.classification.SecurityClassificationService;

/**
 * Returns a comparator that sorts classification strings based on a user defined sort order {@link #setSortOrder(List)}.
 * If a comparator encounters a classification string that was not in the user defined sort order, then
 * that string will be given the highest possible sort priority.
 * <p>
 * <b> This code is experimental. While this interface is functional and tested, it may change or be
 * removed in a future version of the library. </b>
 */
public class SecurityClassificationServiceImpl implements SecurityClassificationService {

    private volatile SecurityClassificationComparator comparator;

    private static String normalize(String s) {
        return s.replaceAll("\\s", "")
                .toLowerCase();
    }

    @Override
    public Comparator<String> getSecurityClassificationComparator() {
        return comparator;
    }

    /**
     * Leading and trailing whitespace will be trimmed.
     *
     * @param rawSortOrder a pipe-separated list of classification strings
     */
    public void setSortOrder(List<String> rawSortOrder) {
        Map<String, Integer> newSecurityClassificationSortOrder = new HashMap<>();

        Holder<Integer> i = new Holder<>(0);
        rawSortOrder.forEach(classification -> {
            newSecurityClassificationSortOrder.put(normalize(classification), i.value);
            i.value++;
        });

        comparator = new SecurityClassificationComparator(newSecurityClassificationSortOrder);
    }

    private static class SecurityClassificationComparator implements Comparator<String> {

        private final Map<String, Integer> securityClassificationSortOrder;

        private SecurityClassificationComparator(
                Map<String, Integer> securityClassificationSortOrder) {
            this.securityClassificationSortOrder = securityClassificationSortOrder;
        }

        @Override
        public int compare(String classification1, String classification2) {
            Integer sortOrder1 = securityClassificationSortOrder.get(normalize(classification1));
            Integer sortOrder2 = securityClassificationSortOrder.get(normalize(classification2));

            if (sortOrder1 == null) {
                sortOrder1 = Integer.MAX_VALUE;
            }

            if (sortOrder2 == null) {
                sortOrder2 = Integer.MAX_VALUE;
            }

            return sortOrder1.compareTo(sortOrder2);
        }
    }
}
