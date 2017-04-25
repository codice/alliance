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
class NitfRenderPluginBuildDerivedImageTitleTest extends Specification {

    def "Building derived image title from \"#fullTitle\"" (String fullTitle, String expectedTitle) {
        setup:
            NitfRenderPlugin plugin = new NitfRenderPlugin()
            def qualifier = "original"

        when: "building a derived image title"
            def derivedTitle = plugin.buildDerivedImageTitle(fullTitle, qualifier, NitfRenderPlugin.JPG)

        then: "the derived title should not include invalid characters"
            derivedTitle == expectedTitle

        where:
            fullTitle                                                  ||  expectedTitle
            null                                                       ||  "original.jpg"
            ""                                                         ||  "original.jpg"
            "_"                                                        ||  "original.jpg"
            "@#\$%^&*()+-={}|[]<>?:"                                   ||  "original.jpg"
            "Too Legit To Quit"                                        ||  "original-toolegittoquit.jpg"
            "A bunch of _invalid_ characters! @#\$%^&*()+-={}|[]<>?:;" ||  "original-abunchof_invalid_characters.jpg"
    }
}