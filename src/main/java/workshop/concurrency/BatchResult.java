package workshop.concurrency;

import java.time.Duration;
import java.util.List;

public record BatchResult(List<DeliveryQuote> quotes, Duration elapsed) {
    public BatchResult {
        quotes = List.copyOf(quotes);
    }
}

