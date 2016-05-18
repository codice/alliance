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
package org.codice.alliance.video.stream.mpegts.filename;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class TestIllegalCharactersFilenameGenerator {

    private IllegalCharactersFilenameGenerator illegalCharactersFilenameGenerator;

    @Before
    public void setup() {
        illegalCharactersFilenameGenerator = new IllegalCharactersFilenameGenerator();
    }

    @Test
    public void testRemoveIllegalCharacters() {
        assertThat(illegalCharactersFilenameGenerator.generateFilename("/x/y/"), is("xy"));
        assertThat(illegalCharactersFilenameGenerator.generateFilename(":x:y:"), is("xy"));
        assertThat(illegalCharactersFilenameGenerator.generateFilename("/:x:y:/"), is("xy"));
    }

    @Test
    public void testToString() {
        assertThat(illegalCharactersFilenameGenerator.toString(), notNullValue());
    }
}
