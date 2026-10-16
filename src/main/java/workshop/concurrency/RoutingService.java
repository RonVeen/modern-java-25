package workshop.concurrency;

import java.time.Duration;

public final class RoutingService {
    private final ServiceTimings timings;
    private final ServiceProbe probe;

    public RoutingService(ServiceTimings timings, ServiceProbe probe) {
        this.timings = timings;
        this.probe = probe;
    }

    // TODO LAB 3: Remove the context parameter and read the scoped value instead.
    public RoutingResult route(QuoteRequest request, RequestContext context)
            throws InterruptedException {
        probe.record("routing", request, context, ServiceProbe.Outcome.STARTED);
        try {
            Thread.sleep(timings.routing());
            var international = !request.originCountry().equals(request.destinationCountry());
            var result = international
                    ? new RoutingResult(1_200, "INTERNATIONAL", Duration.ofDays(3))
                    : new RoutingResult(120, "DOMESTIC", Duration.ofDays(1));
            probe.record("routing", request, context, ServiceProbe.Outcome.COMPLETED);
            return result;
        } catch (InterruptedException exception) {
            probe.record("routing", request, context, ServiceProbe.Outcome.INTERRUPTED);
            throw exception;
        }
    }
}

