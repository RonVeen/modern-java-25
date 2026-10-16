package workshop.delivery.domain;

import java.util.Objects;

public record Letter(String trackingId, Address destination, Weight weight)
        implements Shipment {
    public Letter {
        trackingId = TrackingIds.normalize(trackingId);
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(weight, "weight");
    }
}

