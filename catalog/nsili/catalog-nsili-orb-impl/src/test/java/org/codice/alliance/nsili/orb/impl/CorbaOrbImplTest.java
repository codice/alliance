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
package org.codice.alliance.nsili.orb.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;

import java.util.HashMap;
import java.util.Map;
import org.codice.alliance.nsili.orb.api.CorbaServiceListener;
import org.junit.Test;
import org.omg.CORBA.ORB;

public class CorbaOrbImplTest {

  public int listenerCallCounter = 0;

  @Test
  public void testGetOrb() {
    CorbaOrbImpl corbaOrb = new CorbaOrbImpl();
    corbaOrb.setCorbaTimeout(100);
    corbaOrb.setCorbaPort(0);
    corbaOrb.init();
    ORB orb = corbaOrb.getOrb();
    assertThat(orb, notNullValue());
    corbaOrb.shutdown();
  }

  @Test
  public void testPropRefresh() {
    CorbaOrbImpl corbaOrb = new CorbaOrbImpl();
    corbaOrb.setCorbaTimeout(100);
    corbaOrb.setCorbaPort(20000);
    corbaOrb.init();
    ORB orb = corbaOrb.getOrb();
    assertThat(orb, notNullValue());

    int port = corbaOrb.getCorbaPort();
    assertThat(port, is(20000));

    Map<String, Object> props = new HashMap<>();
    props.put(CorbaOrbImpl.CORBA_PORT, "20001");
    props.put(CorbaOrbImpl.CORBA_TIMEOUT, 61);
    corbaOrb.refresh(props);
    orb = corbaOrb.getOrb();
    assertThat(orb, notNullValue());
  }

  @Test
  public void testEmptyPropRefresh() {
    CorbaOrbImpl corbaOrb = new CorbaOrbImpl();
    corbaOrb.setCorbaTimeout(100);
    corbaOrb.setCorbaPort(0);
    corbaOrb.init();
    ORB orb = corbaOrb.getOrb();
    assertThat(orb, notNullValue());

    Map<String, Object> props = new HashMap<>();
    corbaOrb.refresh(props);
    orb = corbaOrb.getOrb();
    assertThat(orb, notNullValue());
  }

  @Test
  public void testListener() {
    CorbaServiceListener listener =
        new CorbaServiceListener() {
          @Override
          public void corbaInitialized() {
            listenerCallCounter++;
          }

          @Override
          public void corbaShutdown() {
            listenerCallCounter++;
          }
        };

    listenerCallCounter = 0;

    CorbaOrbImpl corbaOrb = new CorbaOrbImpl();
    corbaOrb.setCorbaTimeout(100);
    corbaOrb.setCorbaPort(0);
    corbaOrb.init();
    ORB orb = corbaOrb.getOrb();
    assertThat(orb, notNullValue());

    corbaOrb.addCorbaServiceListener(listener);

    Map<String, Object> props = new HashMap<>();
    props.put(CorbaOrbImpl.CORBA_TIMEOUT, 61);
    corbaOrb.refresh(props);
    orb = corbaOrb.getOrb();
    assertThat(orb, notNullValue());
    assertThat(listenerCallCounter, is(2));

    props = new HashMap<>();
    props.put(CorbaOrbImpl.CORBA_TIMEOUT, 61);
    corbaOrb.refresh(props);
    orb = corbaOrb.getOrb();
    assertThat(orb, notNullValue());
    assertThat(listenerCallCounter, is(4));

    corbaOrb.setCorbaTimeout(61);
    orb = corbaOrb.getOrb();
    assertThat(orb, notNullValue());
    assertThat(listenerCallCounter, is(4));

    corbaOrb.removeCorbaServiceListener(listener);

    props = new HashMap<>();
    props.put(CorbaOrbImpl.CORBA_TIMEOUT, 63);
    corbaOrb.refresh(props);
    orb = corbaOrb.getOrb();
    assertThat(orb, notNullValue());
    assertThat(listenerCallCounter, is(4));
  }
}
