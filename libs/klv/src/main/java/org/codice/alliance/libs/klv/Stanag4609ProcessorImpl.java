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
package org.codice.alliance.libs.klv;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codice.alliance.libs.stanag4609.DecodedKLVMetadataPacket;
import org.codice.ddf.libs.klv.KlvContext;
import org.codice.ddf.libs.klv.KlvDataElement;
import org.codice.ddf.libs.klv.data.set.KlvLocalSet;

public class Stanag4609ProcessorImpl implements Stanag4609Processor {

  private PostProcessor postProcessor;

  public Stanag4609ProcessorImpl(PostProcessor postProcessor) {
    this.postProcessor = postProcessor;
  }

  /**
   * Iterate through the STANAG 4609 metadata and pass each {@link DecodedKLVMetadataPacket} to
   * {@link #handle(Map, KlvHandler, KlvContext, Map)}.
   *
   * @param handlers map of klv handers
   * @param stanagMetadata list of klv metadata packets
   */
  @Override
  public void handle(
      Map<String, KlvHandler> handlers,
      KlvHandler defaultHander,
      Map<Integer, List<DecodedKLVMetadataPacket>> stanagMetadata) {

    stanagMetadata.values().stream()
        .flatMap(List::stream)
        .forEach(
            decodedKLVMetadataPacket -> {
              Map<String, KlvDataElement> dataElements = new HashMap<>();

              handle(
                  handlers, defaultHander, decodedKLVMetadataPacket.getDecodedKLV(), dataElements);

              postProcessor.postProcess(dataElements, handlers);
            });
  }

  /**
   * Pass each KlvDataElement in the klvContext to {@link #handle(Map, KlvHandler, KlvDataElement,
   * Map)}.
   *
   * @param handlers map of klv handers
   * @param klvContext klv context
   */
  @Override
  public void handle(
      Map<String, KlvHandler> handlers,
      KlvHandler defaultHandler,
      KlvContext klvContext,
      Map<String, KlvDataElement> dataElements) {
    klvContext
        .getDataElements()
        .values()
        .forEach(klvDataElement -> handle(handlers, defaultHandler, klvDataElement, dataElements));
  }

  /**
   * If klvDataElement is a KlvLocalSet, then pass it to {@link #handle(Map, KlvHandler,
   * KlvLocalSet, Map)}. Otherwise, call the corresponding data element handler.
   *
   * @param handlers map of klv handers
   * @param klvDataElement klv data element
   */
  @Override
  public void handle(
      Map<String, KlvHandler> handlers,
      KlvHandler defaultHandler,
      KlvDataElement klvDataElement,
      Map<String, KlvDataElement> dataElements) {
    if (klvDataElement instanceof KlvLocalSet) {
      handle(handlers, defaultHandler, (KlvLocalSet) klvDataElement, dataElements);
    } else if (!klvDataElement.isErrorIndicated()) {
      callDataElementHandlers(handlers, defaultHandler, klvDataElement, dataElements);
    }
  }

  /**
   * Find and call the handler based on the name of the klvDataElement, otherwise use the default
   * handler.
   *
   * @param handlers map of klv handers
   * @param klvDataElement klv data element
   */
  @Override
  public void callDataElementHandlers(
      Map<String, KlvHandler> handlers,
      KlvHandler defaultHandler,
      KlvDataElement klvDataElement,
      Map<String, KlvDataElement> dataElements) {
    handlers.getOrDefault(klvDataElement.getName(), defaultHandler).accept(klvDataElement);
    dataElements.put(klvDataElement.getName(), klvDataElement);
  }

  /**
   * Pass the KlvContext within klvLocalSet to {@link #handle(Map, KlvHandler, KlvContext, Map)}.
   *
   * @param handlers map of klv handers
   * @param klvLocalSet klv local set
   */
  @Override
  public void handle(
      Map<String, KlvHandler> handlers,
      KlvHandler defaultHandler,
      KlvLocalSet klvLocalSet,
      Map<String, KlvDataElement> dataElements) {
    handle(handlers, defaultHandler, klvLocalSet.getValue(), dataElements);
  }
}
