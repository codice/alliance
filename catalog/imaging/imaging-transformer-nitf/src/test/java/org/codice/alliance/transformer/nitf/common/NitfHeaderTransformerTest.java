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
package org.codice.alliance.transformer.nitf.common;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import ddf.catalog.data.Metacard;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.function.Supplier;
import org.codice.alliance.catalog.core.api.types.Security;
import org.codice.alliance.transformer.nitf.MetacardFactory;
import org.codice.alliance.transformer.nitf.TreUtilityTest;
import org.codice.alliance.transformer.nitf.image.ImageMetacardType;
import org.codice.imaging.nitf.core.header.NitfHeader;
import org.codice.imaging.nitf.core.security.FileSecurityMetadata;
import org.codice.imaging.nitf.fluent.NitfSegmentsFlow;
import org.codice.imaging.nitf.fluent.impl.NitfCreationFlowImpl;
import org.codice.imaging.nitf.fluent.impl.NitfParserInputFlowImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class NitfHeaderTransformerTest {

  private NitfHeaderTransformer nitfHeaderTransformer;

  private MetacardFactory metacardFactory;

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void setUp() {
    nitfHeaderTransformer = new NitfHeaderTransformer();

    metacardFactory = new MetacardFactory();
    metacardFactory.setMetacardType(new ImageMetacardType());
  }

  @Test
  public void testValidationWarningsOnNitfAttributeTransformException() throws Exception {
    String originalNitfValue = "US";
    File nitfFile = temporaryFolder.newFile("nitf-attribute-header-test.ntf");

    FileSecurityMetadata fileSecurityMetadata = TreUtilityTest.createSecurityMetadata();
    when(fileSecurityMetadata.getSecurityClassificationSystem()).thenReturn(originalNitfValue);

    Supplier<NitfHeader> nitfHeader = () -> TreUtilityTest.createFileHeader(fileSecurityMetadata);
    new NitfCreationFlowImpl().fileHeader(nitfHeader).write(nitfFile.getAbsolutePath());

    Metacard metacard;

    try (InputStream inputStream = new FileInputStream(nitfFile)) {
      metacard = metacardFactory.createMetacard("nitfHeaderTest");
      NitfSegmentsFlow nitfSegmentsFlow =
          new NitfParserInputFlowImpl().inputStream(inputStream).headerOnly();
      nitfHeaderTransformer.transform(nitfSegmentsFlow, metacard);
    }

    assertThat(metacard, is(not(nullValue())));
    assertThat(
        metacard.getAttribute("ext.nitf.file-classification-security-system").getValue(),
        is(originalNitfValue));
    assertThat(metacard.getAttribute(Security.CLASSIFICATION_SYSTEM).getValue(), is("USA"));
  }
}
