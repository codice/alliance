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
package org.codice.alliance.video.stream.mpegts.filename;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class ListFilenameGeneratorTest {

  @Test
  public void testGenerateFilename() {
    String testString = "foo";
    FilenameGenerator filenameGenerator1 = mock(FilenameGenerator.class);
    when(filenameGenerator1.generateFilename(testString)).thenReturn(testString);
    FilenameGenerator filenameGenerator2 = mock(FilenameGenerator.class);
    when(filenameGenerator2.generateFilename(testString)).thenReturn(testString);
    ListFilenameGenerator listFilenameGenerator =
        new ListFilenameGenerator(Arrays.asList(filenameGenerator1, filenameGenerator2));
    assertThat(listFilenameGenerator.generateFilename(testString), is(testString));
    verify(filenameGenerator1).generateFilename(testString);
    verify(filenameGenerator2).generateFilename(testString);
  }

  @Test
  public void testToString() {
    ListFilenameGenerator listFilenameGenerator =
        new ListFilenameGenerator(Collections.emptyList());
    assertThat(listFilenameGenerator.toString(), notNullValue());
  }
}
