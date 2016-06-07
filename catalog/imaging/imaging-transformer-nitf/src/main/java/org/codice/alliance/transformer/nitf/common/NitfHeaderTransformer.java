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
package org.codice.alliance.transformer.nitf.common;

import java.io.IOException;
import java.util.Date;

import org.codice.imaging.nitf.core.header.NitfHeader;
import org.codice.imaging.nitf.fluent.NitfSegmentsFlow;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;

public class NitfHeaderTransformer extends SegmentHandler {

    private static final String NULL_ARGUMENT_MESSAGE = "Cannot transform null input.";

    public NitfSegmentsFlow transform(NitfSegmentsFlow nitfSegmentsFlow, Metacard metacard)
            throws IOException {
        if (nitfSegmentsFlow == null) {
            throw new IllegalArgumentException(NULL_ARGUMENT_MESSAGE);
        }

        nitfSegmentsFlow.fileHeader(header -> handleNitfHeader(metacard, header));
        return nitfSegmentsFlow;
    }

    private void handleNitfHeader(Metacard metacard, NitfHeader header) {
        Date date = (Date) NitfHeaderAttribute.FILE_DATE_AND_TIME.getAccessorFunction()
                .apply(header);

        metacard.setAttribute(new AttributeImpl(Metacard.TITLE, header.getFileTitle()));
        metacard.setAttribute(new AttributeImpl(Metacard.MODIFIED, date));
        metacard.setAttribute(new AttributeImpl(Metacard.CREATED, date));
        metacard.setAttribute(new AttributeImpl(Metacard.EFFECTIVE, date));
        handleSegmentHeader(metacard, header, NitfHeaderAttribute.values());
    }
}
