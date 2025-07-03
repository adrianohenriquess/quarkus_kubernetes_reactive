package org.acme;

import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import java.time.Duration;
import java.util.List;

@ApplicationScoped
public class StreamingExample {

    @Outgoing("mticks")
    public Multi<Long> ticks() {
        return Multi.createFrom()
                .ticks().every(Duration.ofSeconds(1))
                .onOverflow().drop();
    }

    @Incoming("mticks")
    @Outgoing("mgroups")
    public Multi<List<String>> group(Multi<Long> stream) {
        return stream.onItem()
                .transform(l -> Long.toString(l))
                .group().intoLists().of(5);
    }

    @Incoming("mgroups")
    @Outgoing("mhello")
    public String processGroup(List<String> list) {
        return "Hello " + String.join(",", list);
    }

    @Incoming("mhello")
    public void print(String msg) {
        System.out.println(msg);
    }

}
