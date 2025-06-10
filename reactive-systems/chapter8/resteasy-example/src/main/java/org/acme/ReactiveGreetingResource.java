package org.acme;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.common.annotation.NonBlocking;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.nio.file.FileSystemException;
import java.time.Duration;

@Path("/")
public class ReactiveGreetingResource {

    @Inject
    Vertx vertx;

    @GET
    @Path("/hello-resteasy-reactive")
    @Produces(MediaType.TEXT_PLAIN)
    @NonBlocking
    public String hello() {
        return "Hello RESTEasy Reactive from " + Thread.currentThread().getName();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/blocking")
    @Blocking
    public String helloBlocking() {
        return "Hello RESTEasy Reactive from " +
                Thread.currentThread().getName();
    }

    @GET
    @Path("/lorem")
    public Uni<String> getLoremIpsum() {
        return vertx.fileSystem().readFile("lorem.txt")
                .onItem().transform(buffer -> buffer.toString("UTF-8"));
    }

    @GET
    @Path("/missing")
    public Uni<String> getMissingFile() {
        return vertx.fileSystem().readFile("Oops.txt")
                .onItem().transform(buffer -> buffer.toString("UTF-8"));
    }

    @GET
    @Path("/recover")
    public Uni<String> getMissingFileAndRecover() {
        return vertx.fileSystem().readFile("Oops.txt")
                .onItem().transform(buffer -> buffer.toString("UTF-8"))
                .onFailure().recoverWithItem("Oops");
    }

    @GET
    @Path("/404")
    public Uni<Response> get404() {
        return vertx.fileSystem().readFile("Oops.txt")
                .onItem().transform(buffer -> buffer.toString("UTF-8"))
                .onItem().transform(content -> Response.ok(content).build())
                .onFailure().recoverWithItem(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/slow")
    public Uni<String> slow() {
        return vertx.fileSystem().readFile("slow.txt")
                    .onItem().transform(buffer -> buffer.toString("UTF-8"))
                    .ifNoItem().after(Duration.ofSeconds(1)).fail();
    }

    @ServerExceptionMapper
    public Response mapFileSystemException(FileSystemException ex) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(ex.getMessage())
                .build();
    }
}
