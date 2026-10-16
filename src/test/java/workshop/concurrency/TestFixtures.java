package workshop.concurrency;

import java.time.Duration;

final class TestFixtures {
    private TestFixtures() {
    }

    static QuoteAggregator sequential(ServiceTimings timings, ServiceProbe probe) {
        return new SequentialQuoteAggregator(
                new RoutingService(timings, probe),
                new PricingService(timings, probe),
                new CustomsService(timings, probe));
    }

    static QuoteAggregator structured(
            ServiceTimings timings,
            ServiceProbe probe,
            Duration timeout) {
        return new StructuredQuoteAggregator(
                new RoutingService(timings, probe),
                new PricingService(timings, probe),
                new CustomsService(timings, probe),
                timeout);
    }
}

