package workshop.concurrency;

import java.time.Duration;
import java.util.Objects;

public record ServiceTimings(
        Duration routing,
        Duration pricing,
        Duration customs,
        Duration slowCustoms) {

    public ServiceTimings {
        Objects.requireNonNull(routing, "routing");
        Objects.requireNonNull(pricing, "pricing");
        Objects.requireNonNull(customs, "customs");
        Objects.requireNonNull(slowCustoms, "slowCustoms");
    }

    public static ServiceTimings demo() {
        return new ServiceTimings(
                Duration.ofMillis(50),
                Duration.ofMillis(50),
                Duration.ofMillis(50),
                Duration.ofMillis(500));
    }
}

