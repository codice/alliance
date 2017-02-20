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
package org.codice.alliance.video.stream.mpegts.metacard;

import java.util.Collections;
import java.util.List;

import org.codice.alliance.video.stream.mpegts.Context;

import ddf.catalog.data.Metacard;

public class ListMetacardUpdater implements MetacardUpdater {

    private final List<MetacardUpdater> metacardUpdaterList;

    public ListMetacardUpdater(List<MetacardUpdater> metacardUpdaterList) {
        this.metacardUpdaterList = Collections.unmodifiableList(metacardUpdaterList);
    }

    @Override
    public void update(Metacard parent, Metacard child, Context context) {
        metacardUpdaterList.forEach(metacardUpdater -> metacardUpdater.update(parent, child, context));
    }

    @Override
    public String toString() {
        return "ListMetacardUpdater{" +
                "metacardUpdaterList=" + metacardUpdaterList +
                '}';
    }

}
