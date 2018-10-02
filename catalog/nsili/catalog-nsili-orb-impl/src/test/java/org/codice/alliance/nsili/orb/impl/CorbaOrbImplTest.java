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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.codice.alliance.nsili.orb.api.CorbaServiceListener;
import org.codice.alliance.nsili.orb.testing.TestPOA;
import org.junit.Test;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

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

  @Test
  public void testHostnameInOrb()
      throws InvalidName, AdapterInactive, ServantNotActive, WrongPolicy,
          UnsupportedEncodingException, DecoderException {
    POA rootPOA = null;
    org.omg.CORBA.Object testRef = null;

    CorbaOrbImpl corbaOrb = new CorbaOrbImpl();
    corbaOrb.setCorbaTimeout(100);
    corbaOrb.setCorbaPort(0);
    corbaOrb.init();
    ORB orb = corbaOrb.getOrb();

    rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

    rootPOA.the_POAManager().activate();

    TestPOA test = new TestImpl();

    testRef = rootPOA.servant_to_reference(test);

    String ior = orb.object_to_string(testRef);
    ior = ior.substring(4);
    byte[] bytes = Hex.decodeHex(ior.toCharArray());
    String asciiIOR = new String(bytes, "UTF-8");

    assertThat(
        "IOR contains localhost or 127.0.0.1",
        asciiIOR.contains("localhost") || asciiIOR.contains("127.0.0.1"));

    // Check that setting through the properties works
    Map<String, Object> props = new HashMap<>();
    props.put(CorbaOrbImpl.CORBA_TIMEOUT, 61);
    props.put(CorbaOrbImpl.CORBA_PORT, 0);
    props.put(CorbaOrbImpl.CORBA_HOST, "alpha.test.codice.org");
    corbaOrb.refresh(props);

    orb = corbaOrb.getOrb();
    rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
    rootPOA.the_POAManager().activate();

    test = new TestImpl();
    testRef = rootPOA.servant_to_reference(test);

    ior = orb.object_to_string(testRef);
    ior = ior.substring(4);
    bytes = Hex.decodeHex(ior.toCharArray());
    asciiIOR = new String(bytes, "UTF-8");

    assertThat("IOR contains alpha.test.codice.org", asciiIOR.contains("alpha.test.codice.org"));

    // Check that setting directly works
    corbaOrb.setCorbaPort(0);
    corbaOrb.setCorbaHost("bravo.test.codice.org");
    corbaOrb.init();

    orb = corbaOrb.getOrb();
    rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
    rootPOA.the_POAManager().activate();

    test = new TestImpl();
    testRef = rootPOA.servant_to_reference(test);

    ior = orb.object_to_string(testRef);
    ior = ior.substring(4);
    bytes = Hex.decodeHex(ior.toCharArray());
    asciiIOR = new String(bytes, "UTF-8");

    assertThat("IOR contains bravo.test.codice.org", asciiIOR.contains("bravo.test.codice.org"));
  }
}
