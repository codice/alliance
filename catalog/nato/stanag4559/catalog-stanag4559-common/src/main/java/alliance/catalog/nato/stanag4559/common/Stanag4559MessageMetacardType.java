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
package alliance.catalog.nato.stanag4559.common;

import java.util.HashSet;
import java.util.Set;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;

public class Stanag4559MessageMetacardType extends Stanag4559MetacardType {
    public static final String METACARD_TYPE_NAME = STANAG4559_METACARD_TYPE_PREFIX+".message."+STANAG4559_METACARD_TYPE_POSTFIX;

    /**
     * Identify the receiving entity of the message. This will typically be an XMPP conference room
     * identifier (including the fully qualified domain name extension), but could also be an
     * individual/personal XMPP or e- mail account. In the case a message is sent to more than one
     * recipient, this shall be supported by separating the recipients by BCS Comma.
     */
    public static final String RECIPIENT = "recipient";

    /**
     * Identification of message type (protocol).
     */
    public static final String MESSAGE_TYPE = "messageType";

    private static final Set<AttributeDescriptor> STANAG4559_MESSAGE_DESCRIPTORS = new HashSet<>();

    static {
        STANAG4559_MESSAGE_DESCRIPTORS.add(new AttributeDescriptorImpl(RECIPIENT,
                true /* indexed */,
                true /* stored */,
                true /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        STANAG4559_MESSAGE_DESCRIPTORS.add(new AttributeDescriptorImpl(MESSAGE_TYPE,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));
    }

    public Stanag4559MessageMetacardType() {
        super();
        attributeDescriptors.addAll(STANAG4559_MESSAGE_DESCRIPTORS);
    }

    @Override
    public String getName() {
        return METACARD_TYPE_NAME;
    }

}
