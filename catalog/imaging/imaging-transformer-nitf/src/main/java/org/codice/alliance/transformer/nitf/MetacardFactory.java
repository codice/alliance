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
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.types.Core;
import ddf.catalog.data.types.Media;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetacardFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetacardFactory.class);

  public static final MimeType MIME_TYPE;

  static final String MIME_TYPE_STRING = "image/nitf";

  private static final String ID = "ddf/catalog/transformer/nitf";

  private static final String TO_STRING_PATTERN = "InputTransformer {Impl=%s, id=%s, mime-type=%s}";

  static {
    try {
      MIME_TYPE = new MimeType(MIME_TYPE_STRING);
    } catch (MimeTypeParseException e) {
      throw new ExceptionInInitializerError(
          String.format(
              "unable to create MimeType from '%s': %s", MIME_TYPE_STRING, e.getMessage()));
    }
  }

  private MetacardType metacardType;

  public Metacard createMetacard(String id) {
    Metacard metacard = new MetacardImpl(metacardType);
    metacard.setAttribute(new AttributeImpl(Media.TYPE, MIME_TYPE.toString()));
    metacard.setAttribute(new AttributeImpl(Metacard.CONTENT_TYPE, MIME_TYPE.toString()));

    LOGGER.trace(
        "Setting the metacard attribute [{}, {}], [{}, {}]",
        Media.TYPE,
        MIME_TYPE,
        Metacard.CONTENT_TYPE,
        MIME_TYPE);

    if (id != null) {
      metacard.setAttribute(new AttributeImpl(Core.ID, id));
    }

    return metacard;
  }

  public void setMetacardType(MetacardType metacardType) {
    this.metacardType = metacardType;
  }

  @Override
  public String toString() {
    return String.format(TO_STRING_PATTERN, this.getClass().getName(), ID, MIME_TYPE);
  }
}
