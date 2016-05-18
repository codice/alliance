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

public class NsiliMessageMetacardType extends NsiliMetacardType {
    public static final String METACARD_TYPE_NAME = NSILI_METACARD_TYPE_PREFIX +".message."+ NSILI_METACARD_TYPE_POSTFIX;

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

    /**
     * Data that specifies the topic of the message.
     */
    public static final String MESSAGE_SUBJECT = "messageSubject";

    /**
     * The body of the message. In case the message body text exceeds the maximum length of this
     * metadata attribute the message text will be truncated. . The complete message is available
     * through the URL in NSIL_FILE entity, but only the characters stored in this attribute can be
     * used for free text search.
     */
    public static final String MESSAGE_BODY = "messageBody";

    private static final Set<AttributeDescriptor> NSILI_DESCRIPTORS = new HashSet<>();

    static {
        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(RECIPIENT,
                true /* indexed */,
                true /* stored */,
                true /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(MESSAGE_TYPE,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(MESSAGE_SUBJECT,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_DESCRIPTORS.add(new AttributeDescriptorImpl(MESSAGE_BODY,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));
    }

    public NsiliMessageMetacardType() {
        super();
        attributeDescriptors.addAll(NSILI_DESCRIPTORS);
    }

    @Override
    public String getName() {
        return METACARD_TYPE_NAME;
    }

}
