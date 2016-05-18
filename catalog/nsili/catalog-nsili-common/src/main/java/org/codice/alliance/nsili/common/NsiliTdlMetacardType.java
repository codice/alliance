/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.nsili.common;

import java.util.HashSet;
import java.util.Set;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;

public class NsiliTdlMetacardType extends NsiliMetacardType {
    public static final String METACARD_TYPE_NAME = NSILI_METACARD_TYPE_PREFIX +".tdl."+ NSILI_METACARD_TYPE_POSTFIX;

    /**
     * A number that together with the 'platform' number defines the identity of a track
     */
    public static final String ACTIVITY = "activity";

    /**
     * The Link 16 J Series message number.
     */
    public static final String MESSAGE_NUM = "messageNumber";

    /**
     * A number that together with the 'activity' number defines the identity of a track.
     */
    public static final String PLATFORM = "platform";

    /**
     * Link 16 J Series track number for the track found in the product.
     * The track number shall be in the decoded 5-character format (e.g. EK627).
     */
    public static final String TRACK_NUM = "trackNumber";

    private static final Set<AttributeDescriptor> NSILI_TDL_DESCRIPTORS = new HashSet<>();

    static {
        NSILI_TDL_DESCRIPTORS.add(new AttributeDescriptorImpl(ACTIVITY,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.INTEGER_TYPE));

        NSILI_TDL_DESCRIPTORS.add(new AttributeDescriptorImpl(MESSAGE_NUM,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_TDL_DESCRIPTORS.add(new AttributeDescriptorImpl(PLATFORM,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.INTEGER_TYPE));

        NSILI_TDL_DESCRIPTORS.add(new AttributeDescriptorImpl(TRACK_NUM,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));
    }

    public NsiliTdlMetacardType() {
        super();
        attributeDescriptors.addAll(NsiliTdlMetacardType.NSILI_TDL_DESCRIPTORS);
    }

    @Override
    public String getName() {
        return METACARD_TYPE_NAME;
    }
}
