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
package org.codice.alliance.video.stream.mpegts.klv;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class KlvServiceTest {

  @Test
  public void postKlvWithNoSource() {
    KlvService service = new KlvService();
    Map<String, String> props = new HashMap<>();
    props.put(KlvData.PARENT_ID_KEY, "parentId");
    service.postKlvForStream(new KlvData(new HashMap<>(), props));
    assertThat(service.getStreamsKlvByParam(KlvData.PARENT_ID_KEY, "parentId"), notNullValue());
    Collection<KlvData> klvData = service.getAllStreamsKlvWithParam("sourceId");
    assertThat(klvData.size(), is(0));
  }

  @Test
  public void postKlvWithSource() {
    KlvService service = new KlvService();
    Map<String, String> props = new HashMap<>();
    props.put(KlvData.PARENT_ID_KEY, "parentId");
    props.put("sourceId", "sourceId");
    service.postKlvForStream(new KlvData(new HashMap<>(), props));
    assertThat(service.getStreamsKlvByParam(KlvData.PARENT_ID_KEY, "parentId"), notNullValue());
    assertThat(service.getStreamsKlvByParam("sourceId", "sourceId"), notNullValue());
  }

  @Test
  public void itemsTimeout() throws Exception {
    KlvService service = new KlvService();
    Map<String, String> props = new HashMap<>();
    props.put(KlvData.PARENT_ID_KEY, "parentId");
    service.postKlvForStream(new KlvData(new HashMap<>(), props));
    assertThat(service.getStreamsKlvByParam(KlvData.PARENT_ID_KEY, "parentId"), notNullValue());
    Thread.sleep(2000);
    assertThat(service.getStreamsKlvByParam(KlvData.PARENT_ID_KEY, "parentId"), notNullValue());
  }
}
