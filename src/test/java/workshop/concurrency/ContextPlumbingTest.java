package workshop.concurrency;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ContextPlumbingTest {
    @Test
    void explicitContextReachesEveryService() throws InterruptedException {
        var probe = new ServiceProbe();
        var timings = new ServiceTimings(
                Duration.ZERO, Duration.ZERO, Duration.ZERO, Duration.ZERO);
        var aggregator = new SequentialQuoteAggregator(
                new RoutingService(timings, probe),
                new PricingService(timings, probe),
                new CustomsService(timings, probe));
        var request = new QuoteRequest("context-1", "NL", "DE", 2, Scenario.NORMAL);
        var context = new RequestContext("context-1", CustomerTier.REGULAR, true);

        aggregator.quote(request, context);

        assertTrue(probe.events().stream().allMatch(event ->
                event.requestId().equals(event.contextRequestId())));
    }
}

