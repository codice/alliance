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

import java.util.Collections;
import java.util.List;

import org.codice.alliance.video.stream.mpegts.Context;

import ddf.catalog.data.Metacard;

/**
 * List of {@link UpdateParent.UpdateField} objects.
 */
public class UpdateFieldList extends UpdateParent.BaseUpdateField {

    private final List<UpdateParent.UpdateField> updateFieldList;

    /**
     * @param updateFieldList list of UpdateField objects that will be called in {@link #doEnd(Metacard, Context)} and {@link #doUpdateField(Metacard, List, Context)}
     */
    public UpdateFieldList(List<UpdateParent.UpdateField> updateFieldList) {
        this.updateFieldList = Collections.unmodifiableList(updateFieldList);
    }

    @Override
    protected void doEnd(Metacard parent, Context context) {
        updateFieldList.forEach(updateField -> updateField.end(parent, context));
    }

    @Override
    protected void doUpdateField(Metacard parent, List<Metacard> children, Context context) {
        updateFieldList.forEach(updateField -> updateField.updateField(parent, children, context));
    }

    /**
     * Only used for testing.
     */
    List<UpdateParent.UpdateField> getUpdateFieldList() {
        return updateFieldList;
    }

}
