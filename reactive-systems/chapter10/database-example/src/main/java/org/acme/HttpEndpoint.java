package org.acme;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.reactive.messaging.Channel;

import java.util.List;

@Path("/")
public class HttpEndpoint {

   @Channel("upload")
   MutinyEmitter<Person> emitter;

    @POST
    public Uni<Response> upload(Person person) {
        return emitter.send(person)
                .replaceWith(Response.accepted().build())
                .onFailure()
                .recoverWithItem(t ->
                                            Response.status(Response.Status.BAD_REQUEST)
                                                .entity(t.getMessage()).build());
    }

    @GET
    public Uni<List<Person>> getAll() {
        return Person.listAll();
    }
}
