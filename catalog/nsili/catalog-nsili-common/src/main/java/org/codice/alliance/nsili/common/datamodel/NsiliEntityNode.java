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
package org.codice.alliance.nsili.common.datamodel;

import java.util.ArrayList;
import java.util.List;

import org.codice.alliance.nsili.common.GIAS.AttributeInformation;

public class NsiliEntityNode {
    private int nodeId;

    private String nodeName;

    public NsiliEntityNode() {

    }

    public NsiliEntityNode(int nodeId, String nodeName, List<AttributeInformation> attributes) {
        setNodeId(nodeId);
        setNodeName(nodeName);
        setAttributes(attributes);
    }

    protected List<AttributeInformation> attributes = new ArrayList<>();

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public List<AttributeInformation> getAttributes() {
        return new ArrayList<>(attributes);
    }

    public void setAttributes(List<AttributeInformation> attributes) {
        this.attributes.clear();
        if (attributes != null) {
            this.attributes.addAll(attributes);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NsiliEntityNode that = (NsiliEntityNode) o;

        return nodeId == that.nodeId;

    }

    @Override
    public int hashCode() {
        return nodeId;
    }

}
