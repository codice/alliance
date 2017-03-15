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

import ddf.catalog.data.Metacard;

/**
 * List of {@link UpdateParent.UpdateField} objects.
 */
public class UpdateFieldList extends UpdateParent.BaseUpdateField {

    @Override
    protected void doEnd(Metacard parent) {
        updateFieldList.forEach(updateField -> updateField.end(parent));
    }

    @Override
    protected void doUpdateField(Metacard parent, List<Metacard> children) {
        updateFieldList.forEach(updateField -> updateField.updateField(parent, children));
    }

    private final List<UpdateParent.UpdateField> updateFieldList;

    /**
     * @param updateFieldList must be non-null
     */
    public UpdateFieldList(List<UpdateParent.UpdateField> updateFieldList) {
        this.updateFieldList = updateFieldList;
    }

    /**
     * Only used for testing.
     */
    List<UpdateParent.UpdateField> getUpdateFieldList() {
        return updateFieldList;
    }
}
