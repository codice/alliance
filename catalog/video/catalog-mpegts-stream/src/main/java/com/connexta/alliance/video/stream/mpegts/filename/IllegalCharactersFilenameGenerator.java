/**
 * Copyright (c) Connexta, LLC
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
package com.connexta.alliance.video.stream.mpegts.filename;

/**
 * Removes illegal characters from a filename.
 */
public class IllegalCharactersFilenameGenerator extends BaseFilenameGenerator {

    /**
     * These are regexes that are supplied to {@link String#replaceAll(String, String)}.
     */
    private static final String[] ILLEGAL_CHARACTERS_REGEXES = new String[] {"/", ":"};

    private static final String REPLACE_WITH = "";

    @Override
    protected String doGenerateFilename(String baseFilename) {
        String tmp = baseFilename;
        for (String illegalCharacterRegex : ILLEGAL_CHARACTERS_REGEXES) {
            tmp = tmp.replaceAll(illegalCharacterRegex, REPLACE_WITH);
        }
        return tmp;
    }

    @Override
    public String toString() {
        return "IllegalCharactersFilenameGenerator{}";
    }
}
