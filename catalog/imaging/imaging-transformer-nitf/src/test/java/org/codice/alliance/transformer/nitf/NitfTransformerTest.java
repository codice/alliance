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
package org.codice.alliance.transformer.nitf;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import ddf.catalog.data.Metacard;
import ddf.catalog.transform.CatalogTransformerException;
import java.io.InputStream;
import org.junit.Test;

public class NitfTransformerTest {

  private final NitfTransformer nitfTransformer = new NitfTransformer();

  @Test(expected = CatalogTransformerException.class)
  public void testChannelNullInput() throws Exception {
    nitfTransformer.transform(null);
  }

  @Test
  public void imageNoTresNitf() throws Exception {
    InputStream nitfStream = getClass().getClassLoader().getResourceAsStream("imageNoTre.ntf");
    Metacard metacard = nitfTransformer.transform(nitfStream);

    assertThat(metacard.getMetacardType().getName(), is("isr.image"));
  }

  @Test
  public void imageTreNitf() throws Exception {
    InputStream nitfStream = getClass().getClassLoader().getResourceAsStream("imageTre.ntf");
    Metacard metacard = nitfTransformer.transform(nitfStream);

    assertThat(metacard.getMetacardType().getName(), is("isr.image"));
  }

  @Test
  public void noImageTreNitf() throws Exception {
    InputStream nitfStream = getClass().getClassLoader().getResourceAsStream("noImageTre.ntf");
    Metacard metacard = nitfTransformer.transform(nitfStream);

    assertThat(metacard.getMetacardType().getName(), is("isr.gmti"));
  }

  @Test
  public void noImageNoTreNitf() throws Exception {
    InputStream nitfStream = getClass().getClassLoader().getResourceAsStream("noImageNoTre.ntf");
    Metacard metacard = nitfTransformer.transform(nitfStream);

    assertThat(metacard.getMetacardType().getName(), is("isr.image"));
  }

  @Test
  public void gmtiNitf() throws Exception {
    InputStream nitfStream = getClass().getClassLoader().getResourceAsStream("gmti-test.ntf");
    Metacard metacard = nitfTransformer.transform(nitfStream);

    assertThat(metacard.getMetacardType().getName(), is("isr.gmti"));
  }
}
