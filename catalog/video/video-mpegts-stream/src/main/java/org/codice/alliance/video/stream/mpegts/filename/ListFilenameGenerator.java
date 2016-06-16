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
package org.codice.alliance.video.stream.mpegts.filename;

import java.util.List;

/**
 * Contains a list of FilenameGenerator objects and chains those objects together.
 */
public class ListFilenameGenerator extends BaseFilenameGenerator {

    private final List<FilenameGenerator> filenameGeneratorList;

    public ListFilenameGenerator(List<FilenameGenerator> filenameGeneratorList) {
        this.filenameGeneratorList = filenameGeneratorList;
    }

    @Override
    protected String doGenerateFilename(String baseFilename) {
        String tmp = baseFilename;
        for (FilenameGenerator filenameGenerator : filenameGeneratorList) {
            tmp = filenameGenerator.generateFilename(tmp);
        }
        return tmp;
    }

    @Override
    public String toString() {
        return "ListFilenameGenerator{" +
                "filenameGeneratorList=" + filenameGeneratorList +
                '}';
    }
}
