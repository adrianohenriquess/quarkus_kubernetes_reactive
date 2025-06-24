package org.acme.resource;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.acme.model.Customer;
import org.acme.model.Order;
import org.acme.service.OrderService;
import org.jboss.resteasy.reactive.RestPath;

import java.util.List;

@ApplicationScoped
@Path("customer")
public class CustomerResource {

    @Inject
    OrderService service;

    @GET
    public Uni<List<Customer>> findAll() {
        return Customer.listAll(Sort.by("name"));
    }

    @GET
    @Path("{id}")
    public Uni<Response> getCustomer(@RestPath Long id) {
        Uni<Customer> customerUni = Customer.<Customer>findById(id)
                .onItem().ifNull()
                .failWith(new WebApplicationException("Failed to find customer", Response.Status.NOT_FOUND));
        Uni<List<Order>> customerOrdersUni = service.getOrdersForCustomer(id);
        return Uni.combine()
                .all().unis(customerUni, customerOrdersUni)
                .asTuple()
                .onItem().transform(tuple -> {
                   Customer customer = tuple.getItem1();
                   List<Order> orders = tuple.getItem2();
                   customer.setOrders(orders);
                   return Response.ok(customer).build();
                });
    }

    @POST
    public Uni<Response> createCustomer(@Valid Customer customer) {
        if (customer.id != null) {
            throw new WebApplicationException("Invalid customer set on request", 422);
        }

        return Panache
                .withTransaction(customer::persist)
                .replaceWith(Response.ok(customer).status(Response.Status.CREATED).build());
    }

    @PUT
    @Path("{id}")
    public Uni<Response> updateCustomer(@RestPath Long id, @Valid Customer customer) {
        if (customer.id == null) {
            throw new WebApplicationException("Invalid customer set on request", 422);
        }

        return Panache
                .withTransaction(
                        () -> Customer.<Customer>findById(id)
                                .onItem().ifNotNull()
                                .invoke(entity -> entity.setName(customer.getName()))
                )
                .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                .onItem().ifNull().continueWith(Response.ok().status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> deleteCustomer(@RestPath Long id) {
        return Panache
                .withTransaction(() -> Customer.deleteById(id))
                .map(deleted -> deleted
                        ? Response.ok().status(Response.Status.NO_CONTENT).build()
                        : Response.ok().status(Response.Status.NOT_FOUND).build());
    }
}
