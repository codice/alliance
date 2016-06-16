/**
 * Copyright (c) Codice Foundation
 * <p/>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.video.stream.mpegts.rollover;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.codice.alliance.video.stream.mpegts.netty.PacketBuffer;
import org.junit.Before;
import org.junit.Test;

public class TestBooleanOrRolloverCondition {

    private BooleanOrRolloverCondition firstCondition;

    private BooleanOrRolloverCondition secondCondition;

    private BooleanOrRolloverCondition condition;

    @Before
    public void setup() {
        firstCondition = mock(BooleanOrRolloverCondition.class);
        secondCondition = mock(BooleanOrRolloverCondition.class);
        condition = new BooleanOrRolloverCondition(firstCondition, secondCondition);
    }

    private void assertThatBooleanOr(boolean x, boolean y, boolean expected) {

        when(firstCondition.isRolloverReady(any())).thenReturn(x);
        when(secondCondition.isRolloverReady(any())).thenReturn(y);

        assertThat(condition.isRolloverReady(mock(PacketBuffer.class)), is(expected));
    }

    @Test
    public void testBasicLogic() {
        assertThatBooleanOr(false, false, false);
        assertThatBooleanOr(false, true, true);
        assertThatBooleanOr(true, false, true);
        assertThatBooleanOr(true, true, true);
    }

    @Test
    public void testToString() {
        assertThat(condition.toString(), not(nullValue()));
    }

    @Test
    public void testAccept() {

        RolloverCondition.Visitor visitor = mock(RolloverCondition.Visitor.class);

        condition.accept(visitor);

        verify(firstCondition).accept(visitor);
        verify(secondCondition).accept(visitor);

        verify(visitor).visit(condition);

    }

}
