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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Supplier;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

public class TestDateTemplateFilenameGenerator {

    private Date date;

    private DateTemplateFilenameGenerator templateFilenameGenerator;

    @Before
    public void setup() {
        date = new Date();
        Supplier<Date> dateSupplier = mock(Supplier.class);
        when(dateSupplier.get()).thenReturn(date);
        templateFilenameGenerator = new DateTemplateFilenameGenerator();
        templateFilenameGenerator.setDateSupplier(dateSupplier);
    }

    @Test
    public void test() {

        String fmt = "yyyy-MM-dd_hh:mm:ss";

        String filename = templateFilenameGenerator.generateFilename(
                "mpegts-stream-%{date=" + fmt + "}.ts");

        SimpleDateFormat sdf = new SimpleDateFormat(fmt);

        String expected = "mpegts-stream-" + sdf.format(date) + ".ts";

        assertThat(filename, is(expected));
    }

    @Test
    public void testToString() {
        assertThat(templateFilenameGenerator.toString(), notNullValue());
    }

    @Test
    public void testMultiplePatterns() {

        String fmt1 = "yyyy-MM-dd";
        String fmt2 = "hh:mm:ss";

        String filename = templateFilenameGenerator.generateFilename(
                "mpegts-stream-%{date=" + fmt1 + "}-xyz-%{date=" + fmt2 + "}.ts");

        SimpleDateFormat sdf1 = new SimpleDateFormat(fmt1);
        SimpleDateFormat sdf2 = new SimpleDateFormat(fmt2);

        String expected =
                "mpegts-stream-" + sdf1.format(date) + "-xyz-" + sdf2.format(date) + ".ts";

        assertThat(filename, is(expected));

    }

    public static class TestFileExtensionFilenameGenerator {

        private static final String EXT = "xyz";

        private FileExtensionFilenameGenerator fileExtensionFilenameGenerator;

        @Before
        public void setup() {
            fileExtensionFilenameGenerator = new FileExtensionFilenameGenerator(EXT);
        }

        @Test
        public void testGenerateFilenameAddingExt() {
            assertThat(fileExtensionFilenameGenerator.generateFilename("a"),
                    Matchers.is("a." + EXT));
        }

        @Test
        public void testGenerateFilenameExtAlreadyPresent() {
            assertThat(fileExtensionFilenameGenerator.generateFilename("a." + EXT),
                    Matchers.is("a." + EXT));
        }

        @Test
        public void testGenerateFilenameMissingDot() {
            assertThat(fileExtensionFilenameGenerator.generateFilename("a" + EXT),
                    Matchers.is("a" + EXT + "." + EXT));
        }

        @Test
        public void testToString() {
            assertThat(fileExtensionFilenameGenerator.toString(), Matchers.notNullValue());
        }
    }
}
