package workshop.concurrency;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BatchRunnerTest {
    @Test
    void runsRequestsOnTheProvidedPlatformPool() throws InterruptedException {
        var probe = new ServiceProbe();
        var timings = new ServiceTimings(
                Duration.ZERO, Duration.ZERO, Duration.ZERO, Duration.ZERO);
        var aggregator = new SequentialQuoteAggregator(
                new RoutingService(timings, probe),
                new PricingService(timings, probe),
                new CustomsService(timings, probe));
        var runner = new BatchRunner(aggregator);

        try (var executor = Executors.newFixedThreadPool(2)) {
            var result = runner.run(DemoData.requests(10), DemoData::contextFor, executor);
            assertEquals(10, result.quotes().size());
        }

        assertFalse(probe.events().isEmpty());
        assertFalse(probe.events().stream().anyMatch(ServiceProbe.Event::virtualThread));
    }
}

