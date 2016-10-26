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
package org.codice.alliance.libs.klv;

import static org.codice.alliance.libs.klv.Utilities.safelySetAttribute;

import java.util.Map;

import ddf.catalog.data.Metacard;

/**
 * For each handler, call its asAttribute() method and add the result to the metacard.
 */
public class CopyPresentKlvProcessor implements KlvProcessor {

    @Override
    public void process(Map<String, KlvHandler> handlers, Metacard metacard,
            Configuration configuration) {
        handlers.values()
                .stream()
                .distinct()
                .forEach(handler -> {
                    handler.asAttribute()
                            .ifPresent(attribute -> safelySetAttribute(metacard, attribute));
                });
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
