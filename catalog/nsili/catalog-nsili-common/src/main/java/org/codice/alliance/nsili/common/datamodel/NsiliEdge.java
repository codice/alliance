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

public class NsiliEdge {
    private String startNodeId;
    private String endNodeId;

    public NsiliEdge() {

    }

    public NsiliEdge(String startNodeId, String endNodeId) {
        this.startNodeId = startNodeId;
        this.endNodeId = endNodeId;
    }

    public String getStartNodeId() {
        return startNodeId;
    }

    public void setStartNodeId(String startNodeId) {
        this.startNodeId = startNodeId;
    }

    public String getEndNodeId() {
        return endNodeId;
    }

    public void setEndNodeId(String endNodeId) {
        this.endNodeId = endNodeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NsiliEdge nsiliEdge = (NsiliEdge) o;

        if (startNodeId != null ?
                !startNodeId.equals(nsiliEdge.startNodeId) :
                nsiliEdge.startNodeId != null) {
            return false;
        }
        return endNodeId != null ?
                endNodeId.equals(nsiliEdge.endNodeId) :
                nsiliEdge.endNodeId == null;

    }

    @Override
    public int hashCode() {
        int result = startNodeId != null ? startNodeId.hashCode() : 0;
        result = 31 * result + (endNodeId != null ? endNodeId.hashCode() : 0);
        return result;
    }
}
