package org.acme.service;

import io.quarkus.redis.client.reactive.ReactiveRedisClient;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.acme.exceptions.NotFoundException;
import org.acme.model.Customer;
import io.vertx.mutiny.redis.client.Response;

import java.util.Arrays;
import java.util.NoSuchElementException;

@Singleton
public class CustomerService {
    private static final String CUSTOMER_HASH_PREFIX = "cust:";

    @Inject
    ReactiveRedisClient reactiveRedisClient;

    public Multi<Customer> allCustomers() {
        return reactiveRedisClient.keys("*")
                .onItem()
                .transformToMulti(response -> Multi.createFrom()
                                            .iterable(response).map(Response::toString))
                .onItem()
                .transformToUniAndMerge(key -> reactiveRedisClient.hgetall(key)
                .map(resp ->
                        constructCustomer(Long.parseLong(key.substring(CUSTOMER_HASH_PREFIX.length())), resp)));
    }

    public Uni<Customer> getCustomer(Long id) {
        return reactiveRedisClient.hgetall(CUSTOMER_HASH_PREFIX + id)
                .map(resp -> resp.size() > 0
                        ? constructCustomer(id, resp)
                        : null
                );
    }

    public Uni<Customer> createCustomer(Customer customer) {
        return storeCustomer(customer);
    }

    public Uni<Customer> updateCustomer(Customer customer) {
        return getCustomer(customer.getId())
                .onItem().transformToUni((cust) -> {
                    if (cust == null) {
                        return Uni.createFrom().failure(new NotFoundException());
                    }
                    cust.setName(customer.getName());
                    return storeCustomer(cust);
                });
    }

    public Uni<Void> deleteCustomer(Long id) {
        return reactiveRedisClient.hdel(Arrays.asList(CUSTOMER_HASH_PREFIX + id, "name"))
                .map(resp -> resp.toInteger() == 1 ? true : null)
                .onItem().ifNull().failWith(new NotFoundException())
                .onItem().ifNotNull().transformToUni(r -> Uni.createFrom().nullItem());
    }

    private Uni<Customer> storeCustomer(Customer customer) {
        return reactiveRedisClient.hmset(Arrays.asList(CUSTOMER_HASH_PREFIX + customer.getId(), "name", customer.getName()))
                .onItem().transform(resp -> {
                    if (resp.toString().equals("OK")) {
                        return customer;
                    } else {
                        throw new NoSuchElementException();
                    }
                });
    }

    Customer constructCustomer(long id, Response response) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName(response.get("name").toString());
        return customer;
    }

}
