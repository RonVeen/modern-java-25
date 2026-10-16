package workshop.collections;

import java.time.Instant;
import java.util.Objects;

public record ParcelScanEvent(
        String trackingId,
        Instant scannedAt,
        ScanStatus status,
        int weightKg) {

    public ParcelScanEvent {
        Objects.requireNonNull(trackingId, "trackingId");
        Objects.requireNonNull(scannedAt, "scannedAt");
        Objects.requireNonNull(status, "status");
        if (weightKg < 0) {
            throw new IllegalArgumentException("weightKg must not be negative");
        }
    }
}

