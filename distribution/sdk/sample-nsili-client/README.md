<!--
/*
 * Copyright (c) Codice Foundation
 *
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
-->
<img src="https://tools.codice.org/wiki/download/attachments/1179800/ddf.jpg"/>
# [Codice Alliance](http://github.com/codice/alliance/)

## Sample NSILI Client

Codice Alliance contains a sample NSILI client to be used for testing purposes.  This utility can be run from the command-line within the sample-nsili-client directory by using maven and specifying a the IOR URL and an optional email address:
* `mvn -Pcorba.client -Dexec.args="url=IORURL"`
* `mvn -Pcorba.client -Dexec.args="url=IORURL,email=EMAIL"`

Examples:
* `mvn -Pcorba.client -Dexec.args="url=http://localhost:20009/data/ior.txt"`
* `mvn -Pcorba.client -Dexec.args="url=https://localhost:8993/services/nsili/ior.txt"`
* `mvn -Pcorba.client -Dexec.args="url=https://localhost:8993/services/nsili/ior.txt,email=test@test.com"`
