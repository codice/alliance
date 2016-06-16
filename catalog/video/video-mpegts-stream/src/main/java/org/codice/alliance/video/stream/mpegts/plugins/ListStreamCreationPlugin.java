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
package org.codice.alliance.video.stream.mpegts.plugins;

import java.util.List;

import org.codice.alliance.video.stream.mpegts.Context;

/**
 * Iterates through a list of StreamCreationPlugin and call onCreate for each.
 */
public class ListStreamCreationPlugin extends BaseStreamCreationPlugin {
    private final List<StreamCreationPlugin> streamCreationPluginList;

    public ListStreamCreationPlugin(List<StreamCreationPlugin> streamCreationPluginList) {
        this.streamCreationPluginList = streamCreationPluginList;
    }

    @Override
    protected void doOnCreate(Context context) throws StreamCreationException {
        for (StreamCreationPlugin streamCreationPlugin : streamCreationPluginList) {
            streamCreationPlugin.onCreate(context);
        }
    }
}
