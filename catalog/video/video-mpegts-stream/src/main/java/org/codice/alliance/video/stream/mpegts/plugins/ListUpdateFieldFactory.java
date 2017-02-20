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
import java.util.stream.Collectors;

/**
 * Create a {@link org.codice.alliance.video.stream.mpegts.plugins.UpdateParent.UpdateField} based on a list
 * of {@link UpdateParentFactory.Factory} factories.
 */
public class ListUpdateFieldFactory implements UpdateParentFactory.Factory {

    private final List<UpdateParentFactory.Factory> factories;

    /**
     * @param factories must be non-null
     */
    public ListUpdateFieldFactory(List<UpdateParentFactory.Factory> factories) {
        this.factories = factories;
    }

    @Override
    public UpdateParent.UpdateField build() {
        return new UpdateFieldList(factories.stream()
                .map(UpdateParentFactory.Factory::build)
                .collect(Collectors.toList()));
    }

}
