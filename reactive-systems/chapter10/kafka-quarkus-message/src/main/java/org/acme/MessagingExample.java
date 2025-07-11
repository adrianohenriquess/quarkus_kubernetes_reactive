package org.acme;

import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.annotations.Merge;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import java.time.Duration;

/*@ApplicationScoped
public class MessagingExample {

    @Outgoing("ticks")
    public Multi<MyMessage> ticks() {
        return Multi.createFrom().ticks()
                .every(Duration.ofSeconds(1))
                .onOverflow().drop()
                .onItem().transform(MyMessage::new);
    }

    @Incoming("ticks")
    @Outgoing("hello")
    @Merge
    public Message<String> helloMessage(Message<String> tick) {
        return tick.withPayload("Hello " + tick.getPayload());
    }

    @Incoming("hello")
    public void print(String msg) {
        if (msg.contains("3")) {
            throw new IllegalArgumentException("boom");
        }
        System.out.println(msg);
    }

}*/
