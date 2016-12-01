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

## MPEGTS UDP Stream Generator

Codice Alliance contains a sample MPEGTS UDP Stream Generator to be used for testing purposes.  This utility can be run from the command-line within the sample-mpegts-streamgenerator directory by using maven and specifying a file path.

```
mvn -Pmpegts.stream -Dexec.args=path=<mpegPath>,ip=<ip address>,port=<port>,datagramSize=<size|min-max>,fractionalTs=<yes|no>
e.g. mvn -Pmpegts.stream -Dexec.args="path=/Users/johndoe/Documents/stream.ts,ip=127.0.0.1,port=50000,datagramSize=188-1500,fractionalTs=no,interface=en0"
```

path: The full path to a TS file. (required)

ip: The IP address of the destination. (default=127.0.0.1)

port: The port number of the destination. (default=50000)

datagramSize: The size (integer) or range of sizes (integer-integer) for the datagram packet. If a range is specified, then a random number within that range will be selected for each packet sent. (default=188)

fractionalTs: Can datagram packets contain a fractional MPEG-TS packet? (values: yes, no) (default=no)

interface: Bind to a specific interface (e.g. en0) for sending datagrams. If not set, then bind to all interfaces (ie. wildcard). Useful for sending packets to a specific VLAN. (default=unset)
