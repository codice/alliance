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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.codice.alliance.video.stream.mpegts.netty.PacketBuffer;
import org.junit.Before;
import org.junit.Test;

public class ElapsedTimeRolloverConditionTest {

  private static final long THRESHOLD = 100;

  private ElapsedTimeRolloverCondition elapsedTimeRolloverCondition;

  private PacketBuffer packetBuffer;

  @Before
  public void setup() {
    elapsedTimeRolloverCondition = new ElapsedTimeRolloverCondition(THRESHOLD);
    packetBuffer = mock(PacketBuffer.class);
  }

  @Test
  public void testLessThanThreshold() {
    when(packetBuffer.getAge()).thenReturn(THRESHOLD - 1);
    assertThat(elapsedTimeRolloverCondition.isRolloverReady(packetBuffer), is(false));
  }

  @Test
  public void testEqualToThreshold() {
    when(packetBuffer.getAge()).thenReturn(THRESHOLD);
    assertThat(elapsedTimeRolloverCondition.isRolloverReady(packetBuffer), is(true));
  }

  @Test
  public void testGreaterThanThreshold() {
    when(packetBuffer.getAge()).thenReturn(THRESHOLD + 1);
    assertThat(elapsedTimeRolloverCondition.isRolloverReady(packetBuffer), is(true));
  }

  @Test
  public void testToString() {
    assertThat(elapsedTimeRolloverCondition.toString(), notNullValue());
  }

  @Test
  public void testAcceptVisitor() {
    RolloverCondition.Visitor visitor = mock(RolloverCondition.Visitor.class);
    elapsedTimeRolloverCondition.accept(visitor);
    verify(visitor).visit(elapsedTimeRolloverCondition);
  }

  @Test
  public void testSetter() {
    long value = THRESHOLD - 1;
    elapsedTimeRolloverCondition.setElapsedTimeThreshold(value);
    assertThat(elapsedTimeRolloverCondition.getElapsedTimeThreshold(), is(value));
  }
}
