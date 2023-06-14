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

import ddf.catalog.data.Metacard;
import ddf.catalog.transform.CatalogTransformerException;
import ddf.catalog.transform.InputTransformer;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.codice.alliance.transformer.nitf.common.NitfHeaderTransformer;
import org.codice.alliance.transformer.nitf.gmti.GmtiMetacardType;
import org.codice.alliance.transformer.nitf.gmti.NitfGmtiTransformer;
import org.codice.alliance.transformer.nitf.image.ImageMetacardType;
import org.codice.alliance.transformer.nitf.image.NitfImageTransformer;
import org.codice.imaging.nitf.core.DataSource;
import org.codice.imaging.nitf.core.HeapStrategy;
import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.common.impl.NitfInputStreamReader;
import org.codice.imaging.nitf.core.header.NitfHeader;
import org.codice.imaging.nitf.core.header.impl.NitfParser;
import org.codice.imaging.nitf.core.impl.InMemoryHeapStrategy;
import org.codice.imaging.nitf.core.impl.SlottedParseStrategy;
import org.codice.imaging.nitf.core.tre.Tre;
import org.codice.imaging.nitf.core.tre.TreCollection;

public class NitfTransformer implements InputTransformer {

  private final MetacardFactory imageMetacardFactory = new MetacardFactory(new ImageMetacardType());

  private final MetacardFactory gmtiMetacardFactory = new MetacardFactory(new GmtiMetacardType());

  @Override
  public Metacard transform(InputStream inputStream)
      throws IOException, CatalogTransformerException {
    if (inputStream == null) {
      throw new CatalogTransformerException("Cannot transform null input stream.");
    }

    try {
      NitfInputStreamReader nitfReader = new NitfInputStreamReader(inputStream);

      HeapStrategy<ImageInputStream> imageDataStrategy =
          new InMemoryHeapStrategy(this::createMemoryCacheImageInputStream);

      SlottedParseStrategy parseStrategy = new SlottedParseStrategy();
      parseStrategy.setImageHeapStrategy(imageDataStrategy);

      NitfParser.parse(nitfReader, parseStrategy);

      DataSource nitfDataSource = parseStrategy.getDataSource();
      NitfHeader nitfHeaders = nitfDataSource.getNitfHeader();

      Metacard metacard;
      if (isGmtiRoute(nitfDataSource, nitfHeaders)) {
        metacard = gmtiMetacardFactory.createMetacard(null);

        NitfHeaderTransformer segmentHandler = new NitfHeaderTransformer();
        segmentHandler.transform(nitfHeaders, metacard);

        NitfGmtiTransformer gmtiTransformer = new NitfGmtiTransformer();
        gmtiTransformer.transform(metacard);
      } else {
        metacard = imageMetacardFactory.createMetacard(null);

        NitfHeaderTransformer segmentHandler = new NitfHeaderTransformer();
        segmentHandler.transform(nitfHeaders, metacard);

        NitfImageTransformer imageTransformer = new NitfImageTransformer();
        imageTransformer.transform(nitfDataSource, metacard);

        NitfGmtiTransformer gmtiTransformer = new NitfGmtiTransformer();
        gmtiTransformer.transform(metacard);
      }

      imageDataStrategy.cleanUp();

      return metacard;
    } catch (NitfFormatException e) {
      throw new CatalogTransformerException(e);
    }
  }

  private Object createMemoryCacheImageInputStream(Object o) {
    return new MemoryCacheImageInputStream((InputStream) o);
  }

  @Override
  public Metacard transform(InputStream inputStream, String s)
      throws IOException, CatalogTransformerException {
    return null;
  }

  private boolean isGmtiRoute(DataSource dataSource, NitfHeader headers) {
    boolean tresExist = tresExist(headers);
    boolean imagesExist = dataSource.getImageSegments().size() > 0;

    return !imagesExist && tresExist;
  }

  private boolean tresExist(NitfHeader header) {
    TreCollection treCollection = header.getTREsRawStructure();
    List<Tre> mtirpbList = treCollection.getTREsWithName("MTIRPB");
    return !mtirpbList.isEmpty();
  }
}
