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

import ddf.catalog.content.data.ContentItem;
import ddf.catalog.data.BinaryContent;
import ddf.catalog.data.Metacard;
import ddf.catalog.transform.CatalogTransformerException;
import ddf.catalog.transform.MetacardTransformer;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OverviewSupplier
    implements BiFunction<Metacard, Map<String, Serializable>, Optional<BufferedImage>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(OverviewSupplier.class);

  private final MetacardTransformer resourceMetacardTransformer;

  public OverviewSupplier(MetacardTransformer resourceMetacardTransformer) {
    this.resourceMetacardTransformer = resourceMetacardTransformer;
  }

  @Override
  public Optional<BufferedImage> apply(Metacard metacard, Map<String, Serializable> arguments) {
    try {
      final Map<String, Serializable> resourceTransformerArguments = new HashMap<>();
      resourceTransformerArguments.put(ContentItem.QUALIFIER_KEYWORD, "overview");
      final BinaryContent overviewContent =
          resourceMetacardTransformer.transform(metacard, resourceTransformerArguments);
      try (final InputStream inputStream = overviewContent.getInputStream()) {
        return Optional.ofNullable(ImageIO.read(inputStream));
      }
    } catch (IOException | CatalogTransformerException e) {
      LOGGER.debug("Could not get the overview image.", e);
    }

    return Optional.empty();
  }
}
