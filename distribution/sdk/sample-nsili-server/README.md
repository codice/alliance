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

## Mock NSILI Server

Codice Alliance contains a sample NSILI compliant server to be used for testing purposes. This utility can be run from the command-line within the sample-nsili-server directory using Maven by specifying HTTP, FTP, and CORBA ports.

```
mvn -Pcorba.server -Dexec.args=HTTP_PORT,FTP_PORT,CORBA_PORT
    e.g. mvn -Pcorba.server -Dexec.args=20009,20010,20011
```
The IOR string can be retrieved from both http and ftp endpoints.
```
http://localhost:HTTP_PORT/data/ior.txt
ftp://localhost:FTP_PORT/data/ior.txt (username: admin, password: admin)
```