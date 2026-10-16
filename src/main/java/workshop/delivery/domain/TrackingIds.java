package workshop.delivery.domain;

import java.util.Locale;
import java.util.Objects;

final class TrackingIds {
    private TrackingIds() {
    }

    static String normalize(String trackingId) {
        Objects.requireNonNull(trackingId, "trackingId");
        var normalized = trackingId.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z0-9-]{3,20}")) {
            throw new IllegalArgumentException("Invalid tracking id: " + trackingId);
        }
        return normalized;
    }
}

