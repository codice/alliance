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

import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.BasicTypes;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import org.codice.alliance.transformer.nitf.ExtNitfUtility;
import org.codice.imaging.nitf.core.tre.TreGroup;

public class IndexedPiaprdAttribute extends NitfAttributeImpl<TreGroup> {

  private static final List<NitfAttribute<TreGroup>> ATTRIBUTES = new LinkedList<>();

  private static final String PREFIX = ExtNitfUtility.EXT_NITF_PREFIX + "piaprd.";

  public static final String KEYWORD = PREFIX + "keyword";

  /*
   * Non-normalized attributes
   */
  public static final IndexedPiaprdAttribute KEYWORD_ATTRIBUTE =
      new IndexedPiaprdAttribute(
          KEYWORD,
          "KEYWORD",
          tre -> TreUtility.getTreValue(tre, "KEYWORD"),
          BasicTypes.STRING_TYPE);

  private IndexedPiaprdAttribute(
      String longName,
      String shortName,
      Function<TreGroup, Serializable> accessorFunction,
      AttributeType attributeType) {
    super(longName, shortName, accessorFunction, attributeType);
    ATTRIBUTES.add(this);
  }

  public static List<NitfAttribute<TreGroup>> getAttributes() {
    return Collections.unmodifiableList(ATTRIBUTES);
  }
}
