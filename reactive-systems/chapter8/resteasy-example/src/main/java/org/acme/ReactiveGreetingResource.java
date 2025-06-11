package org.acme;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.common.annotation.NonBlocking;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.file.OpenOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.file.AsyncFile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.nio.file.FileSystemException;
import java.time.Duration;
import java.util.List;
import java.util.Random;

@Path("/")
public class ReactiveGreetingResource {

    @Inject
    Vertx vertx;

    @Inject
    BookService service;

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

    @GET
    @Path("/book")
    @Produces(MediaType.TEXT_PLAIN)
    public Multi<String> book() {
        return vertx.fileSystem()
                .open("war-and-peace.txt", new OpenOptions().setRead(true))
                .onItem().transformToMulti(AsyncFile::toMulti)
                .onItem().transform(b -> b.toString("UTF-8"));
    }

    @GET
    @Path("/book/ticks")
    @Produces(MediaType.TEXT_PLAIN)
    public Multi<String> bookWithTicks() {
        Multi<Long> ticks = Multi.createFrom().ticks().every(Duration.ofSeconds(1));
        Multi<String> book = vertx.fileSystem()
                .open("war-and-peace.txt", new OpenOptions().setRead(true))
                .onItem().transformToMulti(AsyncFile::toMulti)
                .onItem().transform(b -> b.toString("UTF-8"));
        return Multi.createBy().combining().streams(ticks, book)
                .asTuple().onItem().transform(Tuple2::getItem2);
    }

    @GET
    @Path("/books")
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<Book> books() {
        return service.getBooks();
    }

    @GET
    @Path("/books/ticks")
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<Book> booksWithTicks() {
        Multi<Long> ticks = Multi.createFrom().ticks().every(Duration.ofSeconds(1));
        Multi<Book> books = service.getBooks();
        return Multi.createBy().combining()
                .streams(ticks, books).asTuple()
                .onItem().transform(Tuple2::getItem2);
    }

    public static class Book {
        public final long id;
        public final String title;
        public final List<String> authors;

        public Book(long id, String title, List<String> authors) {
            this.id = id;
            this.title = title;
            this.authors = authors;
        }
    }

    @ApplicationScoped
    static class BookService {
        private final List<Book> books = List.of(
                new Book(0, "Fundamentals of Software Architecture", List.of("Mark Richards", "Neal Ford")),
                new Book(1, "Domain-Driven Design", List.of("Eric Evans")),
                new Book(2, "Designing Distributed Systems", List.of("Brendan Burns")),
                new Book(3, "Building Evolutionary Architectures", List.of("Neal Ford", "Rebecca Parsons", "Patrick Kua")),
                new Book(4, "Principles of Concurrent and Distributed Programming", List.of("M. Ben-Ari")),
                new Book(5, "Distributed Systems Observability", List.of("Cindy Sridharan")),
                new Book(6, "Event Streams in Action", List.of("Alexander Dean", "Valentin Crettaz")),
                new Book(7, "Designing Data-Intensive Applications", List.of("Martin Kleppman")),
                new Book(8, "Building Microservices", List.of("Sam Newman")),
                new Book(9, "Kubernetes in Action", List.of("Marko Luksa")),
                new Book(10, "Kafka - the definitive guide", List.of("Gwenn Shapira", "Todd Palino", "Rajini Sivaram", "Krit Petty")),
                new Book(11, "Effective Java", List.of("Joshua Bloch")),
                new Book(12, "Building Event-Driven Microservices", List.of("Adam Bellemare"))
        );

        Multi<Book> getBooks() {
            return Multi.createFrom().iterable(books);
        }
    }

    public static class Quote {
        public final String company;
        public final double value;

        public Quote(String company, double value) {
            this.company = company;
            this.value = value;
        }
    }

    @Inject Market market;

    @GET
    @Path("/market")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<Quote> market() {
        return market.getEventStream();
    }

    @ApplicationScoped
    public static class Market {
        Multi<Quote> getEventStream() {
            return Multi.createFrom().ticks().every(Duration.ofSeconds(1))
                    .onItem().transform(x -> getRandomQuote());
        }

        Random random = new Random();

        private Quote getRandomQuote() {
            int i = random.nextInt(3);
            String company = "MacroHard";
            if (i ==0) {
                company = "Divinator";
            } else if (i == 1) {
                company = "Black Coat";
            }

            double value = random.nextInt(200) * random.nextDouble();

            return new Quote(company, value);
        }
    }

}
