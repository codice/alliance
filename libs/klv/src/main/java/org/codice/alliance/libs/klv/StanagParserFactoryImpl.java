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

import org.codice.alliance.libs.stanag4609.Stanag4609TransportStreamParser;
import com.google.common.io.ByteSource;

/**
 * This factory returns a {@link Stanag4609Parser} that uses {@link Stanag4609TransportStreamParser}.
 */
public class StanagParserFactoryImpl implements StanagParserFactory {

    @Override
    public Stanag4609Parser createParser(ByteSource byteSource) {
        return () -> {
            try {
                return new Stanag4609TransportStreamParser(byteSource).parse();
            } catch (Exception e) {
                throw new Stanag4609ParseException("unable to parse stanag 4609 data", e);
            }
        };
    }

}
