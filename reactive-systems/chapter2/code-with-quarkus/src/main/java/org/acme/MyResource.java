package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
@Produces({MediaType.TEXT_PLAIN})
public class MyResource {

    @GET
    public String message() {
        return "Hi";
    }
}
