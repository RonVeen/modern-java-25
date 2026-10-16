package workshop.concurrency;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SequentialQuoteAggregatorTest {
    @Test
    void combinesTheThreeServiceResults() throws InterruptedException {
        var probe = new ServiceProbe();
        var timings = new ServiceTimings(
                Duration.ZERO, Duration.ZERO, Duration.ZERO, Duration.ZERO);
        var aggregator = TestFixtures.sequential(timings, probe);
        var request = new QuoteRequest("quote-1", "NL", "DE", 10, Scenario.NORMAL);
        var context = new RequestContext("quote-1", CustomerTier.GOLD, true);

        var quote = aggregator.quote(request, context);

        assertEquals("INTERNATIONAL", quote.routing().zone());
        assertEquals(175, quote.pricing().discountInCents());
        assertEquals(1_500, quote.customs().feeInCents());
        assertEquals(3_075, quote.totalPriceInCents());
    }
}

