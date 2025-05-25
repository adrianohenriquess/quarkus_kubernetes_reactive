package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/welcome")
public class WelcomeResource {

    @GET
    public String welcomeMessage() {
        return "Welcome to Quarkus!";
    }
}
