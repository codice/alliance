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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class SecurityClassificationServiceImplTest {

    @Test
    public void testSort() {
        SecurityClassificationServiceImpl service = new SecurityClassificationServiceImpl();
        service.setSortOrder(Arrays.asList("a", "b", "c"));
        List<String> in = Arrays.asList("a", "b", "c", "a", "b", "c", "a", "b", "c");
        Collections.sort(in, service.getSecurityClassificationComparator());
        assertThat(in, is(Arrays.asList("a", "a", "a", "b", "b", "b", "c", "c", "c")));
    }

    @Test
    public void testSortWithoutSortOrderEntry() {
        SecurityClassificationServiceImpl service = new SecurityClassificationServiceImpl();
        service.setSortOrder(Arrays.asList("a", "b"));
        List<String> in = Arrays.asList("a", "b", "c", "a", "b", "c", "a", "b", "c");
        Collections.sort(in, service.getSecurityClassificationComparator());
        assertThat(in, is(Arrays.asList("a", "a", "a", "b", "b", "b", "c", "c", "c")));
    }

    @Test
    public void testSetSortedOrder() {
        SecurityClassificationServiceImpl service = new SecurityClassificationServiceImpl();
        service.setSortOrder(Arrays.asList(" c ", "\t\tb ", "   a \n\n"));
        List<String> in = Arrays.asList("a", "b", "c", "a", "b", "c", "a", "b", "c");
        Collections.sort(in, service.getSecurityClassificationComparator());
        assertThat(in, is(Arrays.asList("c", "c", "c", "b", "b", "b", "a", "a", "a")));
    }

}
