/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.nsili.mockserver.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/nsili")
public class NsiliWeb {
    public NsiliWeb() {

    }

    @Path("test")
    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public Response NsiliWebClient() {
        Response r = Response.status(200).entity("Welcome to NSILI Test Web Listener").build();
        return r;
    }

    @Path("file/{filename}")
    @PUT
    public Response filePut(@PathParam("filename") String filename, InputStream message)
            throws IOException {
        byte[] buf = new byte[2048];

        String storedFileName = "/tmp/"+filename;
        File storeFile = new File(storedFileName);
        storeFile.deleteOnExit();
        System.out.println("PUT File Received: " + storedFileName);

        try (FileOutputStream fos = new FileOutputStream(storeFile)) {
            int numRead = message.read(buf);
            while (numRead != -1) {
                fos.write(buf, 0, numRead);
                numRead = message.read(buf);
            }
            fos.flush();
        }
        catch (IOException ioe) {
            System.err.println("Unable to store file: "+storedFileName);
            ioe.printStackTrace(System.err);
        }

        Response r = Response.accepted().build();
        return r;
    }
}
