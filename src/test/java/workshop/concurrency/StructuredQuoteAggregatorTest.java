package workshop.concurrency;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StructuredQuoteAggregatorTest {
    @Test
    void combinesSuccessfulChildren() throws InterruptedException {
        var probe = new ServiceProbe();
        var timings = new ServiceTimings(
                Duration.ofMillis(5),
                Duration.ofMillis(5),
                Duration.ofMillis(5),
                Duration.ofMillis(100));
        var aggregator = TestFixtures.structured(timings, probe, Duration.ofSeconds(1));
        var request = new QuoteRequest("structured-1", "NL", "DE", 10, Scenario.NORMAL);
        var context = new RequestContext("structured-1", CustomerTier.GOLD, true);

        var quote = aggregator.quote(request, context);

        assertEquals(3_075, quote.totalPriceInCents());
        assertEquals(3, probe.events().stream()
                .filter(event -> event.outcome() == ServiceProbe.Outcome.COMPLETED)
                .count());
    }

    @Test
    void failsFastAndInterruptsUnfinishedChildren() {
        var probe = new ServiceProbe();
        var timings = new ServiceTimings(
                Duration.ofSeconds(2),
                Duration.ofSeconds(2),
                Duration.ofMillis(30),
                Duration.ofSeconds(2));
        var aggregator = TestFixtures.structured(timings, probe, Duration.ofSeconds(3));
        var request = new QuoteRequest(
                "failure-1", "NL", "US", 10, Scenario.CUSTOMS_FAILURE);
        var context = new RequestContext("failure-1", CustomerTier.REGULAR, true);

        var exception = assertThrows(
                QuoteFailedException.class,
                () -> aggregator.quote(request, context));

        assertInstanceOf(CustomsRejectedException.class, exception.getCause());
        assertTrue(probe.events().stream().anyMatch(event ->
                event.outcome() == ServiceProbe.Outcome.INTERRUPTED));
    }

    @Test
    void deadlineInterruptsSlowCustoms() {
        var probe = new ServiceProbe();
        var timings = new ServiceTimings(
                Duration.ofMillis(10),
                Duration.ofMillis(10),
                Duration.ofMillis(10),
                Duration.ofSeconds(2));
        var aggregator = TestFixtures.structured(timings, probe, Duration.ofMillis(75));
        var request = new QuoteRequest(
                "timeout-1", "NL", "US", 10, Scenario.SLOW_CUSTOMS);
        var context = new RequestContext("timeout-1", CustomerTier.REGULAR, true);

        assertThrows(
                QuoteTimeoutException.class,
                () -> aggregator.quote(request, context));

        assertTrue(probe.events().stream().anyMatch(event ->
                event.service().equals("customs")
                        && event.outcome() == ServiceProbe.Outcome.INTERRUPTED));
    }
}

