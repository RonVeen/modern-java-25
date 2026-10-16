package workshop.collections;

import java.time.Instant;

public record FailureAlert(String trackingId, Instant detectedAt, int consecutiveFailures) {
}

