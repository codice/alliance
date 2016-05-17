/**
 * Copyright (c) Connexta, LLC
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
package com.connexta.alliance.video.stream.mpegts.netty;

import java.util.LinkedList;
import java.util.List;

import io.netty.channel.embedded.EmbeddedChannel;

public class NettyUtility {

    public static List<Object> read(EmbeddedChannel channel) {
        List<Object> output = new LinkedList<>();
        Object inbound;
        while ((inbound = channel.readInbound()) != null) {
            output.add(inbound);
        }
        return output;
    }

}
