package workshop.ceremony;

import java.time.Instant;

public record ScanEvent(
        String trackingId,
        String hub,
        ScanStatus status,
        int weightKg,
        Instant scannedAt) {
}
