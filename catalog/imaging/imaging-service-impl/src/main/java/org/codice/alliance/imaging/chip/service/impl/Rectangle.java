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
package org.codice.alliance.imaging.chip.service.impl;

class Rectangle<T extends Object> {
    private T upperLeft;
    private T lowerRight;

    public T getUpperLeft() {
        return upperLeft;
    }

    public T getLowerRight() {
        return lowerRight;
    }

    public Rectangle(T upperLeft, T lowerRight) {
        this.upperLeft = upperLeft;
        this.lowerRight = lowerRight;
    }
}
