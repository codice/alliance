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
package org.codice.alliance.nsili.transformer;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.codice.alliance.nsili.common.CorbaUtils;
import org.omg.CORBA.Any;

/** A {@link Converter} for serializing the {@link Any} object's {@code value} to XML. */
public class AnyConverter implements Converter {

  @Override
  public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext context) {
    String value =
        CorbaUtils.getNodeValue((Any) o) != null ? CorbaUtils.getNodeValue((Any) o) : "null";
    writer.setValue(value);
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    return null;
  }

  @Override
  public boolean canConvert(Class clazz) {
    return Any.class.isAssignableFrom(clazz);
  }
}
