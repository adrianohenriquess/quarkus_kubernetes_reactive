package org.acme.resource;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.acme.model.Customer;
import org.acme.service.CustomerService;
import org.jboss.resteasy.reactive.RestPath;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
@Path("customer")
public class CustomerResource {

    private final AtomicLong customerId = new AtomicLong(1);

    @Inject
    CustomerService client;

    @GET
    public Multi<Customer> allCustomers() {
        return client.allCustomers();
    }

    @GET
    @Path("{id}")
    public Uni<Customer> getCustomer(@RestPath Long id) {
        return client.getCustomer(id).onItem().ifNull()
                .failWith(new WebApplicationException("Failed to find customer", Response.Status.NOT_FOUND));
    }

    @POST
    public Uni<Response> createCustomer(Customer customer) {
        if (customer.getId() == null || customer.getName().isEmpty()) {
            throw new WebApplicationException("Invalid customer set on request", 422);
        }

        customer.setId(customerId.getAndIncrement());

        return client.createCustomer(customer)
                .onItem().transform(cust -> Response.ok(cust).status(Response.Status.CREATED).build())
                .onFailure().recoverWithItem(Response.serverError().build());
    }

    @PUT
    @Path("{id}")
    public Uni<Response> updateCustomer(@RestPath Long id, Customer customer) {
        if (customer.getId() == null ||
                (customer.getName() == null || customer.getName().length() == 0)) {
            throw new WebApplicationException("Invalid customer set on request", 422);
        }

        return client.updateCustomer(customer)
                .onItem().ifNotNull().transform(success -> Response.ok(customer).build())
                .onFailure().recoverWithItem(Response.ok().status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> deleteCustomer(@RestPath Long id) {
        return client.deleteCustomer(id)
                .onItem().transform(i -> Response.ok().status(Response.Status.NO_CONTENT).build())
                .onFailure().recoverWithItem(Response.ok().status(Response.Status.NOT_FOUND).build());
    }
}
