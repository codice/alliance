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
package org.codice.alliance.plugin.nitf

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class NitfPostIngestPluginSpec extends Specification {

    def "Building the derived image filename from file title \"#ftitle\""(String ftitle, String expectedFname) {
        setup:
        NitfPostIngestPlugin plugin = new NitfPostIngestPlugin()
        def qualifier = "original"

        when: "building a derived image filename"
        def derivedFname = plugin.buildDerivedImageTitle(ftitle, qualifier, "jpg")

        then: "the derived filenames should not include invalid characters"
        derivedFname == expectedFname

        where:
        ftitle                                                     || expectedFname
        null                                                       || "original.jpg"
        ""                                                         || "original.jpg"
        "_"                                                        || "original.jpg"
        "@#\$%^&*()+-={}|[]<>?:"                                   || "original.jpg"
        "Too Legit To Quit"                                        || "original-toolegittoquit.jpg"
        "A bunch of _invalid_ characters! @#\$%^&*()+-={}|[]<>?:;" || "original-abunchof_invalid_characters.jpg"
    }
}