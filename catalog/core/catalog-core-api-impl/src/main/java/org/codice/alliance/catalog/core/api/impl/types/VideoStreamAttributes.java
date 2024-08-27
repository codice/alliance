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
package org.codice.alliance.catalog.core.api.impl.types;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.codice.alliance.catalog.core.api.types.VideoStream;

public class VideoStreamAttributes implements MetacardType {

  private static final Set<AttributeDescriptor> DESCRIPTORS;

  static {
    final Set<AttributeDescriptor> descriptors = new HashSet<>();
    descriptors.add(
        new AttributeDescriptorImpl(
            VideoStream.STREAM_ID, true, true, false, false, BasicTypes.STRING_TYPE));
    descriptors.add(
        new AttributeDescriptorImpl(
            VideoStream.CONTAINER, true, true, false, false, BasicTypes.BOOLEAN_TYPE));
    descriptors.add(
        new AttributeDescriptorImpl(
            VideoStream.RECORDING_ID, true, true, false, false, BasicTypes.STRING_TYPE));
    descriptors.add(
        new AttributeDescriptorImpl(
            VideoStream.SEGMENT_IDS, true, true, false, true, BasicTypes.STRING_TYPE));
    DESCRIPTORS = Collections.unmodifiableSet(descriptors);
  }

  @Override
  public String getName() {
    return "video-stream";
  }

  @Override
  public Set<AttributeDescriptor> getAttributeDescriptors() {
    return DESCRIPTORS;
  }

  @Override
  public AttributeDescriptor getAttributeDescriptor(final String attributeName) {
    return DESCRIPTORS.stream()
        .filter(attr -> attr.getName().equals(attributeName))
        .findFirst()
        .orElse(null);
  }
}
