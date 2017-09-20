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
package org.codice.alliance.video.stream.mpegts.rollover;

import static org.apache.commons.lang3.Validate.notNull;

import org.codice.alliance.video.stream.mpegts.netty.PacketBuffer;

/** Compute the boolean-or of two RolloverCondition objects. */
public class BooleanOrRolloverCondition implements RolloverCondition {

  private final RolloverCondition firstCondition;

  private final RolloverCondition secondCondition;

  /**
   * @param firstCondition must be non-null
   * @param secondCondition must be non-null
   */
  public BooleanOrRolloverCondition(
      RolloverCondition firstCondition, RolloverCondition secondCondition) {
    notNull(firstCondition, "firstCondition must be non-null");
    notNull(secondCondition, "secondCondition must be non-null");

    this.firstCondition = firstCondition;
    this.secondCondition = secondCondition;
  }

  @Override
  public boolean isRolloverReady(PacketBuffer packetBuffer) {
    notNull(packetBuffer, "packetBuffer must be non-null");

    return this.firstCondition.isRolloverReady(packetBuffer)
        || this.secondCondition.isRolloverReady(packetBuffer);
  }

  @Override
  public void accept(Visitor visitor) {
    notNull(visitor, "visitor must be non-null");

    firstCondition.accept(visitor);
    visitor.visit(this);
    secondCondition.accept(visitor);
  }

  @Override
  public String toString() {
    return "BooleanOrRolloverCondition{"
        + "firstCondition="
        + firstCondition
        + ", secondCondition="
        + secondCondition
        + '}';
  }
}
