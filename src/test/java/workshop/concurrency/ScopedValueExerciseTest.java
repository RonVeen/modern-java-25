package workshop.concurrency;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScopedValueExerciseTest {
    @Test
    void everyChildSeesItsRequestsContext() throws InterruptedException {
        var probe = new ServiceProbe();
        var timings = new ServiceTimings(
                Duration.ofMillis(2),
                Duration.ofMillis(2),
                Duration.ofMillis(2),
                Duration.ofMillis(20));
        var runner = new BatchRunner(
                TestFixtures.structured(timings, probe, Duration.ofSeconds(1)));

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var result = runner.run(DemoData.requests(100), DemoData::contextFor, executor);
            assertEquals(100, result.quotes().size());
        }

        assertFalse(probe.events().isEmpty());
        assertTrue(probe.events().stream().allMatch(event ->
                event.requestId().equals(event.contextRequestId())));
    }

    @Test
    void bindingDoesNotLeakOutsideItsLexicalScope() throws InterruptedException {
        var probe = new ServiceProbe();
        var timings = new ServiceTimings(
                Duration.ZERO, Duration.ZERO, Duration.ZERO, Duration.ZERO);
        var aggregator = TestFixtures.sequential(timings, probe);
        var request = new QuoteRequest("scope-1", "NL", "NL", 1, Scenario.NORMAL);

        assertThrows(IllegalStateException.class, RequestContexts::current);

        aggregator.quote(
                request,
                new RequestContext("scope-1", CustomerTier.REGULAR, false));

        assertThrows(IllegalStateException.class, RequestContexts::current);
    }
}
